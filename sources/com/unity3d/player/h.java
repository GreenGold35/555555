package com.unity3d.player;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;

final class h implements Application.ActivityLifecycleCallbacks {
    WeakReference a = new WeakReference((Object) null);
    Activity b;
    a c = null;

    class a extends View implements PixelCopy.OnPixelCopyFinishedListener {
        Bitmap a;

        a(Context context) {
            super(context);
        }

        public final void a(SurfaceView surfaceView) {
            Bitmap createBitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
            this.a = createBitmap;
            PixelCopy.request(surfaceView, createBitmap, this, new Handler(Looper.getMainLooper()));
        }

        public final void onPixelCopyFinished(int i) {
            if (i == 0) {
                setBackground(new LayerDrawable(new Drawable[]{new ColorDrawable(-16777216), new BitmapDrawable(getResources(), this.a)}));
            }
        }
    }

    h(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            this.b = activity;
            activity.getApplication().registerActivityLifecycleCallbacks(this);
        }
    }

    public final void a() {
        Activity activity = this.b;
        if (activity != null) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }

    public final void a(SurfaceView surfaceView) {
        if (PlatformSupport.NOUGAT_SUPPORT) {
            if (this.c == null) {
                this.c = new a(this.b);
            }
            this.c.a(surfaceView);
        }
    }

    public final void a(ViewGroup viewGroup) {
        a aVar = this.c;
        if (aVar != null && aVar.getParent() == null) {
            viewGroup.addView(this.c);
            viewGroup.bringChildToFront(this.c);
        }
    }

    public final void b() {
        this.c = null;
    }

    public final void b(ViewGroup viewGroup) {
        a aVar = this.c;
        if (aVar != null && aVar.getParent() != null) {
            viewGroup.removeView(this.c);
        }
    }

    public final void onActivityCreated(Activity activity, Bundle bundle) {
    }

    public final void onActivityDestroyed(Activity activity) {
    }

    public final void onActivityPaused(Activity activity) {
    }

    public final void onActivityResumed(Activity activity) {
        this.a = new WeakReference(activity);
    }

    public final void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    public final void onActivityStarted(Activity activity) {
    }

    public final void onActivityStopped(Activity activity) {
    }
}
