package com.google.android.gms.games.multiplayer.realtime;

import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface RoomStatusUpdateListener {
    void onConnectedToRoom(Room room);

    void onDisconnectedFromRoom(Room room);

    void onP2PConnected(String str);

    void onP2PDisconnected(String str);

    void onPeerDeclined(Room room, List<String> list);

    void onPeerInvitedToRoom(Room room, List<String> list);

    void onPeerJoined(Room room, List<String> list);

    void onPeerLeft(Room room, List<String> list);

    void onPeersConnected(Room room, List<String> list);

    void onPeersDisconnected(Room room, List<String> list);

    void onRoomAutoMatching(Room room);

    void onRoomConnecting(Room room);
}
