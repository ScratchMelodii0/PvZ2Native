/* A complete, minimal PvZ2Native plugin: it counts frames and says so.
 *
 * It does nothing useful on purpose. What it demonstrates is the shape every
 * plugin has -- decline politely on a build it does not support, resolve a
 * guest function by NAME rather than by address, hook it, and call the original
 * so the game carries on working.
 *
 * Build (adjust the include path to wherever this source tree is):
 *
 *   Linux    cc -std=c11 -shared -fPIC -I../../pvz2native/include \
 *               frame_counter.c -o frame_counter.so
 *   macOS    cc -std=c11 -dynamiclib -I../../pvz2native/include \
 *               frame_counter.c -o frame_counter.dylib
 *   Windows  gcc -std=c11 -shared -I../../pvz2native/include \
 *               frame_counter.c -o frame_counter.dll
 *
 * Then launch the game with [mods] plugins = 1 (the default) and watch the log.
 *
 * Note there is nothing to link against: the whole interface arrives as the
 * argument to pvz2_mod_init, so the plugin resolves no symbol from the host at
 * load time and cannot fail to load because of a version skew.
 */

#include <pvz2native/mods/mod_api.h>

static const Pvz2ModApi *g_api;
static unsigned g_frames;

/* Runs in place of the engine's Native_onDrawFrame.
 *
 * Calling the original is what makes this an observer rather than a
 * replacement: without that line the game would draw nothing at all, which is
 * exactly what a hook that forgets it looks like. */
static void on_draw_frame(Pvz2ModCall *call, const Pvz2ModHook *hook, void *user) {
    unsigned *frames = (unsigned *)user;

    /* NULL arguments re-use the registers as the engine passed them, which is
     * what an observing hook wants. */
    uint32_t result = g_api->call_original(call, hook, NULL, 0);

    if ((++*frames % 600u) == 0u) {
        g_api->log("%u frames drawn", *frames);
    }

    /* Hand the engine's caller back what the original returned. */
    g_api->set_result(call, result);
}

PVZ2_MOD_EXPORT int pvz2_mod_init(const Pvz2ModApi *api) {
    /* The host is older than this plugin was built against, so the table is
     * shorter than the fields below expect. Decline rather than read past it. */
    if (api->api_version < PVZ2_MOD_API_VERSION) return 1;
    g_api = api;

    api->log("loaded; game build is %s, mod folder is %s", api->game_version, api->mod_dir);

    /* By NAME, not by address: the core resolves it for whichever build is
     * loaded, so this line keeps working on a release that does not exist yet.
     * A build whose table does not map it is one this mod cannot run on. */
    uint32_t draw_frame = api->symbol("native.on_draw_frame");
    if (draw_frame == 0) {
        api->log("this build does not map native.on_draw_frame -- declining");
        return 1;
    }

    if (!api->hook(draw_frame, "example frame counter", on_draw_frame, &g_frames)) {
        api->log("could not hook onDrawFrame -- declining");
        return 1;
    }
    return 0;
}
