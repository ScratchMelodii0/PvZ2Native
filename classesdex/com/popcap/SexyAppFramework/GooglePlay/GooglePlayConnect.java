package com.popcap.SexyAppFramework.GooglePlay;

import com.popcap.SexyAppFramework.SexyAppFrameworkActivity;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public class GooglePlayConnect {
    BaseGameActivity GetGamesActivity() {
        return SexyAppFrameworkActivity.instance();
    }

    void Play_Connect_Silent() {
        GetGamesActivity().beginSilentSignIn();
    }

    void Play_Connect() {
        GetGamesActivity().beginUserInitiatedSignIn();
    }

    void Play_Disconnect() {
        GetGamesActivity().signOut();
    }

    boolean Play_IsConnected() {
        return GetGamesActivity().isSignedIn();
    }
}
