#ifndef PVZ2NATIVE_PVZ2_SESSION_H
#define PVZ2NATIVE_PVZ2_SESSION_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

/* A running instance of the emulated game: the loaded .so image plus all the
 * guest runtime state that has to survive between frames.
 *
 * The host owns the frame loop. It has to: SDL must pump its message queue
 * and swap the GL buffers around each Native_onDrawFrame, and neither is
 * possible if one call runs the whole boot plus every frame to completion. */
typedef struct pvz2_session pvz2_session_t;

/* Called periodically from inside long guest calls, on the thread that drives
 * them. The boot sequence runs for minutes without returning to the host, so
 * without this the window never pumps its message queue and the OS marks it
 * "Not responding" for the whole startup. Only ever invoked on the thread that
 * called pvz2_session_start / pvz2_session_frame, never on a guest worker, so
 * it is safe to touch the window and GL context from it. */
typedef void (*pvz2_host_pump_fn)(void);
void pvz2_session_set_host_pump(pvz2_host_pump_fn fn);

/* Copies a short description of what the guest is currently doing (which boot
 * phase, which constructor) into `out`. The boot takes minutes with a dark
 * window, so surfacing this -- e.g. in the window title -- is the only way to
 * tell a working startup from a hang. */
void pvz2_session_status(char *out, size_t n);

/* Loads the .so and runs the full boot sequence (init_array, JNI_OnLoad, the
 * registered-native lifecycle). Returns NULL if the image cannot be loaded. */
pvz2_session_t *pvz2_session_start(const char *so_path);

/* Runs a single Native_onDrawFrame. Returns non-zero while the session is
 * still usable; the host decides when to stop. */
int pvz2_session_frame(pvz2_session_t *session);

/* Ask the engine to re-render at the surface's CURRENT size, after the host has
 * published a new one with pvz2_surface_set (see <pvz2native/surface.h>).
 *
 * Deferred: the actual onSurfaceChanged runs on the frame thread at the start of
 * the next pvz2_session_frame, so this is safe to call from the SDL event
 * handler. Takes no size -- there is only one surface size and every layer
 * already reads it, which is also what makes repeated calls during a window drag
 * coalesce into one re-layout per frame at the latest size. */
void pvz2_session_request_resize(void);

/* Joins any leftover guest threads and frees the image. */
void pvz2_session_end(pvz2_session_t *session);

#ifdef __cplusplus
}
#endif

#endif
