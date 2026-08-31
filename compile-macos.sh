#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$ROOT_DIR/build-macos"
VENV_DIR="$ROOT_DIR/.venv-macos"

BUILD_TYPE="Release"
CLEAN=0
JOBS=""

usage() {
    cat <<USAGE
Usage: ./compile-macos.sh [options]

Build PvZ2Native natively for Apple Silicon macOS.

Options:
  -c, --clean       Delete build-macos before configuring
  -d, --debug       Build Debug
  -r, --release     Build Release (default)
  -j, --jobs N      Parallel build jobs
  -h, --help        Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -c|--clean)
            CLEAN=1
            shift
            ;;
        -d|--debug)
            BUILD_TYPE="Debug"
            shift
            ;;
        -r|--release)
            BUILD_TYPE="Release"
            shift
            ;;
        -j|--jobs)
            if [[ $# -lt 2 ]]; then
                echo "error: --jobs requires a number" >&2
                exit 1
            fi
            JOBS="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "error: unknown option: $1" >&2
            usage >&2
            exit 1
            ;;
    esac
done

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "error: compile-macos.sh must be run on macOS" >&2
    exit 1
fi

if [[ "$(uname -m)" != "arm64" ]]; then
    echo "error: this macOS build is currently validated only on Apple Silicon (arm64)" >&2
    exit 1
fi

for tool in cmake ninja python3; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        echo "error: required tool '$tool' was not found in PATH" >&2
        exit 1
    fi
done

BOOST_DIR="$ROOT_DIR/third_party/boost_1_84_0"

if [[ ! -d "$BOOST_DIR" ]]; then
    echo "error: bundled Boost directory not found:" >&2
    echo "  $BOOST_DIR" >&2
    exit 1
fi

if [[ ! -d "$VENV_DIR" ]]; then
    echo "==> Creating Python environment for GLAD"
    python3 -m venv "$VENV_DIR"
fi

PYTHON="$VENV_DIR/bin/python"

if ! "$PYTHON" -c 'import jinja2' >/dev/null 2>&1; then
    echo "==> Installing Jinja2 for GLAD"
    "$PYTHON" -m pip install "Jinja2>=3.1,<4"
fi

if [[ "$CLEAN" -eq 1 ]]; then
    echo "==> Removing previous macOS build"
    rm -rf "$BUILD_DIR"
fi

echo "==> Configuring PvZ2Native"
echo "    Host:       macOS arm64"
echo "    Build type: $BUILD_TYPE"
echo "    Build dir:  $BUILD_DIR"

cmake \
    -S "$ROOT_DIR" \
    -B "$BUILD_DIR" \
    -G Ninja \
    -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
    -DCMAKE_BUILD_TYPE="$BUILD_TYPE" \
    -DCMAKE_OSX_ARCHITECTURES=arm64 \
    -DBoost_ROOT="$BOOST_DIR" \
    -DPython_EXECUTABLE="$PYTHON" \
    -DCMAKE_CXX_FLAGS="-DFMT_CONSTEVAL=" \
    -DDYNARMIC_USE_PRECOMPILED_HEADERS=OFF

echo "==> Building PvZ2Native"

if [[ -n "$JOBS" ]]; then
    cmake --build "$BUILD_DIR" \
        --target pvz2native \
        --parallel "$JOBS"
else
    cmake --build "$BUILD_DIR" \
        --target pvz2native
fi

BINARY="$BUILD_DIR/pvz2native/pvz2native"

if [[ ! -x "$BINARY" ]]; then
    echo "error: build completed but executable was not found:" >&2
    echo "  $BINARY" >&2
    exit 1
fi

echo
echo "==> Build complete"
file "$BINARY"

echo
echo "Executable:"
echo "  $BINARY"
echo
echo "Game files are NOT included."
echo "Provide your own legal libPVZ2.so and matching .obb as documented in README.md."
