package com.unity3d.player;

import android.content.Context;
import com.unity3d.player.b;

public class AudioVolumeHandler implements b.C0003b {
    private b a;

    AudioVolumeHandler(Context context) {
        b bVar = new b(context);
        this.a = bVar;
        bVar.a(this);
    }

    public final void a() {
        this.a.a();
        this.a = null;
    }

    public final native void onAudioVolumeChanged(int i);
}
