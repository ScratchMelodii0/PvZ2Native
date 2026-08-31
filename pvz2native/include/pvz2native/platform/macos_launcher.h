#ifndef PVZ2NATIVE_PLATFORM_MACOS_LAUNCHER_H
#define PVZ2NATIVE_PLATFORM_MACOS_LAUNCHER_H

#include <pvz2native/config.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Shows the native macOS pre-launch video settings dialog.
 * Returns 1 when the user chooses Play, 0 when they cancel.
 * On Play, only the [video] keys in config.ini are updated. */
int pvz2_macos_show_launcher(const char *ini_path, const pvz2_config_t *cfg);

#ifdef __cplusplus
}
#endif

#endif
