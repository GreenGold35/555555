package com.unity3d.player;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

final class b {
    private final Context a;
    private final AudioManager b;
    private a c;

    private class a extends ContentObserver {
        private final C0003b b;
        private final AudioManager c;
        private final int d = 3;
        private int e;

        public a(Handler handler, AudioManager audioManager, int i, C0003b bVar) {
            super(handler);
            this.c = audioManager;
            this.b = bVar;
            this.e = audioManager.getStreamVolume(3);
        }

        public final boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        public final void onChange(boolean z, Uri uri) {
            int streamVolume;
            AudioManager audioManager = this.c;
            if (audioManager != null && this.b != null && (streamVolume = audioManager.getStreamVolume(this.d)) != this.e) {
                this.e = streamVolume;
                this.b.onAudioVolumeChanged(streamVolume);
            }
        }
    }

    /* renamed from: com.unity3d.player.b$b  reason: collision with other inner class name */
    public interface C0003b {
        void onAudioVolumeChanged(int i);
    }

    public b(Context context) {
        this.a = context;
        this.b = (AudioManager) context.getSystemService("audio");
    }

    public final void a() {
        if (this.c != null) {
            this.a.getContentResolver().unregisterContentObserver(this.c);
            this.c = null;
        }
    }

    public final void a(C0003b bVar) {
        this.c = new a(new Handler(), this.b, 3, bVar);
        this.a.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, this.c);
    }
}
