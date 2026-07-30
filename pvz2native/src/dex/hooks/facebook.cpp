/* com.popcap.SexyAppFramework.AndroidFacebookDriver
 *
 * Facebook integration, which does not exist in this port and does not need to:
 * the engine treats "not logged in" as an ordinary state.
 *
 * What it does NOT tolerate is a null String. The engine feeds
 * GetAccessToken's result straight into a std::string, and libstdc++ aborts on
 * a null char* with
 *     terminate called after throwing an instance of 'std::logic_error'
 *       what(): basic_string::_S_construct null not valid
 * which is precisely what killed the game the moment PLAY was pressed -- the
 * social/profile code runs as part of that transition. An unhooked method
 * returns null, so the fix is not "return nothing", it is "return an empty
 * string": absent and empty are the same thing to the caller here, and only one
 * of them survives the constructor.
 *
 * The distinction matters and is not general: for other methods (notably
 * Resources_GetAssetFileInfo) null IS the correct, load-bearing answer for
 * "missing", so string returns are fixed one at a time, where evidence shows
 * the engine cannot cope -- not blanket-converted across the JNI layer.
 */

#include <pvz2native/dex/dex.h>

namespace pvz2native {
namespace dex {
namespace {

constexpr const char *kFacebook = "com/popcap/SexyAppFramework/AndroidFacebookDriver";

/* No session, so no token: empty rather than null (see the file comment) and
 * "not connected" for every session-state query, both from dex.h's shared
 * stubs -- hook_empty_string, hook_not_connected. */

}  // namespace

void register_facebook(HookTable &t) {
    /* Every String getter, for the reason above. RefreshFriendsLists fills the
     * two list getters asynchronously on a device, so an empty answer is what
     * they legitimately return before it completes. */
    t.add(kFacebook, "GetAccessToken", hook_empty_string);
    t.add(kFacebook, "GetFriendName", hook_empty_string);
    t.add(kFacebook, "GetFriendPictureURL", hook_empty_string);
    t.add(kFacebook, "getAppFriends", hook_empty_string);
    t.add(kFacebook, "getNonAppFriends", hook_empty_string);

    /* The session-state queries. These were "IsLoggedIn" and "IsSessionValid"
     * until the names were checked against the decompiled driver: neither
     * exists, so both hooks were dead. The real ones are below -- and note
     * SessionIsValid, which is the transposition that hid the mistake. */
    t.add(kFacebook, "IsSessionOpen", hook_not_connected);
    t.add(kFacebook, "IsSessionOpening", hook_not_connected);
    t.add(kFacebook, "SessionIsValid", hook_not_connected);
    t.add(kFacebook, "HasPermissions", hook_not_connected);
    t.add(kFacebook, "OpenSessionForRead", hook_not_connected);
    t.add(kFacebook, "AddPublishPermissions", hook_not_connected);
}

}  // namespace dex
}  // namespace pvz2native
