package com.google.android.gms.games.multiplayer;

import com.google.android.gms.common.data.f;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public final class InvitationBuffer extends f<Invitation> {
    public InvitationBuffer(com.google.android.gms.common.data.d dataHolder) {
        super(dataHolder);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.android.gms.common.data.f
    /* JADX INFO: renamed from: getEntry, reason: merged with bridge method [inline-methods] */
    public Invitation a(int rowIndex, int numChildren) {
        return new b(this.S, rowIndex, numChildren);
    }

    @Override // com.google.android.gms.common.data.f
    protected String getPrimaryDataMarkerColumn() {
        return "external_invitation_id";
    }
}
