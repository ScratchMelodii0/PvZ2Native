#!/bin/bash
#
# compile-macos.sh - Native Apple Silicon build script for PvZ2Native.
#
# Mirrors the original compile.sh philosophy:
#   1. build the open-source project from source with CMake
#   2. optionally prepare user-supplied game assets from game/
#
# The game is NOT included and nothing is downloaded from EA/PopCap.
# Easy path: ONE legal APK + ONE matching OBB in game/.
# Advanced path: libPVZ2.so + ONE matching OBB in game/.
#
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="${PROJECT_ROOT}/build-macos"
GAME_DIR="${PROJECT_ROOT}/game"
EXE_DIR="${BUILD_DIR}/pvz2native"
LIB_DIR="${EXE_DIR}/lib"
LOG_FILE="${BUILD_DIR}/compile-macos.log"
VENV_DIR="${PROJECT_ROOT}/.venv-macos"
BOOST_ROOT="${PROJECT_ROOT}/third_party/boost_1_84_0"

BUILD_TYPE="Release"
CLEAN="no"
NO_ASSETS="no"
JOBS=""
VERBOSE="no"

# PvZ2Native runs the Android ARM32 library through Dynarmic A32.
APK_LIB_PATH="lib/armeabi-v7a/libPVZ2.so"

if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
    RED=$'\e[31m'
    GREEN=$'\e[32m'
    YELLOW=$'\e[33m'
    BLUE=$'\e[34m'
    BOLD=$'\e[1m'
    RESET=$'\e[0m'
else
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    BOLD=''
    RESET=''
fi

info()    { echo "${BLUE}[INFO]${RESET} $*"; }
success() { echo "${GREEN}[OK]${RESET} $*"; }
warn()    { echo "${YELLOW}[WARN]${RESET} $*" >&2; }
error()   { echo "${RED}[ERROR]${RESET} $*" >&2; }
fail()    { error "$*"; exit 1; }

usage() {
    cat <<EOF
Usage: $0 [OPTIONS]

Build PvZ2Native natively for macOS Apple Silicon and optionally prepare
user-supplied game assets from game/.

Options:
  -c, --clean          Delete build-macos/ before configuring
  -d, --debug          Build in Debug mode
  -r, --release        Build in Release mode (default)
  -j, --jobs N         Use N parallel jobs
  -n, --no-assets      Build only; do not copy/extract game assets
  -v, --verbose        Print full CMake/build output
  -h, --help           Show this help

Easy game setup:
  Put exactly ONE APK and ONE matching OBB directly inside:

      ${GAME_DIR}/

  Example:
      game/
      ├── PlantsVsZombies2.apk
      └── main.147.com.ea.game.pvz2_row.obb

  You do NOT need to extract the APK yourself. This script extracts the ARM32
  libPVZ2.so automatically.

Advanced setup:
      game/
      ├── libPVZ2.so
      └── <matching game>.obb

The game is not included and this script never downloads game files.
EOF
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -c|--clean) CLEAN="yes"; shift ;;
            -d|--debug) BUILD_TYPE="Debug"; shift ;;
            -r|--release) BUILD_TYPE="Release"; shift ;;
            -j|--jobs)
                if [[ -z "${2:-}" || "$2" =~ ^- ]] || ! [[ "$2" =~ ^[0-9]+$ ]] || [[ "$2" == "0" ]]; then
                    fail "Option $1 requires a positive integer"
                fi
                JOBS="$2"
                shift 2
                ;;
            -n|--no-assets) NO_ASSETS="yes"; shift ;;
            -v|--verbose) VERBOSE="yes"; shift ;;
            -h|--help) usage; exit 0 ;;
            -*) fail "Unknown option: $1 (use --help)" ;;
            *) fail "Unexpected argument: $1" ;;
        esac
    done
}

check_host_and_deps() {
    [[ "$(uname -s)" == "Darwin" ]] || fail "This script is for macOS only."
    [[ "$(uname -m)" == "arm64" ]] || fail "Apple Silicon (arm64) is required."

    local missing=()
    for cmd in cmake ninja python3 unzip file; do
        command -v "$cmd" >/dev/null 2>&1 || missing+=("$cmd")
    done

    if ! xcrun --find clang >/dev/null 2>&1; then
        missing+=("Xcode Command Line Tools / Apple Clang")
    fi

    [[ -d "${BOOST_ROOT}" ]] || fail "Bundled Boost not found: ${BOOST_ROOT}"

    if [[ ${#missing[@]} -gt 0 ]]; then
        echo
        error "Missing required tools: ${missing[*]}"
        echo "Install Apple's command line tools with:"
        echo "  xcode-select --install"
        echo
        echo "With Homebrew installed, install the remaining tools with:"
        echo "  brew install cmake ninja python3"
        exit 1
    fi
}

prepare_python() {
    if [[ ! -x "${VENV_DIR}/bin/python" ]]; then
        info "Creating Python environment for GLAD..."
        python3 -m venv "${VENV_DIR}"
    fi

    if ! "${VENV_DIR}/bin/python" -c 'import jinja2' >/dev/null 2>&1; then
        info "Installing Jinja2 in .venv-macos..."
        "${VENV_DIR}/bin/python" -m pip install "Jinja2>=3.1,<4"
    fi
}

scan_game_dir() {
    shopt -s nullglob nocaseglob
    GAME_APKS=("${GAME_DIR}"/*.apk)
    GAME_OBBS=("${GAME_DIR}"/*.obb)
    GAME_SO=""
    [[ -f "${GAME_DIR}/libPVZ2.so" ]] && GAME_SO="${GAME_DIR}/libPVZ2.so"
    shopt -u nocaseglob
}

show_asset_plan() {
    echo
    echo "${BOLD}Game files${RESET}"
    echo "  Folder: ${GAME_DIR}"

    if [[ ! -d "${GAME_DIR}" ]]; then
        warn "game/ does not exist."
        echo "  Create it and put your own APK + matching OBB inside."
        return
    fi

    scan_game_dir

    if [[ -n "${GAME_SO}" ]]; then
        success "Found libPVZ2.so (advanced setup)"
        if [[ ${#GAME_APKS[@]} -gt 0 ]]; then
            info "An APK is also present; libPVZ2.so takes priority."
        fi
    elif [[ ${#GAME_APKS[@]} -eq 1 ]]; then
        success "Found APK: $(basename "${GAME_APKS[0]}")"
        info "libPVZ2.so will be extracted automatically."
    elif [[ ${#GAME_APKS[@]} -eq 0 ]]; then
        warn "No APK or libPVZ2.so found."
        echo "  Put ONE legal PvZ2 APK directly inside game/."
    else
        warn "More than one APK found."
        echo "  Leave only ONE APK in game/."
    fi

    if [[ ${#GAME_OBBS[@]} -eq 1 ]]; then
        success "Found OBB: $(basename "${GAME_OBBS[0]}")"
    elif [[ ${#GAME_OBBS[@]} -eq 0 ]]; then
        warn "No OBB found."
        echo "  Put the matching .obb directly inside game/."
    else
        warn "More than one OBB found."
        echo "  Leave only ONE OBB in game/."
    fi
}

configure() {
    info "Configuring PvZ2Native"
    echo "    Host:       macOS arm64"
    echo "    Build type: ${BUILD_TYPE}"
    echo "    Build dir:  ${BUILD_DIR}"

    local args=(
        -G Ninja
        -DCMAKE_POLICY_VERSION_MINIMUM=3.5
        -DCMAKE_BUILD_TYPE="${BUILD_TYPE}"
        -DCMAKE_OSX_ARCHITECTURES=arm64
        -DBoost_ROOT="${BOOST_ROOT}"
        -DPython_EXECUTABLE="${VENV_DIR}/bin/python"
        -DCMAKE_CXX_FLAGS=-DFMT_CONSTEVAL=
        -DDYNARMIC_USE_PRECOMPILED_HEADERS=OFF
        "${PROJECT_ROOT}"
    )

    if [[ "${VERBOSE}" == "yes" ]]; then
        cmake "${args[@]}"
    else
        cmake "${args[@]}" >>"${LOG_FILE}" 2>&1 || {
            error "CMake configuration failed."
            echo "See: ${LOG_FILE}"
            exit 1
        }
    fi
}

build_project() {
    info "Building PvZ2Native"

    local args=(--build "${BUILD_DIR}" --target pvz2native)
    if [[ -n "${JOBS}" ]]; then
        args+=(--parallel "${JOBS}")
    fi

    if [[ "${VERBOSE}" == "yes" ]]; then
        cmake "${args[@]}"
    else
        cmake "${args[@]}" >>"${LOG_FILE}" 2>&1 || {
            error "Build failed."
            echo "See: ${LOG_FILE}"
            exit 1
        }
    fi

    [[ -x "${EXE_DIR}/pvz2native" ]] || fail "Executable was not produced."
}

validate_arm32_so() {
    local so="$1"
    local desc
    desc="$(file "$so")"

    if [[ "$desc" != *"ELF 32-bit"* || "$desc" != *"ARM"* ]]; then
        error "libPVZ2.so is not the ARM32 library PvZ2Native expects."
        echo "Detected:"
        echo "  $desc"
        return 1
    fi
    return 0
}

prepare_assets() {
    if [[ "${NO_ASSETS}" == "yes" ]]; then
        info "Skipping game assets (--no-assets)"
        return 0
    fi

    if [[ ! -d "${GAME_DIR}" ]]; then
        warn "No game/ folder. Build succeeded, but game files were not prepared."
        return 0
    fi

    scan_game_dir

    if [[ -z "${GAME_SO}" && ${#GAME_APKS[@]} -gt 1 ]]; then
        fail "More than one APK is inside game/. Leave only one and run again."
    fi
    if [[ ${#GAME_OBBS[@]} -gt 1 ]]; then
        fail "More than one OBB is inside game/. Leave only one and run again."
    fi

    mkdir -p "${LIB_DIR}"

    local source_so=""
    local temp_so=""

    if [[ -n "${GAME_SO}" ]]; then
        source_so="${GAME_SO}"
        info "Using game/libPVZ2.so"
    elif [[ ${#GAME_APKS[@]} -eq 1 ]]; then
        local apk="${GAME_APKS[0]}"
        temp_so="${BUILD_DIR}/libPVZ2.extracted.tmp"
        rm -f "${temp_so}"

        info "Extracting ${APK_LIB_PATH}"
        echo "    from: $(basename "${apk}")"

        if ! unzip -p "${apk}" "${APK_LIB_PATH}" >"${temp_so}" 2>/dev/null; then
            rm -f "${temp_so}"
            fail "This APK does not contain ${APK_LIB_PATH}. PvZ2Native needs the ARM32 library."
        fi

        [[ -s "${temp_so}" ]] || {
            rm -f "${temp_so}"
            fail "The APK extraction produced an empty libPVZ2.so."
        }

        source_so="${temp_so}"
        success "libPVZ2.so extracted from APK"
    else
        warn "No APK or libPVZ2.so in game/. Build succeeded; no library was copied."
    fi

    if [[ -n "${source_so}" ]]; then
        validate_arm32_so "${source_so}" || {
            rm -f "${temp_so}"
            fail "Use an APK/library containing the ARM32 armeabi-v7a build."
        }
        cp "${source_so}" "${LIB_DIR}/libPVZ2.so"
        rm -f "${temp_so}"
        success "Prepared lib/libPVZ2.so"
    fi

    if [[ ${#GAME_OBBS[@]} -eq 1 ]]; then
        rm -f "${LIB_DIR}"/*.obb
        cp "${GAME_OBBS[0]}" "${LIB_DIR}/"
        success "Prepared lib/$(basename "${GAME_OBBS[0]}")"
    else
        warn "No OBB in game/. Build succeeded; no OBB was copied."
    fi
}

summary() {
    echo
    echo "${BOLD}============================================================${RESET}"
    echo "${BOLD} PvZ2Native macOS build complete${RESET}"
    echo "${BOLD}============================================================${RESET}"
    echo
    file "${EXE_DIR}/pvz2native"
    echo
    echo "Executable:"
    echo "  ${EXE_DIR}/pvz2native"
    echo

    local have_so="no"
    local have_obb="no"
    [[ -f "${LIB_DIR}/libPVZ2.so" ]] && have_so="yes"

    shopt -s nullglob
    local out_obbs=("${LIB_DIR}"/*.obb)
    shopt -u nullglob
    [[ ${#out_obbs[@]} -gt 0 ]] && have_obb="yes"

    if [[ "${NO_ASSETS}" == "yes" ]]; then
        info "Game asset handling was disabled."
    elif [[ "${have_so}" == "yes" && "${have_obb}" == "yes" ]]; then
        success "Game files are ready."
        echo
        echo "Start PvZ2Native with:"
        echo "  ./build-macos/pvz2native/pvz2native"
        echo
        echo "The macOS launcher lets you choose resolution, FPS and fullscreen."
    else
        warn "The program compiled, but game files are incomplete."
        echo
        echo "For the easy setup, put these TWO files directly in:"
        echo "  ${GAME_DIR}/"
        echo
        echo "  1. ONE legal PvZ2 .apk"
        echo "  2. ONE matching .obb"
        echo
        echo "Then run:"
        echo "  ./compile-macos.sh"
        echo
        echo "You do NOT need to extract libPVZ2.so yourself."
    fi

    echo
    echo "Build log:"
    echo "  ${LOG_FILE}"
    echo
    echo "Game files are never included in the repository or downloaded by this script."
}

main() {
    parse_args "$@"
    check_host_and_deps
    show_asset_plan
    prepare_python

    if [[ "${CLEAN}" == "yes" ]]; then
        warn "Cleaning build directory: ${BUILD_DIR}"
        rm -rf "${BUILD_DIR}"
    fi

    mkdir -p "${BUILD_DIR}"
    : >"${LOG_FILE}"

    cd "${BUILD_DIR}"
    configure
    cd "${PROJECT_ROOT}"

    build_project
    prepare_assets
    summary
}

trap 'error "Interrupted"; exit 130' INT TERM
main "$@"
