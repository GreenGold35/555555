package com.unity3d.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class HFPStatus {
    private Context a;
    private BroadcastReceiver b = null;
    private Intent c = null;
    /* access modifiers changed from: private */
    public boolean d = false;
    /* access modifiers changed from: private */
    public AudioManager e = null;
    private boolean f = false;
    /* access modifiers changed from: private */
    public int g = a.a;

    enum a {
        ;

        static {
            c = new int[]{1, 2};
        }
    }

    public HFPStatus(Context context) {
        this.a = context;
        this.e = (AudioManager) context.getSystemService("audio");
        initHFPStatusJni();
    }

    private void b() {
        BroadcastReceiver broadcastReceiver = this.b;
        if (broadcastReceiver != null) {
            this.a.unregisterReceiver(broadcastReceiver);
            this.b = null;
            this.c = null;
        }
        this.g = a.a;
    }

    /* access modifiers changed from: private */
    public void c() {
        if (this.f) {
            this.f = false;
            this.e.stopBluetoothSco();
        }
    }

    private final native void deinitHFPStatusJni();

    private final native void initHFPStatusJni();

    public final void a() {
        clearHFPStat();
        deinitHFPStatusJni();
    }

    /* access modifiers changed from: protected */
    public void clearHFPStat() {
        b();
        c();
    }

    /* access modifiers changed from: protected */
    public boolean getHFPStat() {
        return this.g == a.b;
    }

    /* access modifiers changed from: protected */
    public void requestHFPStat() {
        clearHFPStat();
        AnonymousClass1 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1) == 1) {
                    int unused = HFPStatus.this.g = a.b;
                    HFPStatus.this.c();
                    if (HFPStatus.this.d) {
                        HFPStatus.this.e.setMode(3);
                    }
                }
            }
        };
        this.b = r0;
        this.c = this.a.registerReceiver(r0, new IntentFilter("android.media.ACTION_SCO_AUDIO_STATE_UPDATED"));
        try {
            this.f = true;
            this.e.startBluetoothSco();
        } catch (NullPointerException unused) {
            f.Log(5, "startBluetoothSco() failed. no bluetooth device connected.");
        }
    }

    /* access modifiers changed from: protected */
    public void setHFPRecordingStat(boolean z) {
        this.d = z;
        if (!z) {
            this.e.setMode(0);
        }
    }
}
