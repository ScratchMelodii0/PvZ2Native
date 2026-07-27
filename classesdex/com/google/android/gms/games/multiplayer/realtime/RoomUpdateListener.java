package com.google.android.gms.games.multiplayer.realtime;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface RoomUpdateListener {
    void onJoinedRoom(int i, Room room);

    void onLeftRoom(int i, String str);

    void onRoomConnected(int i, Room room);

    void onRoomCreated(int i, Room room);
}
