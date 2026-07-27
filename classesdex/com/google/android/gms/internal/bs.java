package com.google.android.gms.internal;

import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.gms.internal.bp;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import java.util.List;

/* JADX INFO: loaded from: /tmp/decompiler/4afe3c4d055e4f0b9319eb842de572b0/classes.dex */
public interface bs extends IInterface {

    public static abstract class a extends Binder implements bs {

        /* JADX INFO: renamed from: com.google.android.gms.internal.bs$a$a, reason: collision with other inner class name */
        private static class C0015a implements bs {
            private IBinder a;

            C0015a(IBinder iBinder) {
                this.a = iBinder;
            }

            @Override // com.google.android.gms.internal.bs
            public void a(ak akVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    if (akVar != null) {
                        parcelObtain.writeInt(1);
                        akVar.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.a.transact(4, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    this.a.transact(8, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, int i, int i2, int i3, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeInt(i2);
                    parcelObtain.writeInt(i3);
                    parcelObtain.writeString(str);
                    this.a.transact(16, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, int i, int i2, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeInt(i2);
                    parcelObtain.writeString(str);
                    this.a.transact(39, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, int i, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str);
                    this.a.transact(20, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, int i, String str, Uri uri, String str2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str);
                    if (uri != null) {
                        parcelObtain.writeInt(1);
                        uri.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    parcelObtain.writeString(str2);
                    this.a.transact(32, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, int i, String str, Uri uri, String str2, String str3) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str);
                    if (uri != null) {
                        parcelObtain.writeInt(1);
                        uri.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    parcelObtain.writeString(str2);
                    parcelObtain.writeString(str3);
                    this.a.transact(14, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, Uri uri, Bundle bundle) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    if (uri != null) {
                        parcelObtain.writeInt(1);
                        uri.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    if (bundle != null) {
                        parcelObtain.writeInt(1);
                        bundle.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.a.transact(9, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, co coVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    if (coVar != null) {
                        parcelObtain.writeInt(1);
                        coVar.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.a.transact(30, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, int i, String str2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str2);
                    this.a.transact(36, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, bv bvVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    if (bvVar != null) {
                        parcelObtain.writeInt(1);
                        bvVar.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.a.transact(25, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, String str2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeString(str2);
                    this.a.transact(2, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, String str2, int i, String str3) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeString(str2);
                    parcelObtain.writeInt(i);
                    parcelObtain.writeString(str3);
                    this.a.transact(12, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, String str2, boolean z, String str3) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeString(str2);
                    parcelObtain.writeInt(z ? 1 : 0);
                    parcelObtain.writeString(str3);
                    this.a.transact(37, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, List<x> list) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeTypedList(list);
                    this.a.transact(28, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, List<x> list, Bundle bundle) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeTypedList(list);
                    if (bundle != null) {
                        parcelObtain.writeInt(1);
                        bundle.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.a.transact(31, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, List<String> list, List<String> list2, List<String> list3) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeStringList(list);
                    parcelObtain.writeStringList(list2);
                    parcelObtain.writeStringList(list3);
                    this.a.transact(23, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, List<x> list, boolean z) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeTypedList(list);
                    parcelObtain.writeInt(z ? 1 : 0);
                    this.a.transact(29, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, boolean z) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeInt(z ? 1 : 0);
                    this.a.transact(21, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, String str, boolean z, String str2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    parcelObtain.writeInt(z ? 1 : 0);
                    parcelObtain.writeString(str2);
                    this.a.transact(27, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, List<String> list) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeStringList(list);
                    this.a.transact(34, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void a(bp bpVar, boolean z, boolean z2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeInt(z ? 1 : 0);
                    parcelObtain.writeInt(z2 ? 1 : 0);
                    this.a.transact(22, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.a;
            }

            @Override // com.google.android.gms.internal.bs
            public void b(bp bpVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    this.a.transact(13, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void b(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(3, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void c(bp bpVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    this.a.transact(19, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void c(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(7, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void clearDefaultAccount() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    this.a.transact(6, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void d(bp bpVar) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    this.a.transact(38, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void d(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(10, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void e(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(18, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void f(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(24, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void f(String str, String str2) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeString(str);
                    parcelObtain.writeString(str2);
                    this.a.transact(11, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void g(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(26, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public String getAccountName() throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    this.a.transact(5, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    return parcelObtain2.readString();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void h(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(33, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void i(bp bpVar, String str) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeStrongBinder(bpVar != null ? bpVar.asBinder() : null);
                    parcelObtain.writeString(str);
                    this.a.transact(35, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }

            @Override // com.google.android.gms.internal.bs
            public void removeMoment(String momentId) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken("com.google.android.gms.plus.internal.IPlusService");
                    parcelObtain.writeString(momentId);
                    this.a.transact(17, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                } finally {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                }
            }
        }

        public static bs ab(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.plus.internal.IPlusService");
            return (iInterfaceQueryLocalInterface == null || !(iInterfaceQueryLocalInterface instanceof bs)) ? new C0015a(iBinder) : (bs) iInterfaceQueryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 2:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 3:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    b(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 4:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(parcel.readInt() != 0 ? ak.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                case 5:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    String accountName = getAccountName();
                    parcel2.writeNoException();
                    parcel2.writeString(accountName);
                    return true;
                case 6:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    clearDefaultAccount();
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    c(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 8:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 9:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt() != 0 ? (Uri) Uri.CREATOR.createFromParcel(parcel) : null, parcel.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                case 10:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    d(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 11:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    f(parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 12:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 13:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    b(bp.a.Y(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE /* 14 */:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt(), parcel.readString(), parcel.readInt() != 0 ? (Uri) Uri.CREATOR.createFromParcel(parcel) : null, parcel.readString(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 16:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case IDownloaderClient.STATE_FAILED_SDCARD_FULL /* 17 */:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    removeMoment(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case IDownloaderClient.STATE_FAILED_CANCELED /* 18 */:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    e(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 19:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    c(bp.a.Y(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 20:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 21:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 22:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt() != 0, parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 23:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.createStringArrayList(), parcel.createStringArrayList(), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 24:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    f(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 25:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readInt() != 0 ? bv.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                case 26:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    g(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 27:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readInt() != 0, parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 28:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.createTypedArrayList(x.CREATOR));
                    parcel2.writeNoException();
                    return true;
                case 29:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.createTypedArrayList(x.CREATOR), parcel.readInt() != 0);
                    parcel2.writeNoException();
                    return true;
                case 30:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt() != 0 ? co.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                case 31:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.createTypedArrayList(x.CREATOR), parcel.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(parcel) : null);
                    parcel2.writeNoException();
                    return true;
                case AccessibilityNodeInfoCompat.ACTION_LONG_CLICK /* 32 */:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt(), parcel.readString(), parcel.readInt() != 0 ? (Uri) Uri.CREATOR.createFromParcel(parcel) : null, parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 33:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    h(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 34:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 35:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    i(bp.a.Y(parcel.readStrongBinder()), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 36:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readInt(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 37:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readString(), parcel.readString(), parcel.readInt() != 0, parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 38:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    d(bp.a.Y(parcel.readStrongBinder()));
                    parcel2.writeNoException();
                    return true;
                case 39:
                    parcel.enforceInterface("com.google.android.gms.plus.internal.IPlusService");
                    a(bp.a.Y(parcel.readStrongBinder()), parcel.readInt(), parcel.readInt(), parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 1598968902:
                    parcel2.writeString("com.google.android.gms.plus.internal.IPlusService");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void a(ak akVar) throws RemoteException;

    void a(bp bpVar) throws RemoteException;

    void a(bp bpVar, int i, int i2, int i3, String str) throws RemoteException;

    void a(bp bpVar, int i, int i2, String str) throws RemoteException;

    void a(bp bpVar, int i, String str) throws RemoteException;

    void a(bp bpVar, int i, String str, Uri uri, String str2) throws RemoteException;

    void a(bp bpVar, int i, String str, Uri uri, String str2, String str3) throws RemoteException;

    void a(bp bpVar, Uri uri, Bundle bundle) throws RemoteException;

    void a(bp bpVar, co coVar) throws RemoteException;

    void a(bp bpVar, String str) throws RemoteException;

    void a(bp bpVar, String str, int i, String str2) throws RemoteException;

    void a(bp bpVar, String str, bv bvVar) throws RemoteException;

    void a(bp bpVar, String str, String str2) throws RemoteException;

    void a(bp bpVar, String str, String str2, int i, String str3) throws RemoteException;

    void a(bp bpVar, String str, String str2, boolean z, String str3) throws RemoteException;

    void a(bp bpVar, String str, List<x> list) throws RemoteException;

    void a(bp bpVar, String str, List<x> list, Bundle bundle) throws RemoteException;

    void a(bp bpVar, String str, List<String> list, List<String> list2, List<String> list3) throws RemoteException;

    void a(bp bpVar, String str, List<x> list, boolean z) throws RemoteException;

    void a(bp bpVar, String str, boolean z) throws RemoteException;

    void a(bp bpVar, String str, boolean z, String str2) throws RemoteException;

    void a(bp bpVar, List<String> list) throws RemoteException;

    void a(bp bpVar, boolean z, boolean z2) throws RemoteException;

    void b(bp bpVar) throws RemoteException;

    void b(bp bpVar, String str) throws RemoteException;

    void c(bp bpVar) throws RemoteException;

    void c(bp bpVar, String str) throws RemoteException;

    void clearDefaultAccount() throws RemoteException;

    void d(bp bpVar) throws RemoteException;

    void d(bp bpVar, String str) throws RemoteException;

    void e(bp bpVar, String str) throws RemoteException;

    void f(bp bpVar, String str) throws RemoteException;

    void f(String str, String str2) throws RemoteException;

    void g(bp bpVar, String str) throws RemoteException;

    String getAccountName() throws RemoteException;

    void h(bp bpVar, String str) throws RemoteException;

    void i(bp bpVar, String str) throws RemoteException;

    void removeMoment(String str) throws RemoteException;
}
