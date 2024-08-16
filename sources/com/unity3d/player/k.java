package com.unity3d.player;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

final class k {
    private Context a;
    private b b;

    public interface a {
        void b();
    }

    private class b extends ContentObserver {
        private a b;

        public b(Handler handler, a aVar) {
            super(handler);
            this.b = aVar;
        }

        public final boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        public final void onChange(boolean z) {
            a aVar = this.b;
            if (aVar != null) {
                aVar.b();
            }
        }
    }

    public k(Context context) {
        this.a = context;
    }

    public final void a() {
        if (this.b != null) {
            this.a.getContentResolver().unregisterContentObserver(this.b);
            this.b = null;
        }
    }

    public final void a(a aVar, String str) {
        this.b = new b(new Handler(Looper.getMainLooper()), aVar);
        this.a.getContentResolver().registerContentObserver(Settings.System.getUriFor(str), true, this.b);
    }
}
