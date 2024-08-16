package com.unity3d.player;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.unity3d.player.UnityPermissions;
import com.unity3d.player.j;
import com.unity3d.player.o;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class UnityPlayer extends FrameLayout implements IUnityPlayerLifecycleEvents {
    private static final int ANR_TIMEOUT_SECONDS = 4;
    private static final String ARCORE_ENABLE_METADATA_NAME = "unity.arcore-enable";
    private static final String AUTO_REPORT_FULLY_DRAWN_ENABLE_METADATA_NAME = "unity.auto-report-fully-drawn";
    private static final String LAUNCH_FULLSCREEN = "unity.launch-fullscreen";
    private static final int RUN_STATE_CHANGED_MSG_CODE = 2269;
    private static final String SPLASH_ENABLE_METADATA_NAME = "unity.splash-enable";
    private static final String SPLASH_MODE_METADATA_NAME = "unity.splash-mode";
    public static Activity currentActivity;
    /* access modifiers changed from: private */
    public Activity mActivity;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public SurfaceView mGlView;
    Handler mHandler;
    /* access modifiers changed from: private */
    public int mInitialScreenOrientation;
    private boolean mIsFullscreen;
    private BroadcastReceiver mKillingIsMyBusiness;
    /* access modifiers changed from: private */
    public boolean mMainDisplayOverride;
    /* access modifiers changed from: private */
    public int mNaturalOrientation;
    private OrientationEventListener mOrientationListener;
    private boolean mProcessKillRequested;
    /* access modifiers changed from: private */
    public boolean mQuitting;
    i mSoftInputDialog;
    private m mState;
    /* access modifiers changed from: private */
    public o mVideoPlayerProxy;
    private GoogleARCoreApi m_ARCoreApi;
    private boolean m_AddPhoneCallListener;
    private AudioVolumeHandler m_AudioVolumeHandler;
    private Camera2Wrapper m_Camera2Wrapper;
    private ClipboardManager m_ClipboardManager;
    private final ConcurrentLinkedQueue m_Events;
    private a m_FakeListener;
    private HFPStatus m_HFPStatus;
    e m_MainThread;
    private NetworkConnectivity m_NetworkConnectivity;
    private OrientationLockListener m_OrientationLockListener;
    /* access modifiers changed from: private */
    public h m_PersistentUnitySurface;
    private c m_PhoneCallListener;
    /* access modifiers changed from: private */
    public j m_SplashScreen;
    private TelephonyManager m_TelephonyManager;
    /* access modifiers changed from: private */
    public IUnityPlayerLifecycleEvents m_UnityPlayerLifecycleEvents;
    private Uri m_launchUri;

    class a implements SensorEventListener {
        a() {
        }

        public final void onAccuracyChanged(Sensor sensor, int i) {
        }

        public final void onSensorChanged(SensorEvent sensorEvent) {
        }
    }

    enum b {
        ;

        static {
            d = new int[]{1, 2, 3};
        }
    }

    private class c extends PhoneStateListener {
        private c() {
        }

        /* synthetic */ c(UnityPlayer unityPlayer, byte b) {
            this();
        }

        public final void onCallStateChanged(int i, String str) {
            UnityPlayer unityPlayer = UnityPlayer.this;
            boolean z = true;
            if (i != 1) {
                z = false;
            }
            unityPlayer.nativeMuteMasterAudio(z);
        }
    }

    enum d {
        PAUSE,
        RESUME,
        QUIT,
        SURFACE_LOST,
        SURFACE_ACQUIRED,
        FOCUS_LOST,
        FOCUS_GAINED,
        NEXT_FRAME,
        URL_ACTIVATED,
        ORIENTATION_ANGLE_CHANGE
    }

    private class e extends Thread {
        Handler a;
        boolean b;
        boolean c;
        int d;
        int e;
        int f;
        int g;
        int h;

        private e() {
            this.b = false;
            this.c = false;
            this.d = b.b;
            this.e = 0;
            this.h = 5;
        }

        /* synthetic */ e(UnityPlayer unityPlayer, byte b2) {
            this();
        }

        private void a(d dVar) {
            Handler handler = this.a;
            if (handler != null) {
                Message.obtain(handler, UnityPlayer.RUN_STATE_CHANGED_MSG_CODE, dVar).sendToTarget();
            }
        }

        public final void a() {
            a(d.QUIT);
        }

        public final void a(int i2, int i3) {
            this.f = i2;
            this.g = i3;
            a(d.ORIENTATION_ANGLE_CHANGE);
        }

        public final void a(Runnable runnable) {
            if (this.a != null) {
                a(d.PAUSE);
                Message.obtain(this.a, runnable).sendToTarget();
            }
        }

        public final void b() {
            a(d.RESUME);
        }

        public final void b(Runnable runnable) {
            if (this.a != null) {
                a(d.SURFACE_LOST);
                Message.obtain(this.a, runnable).sendToTarget();
            }
        }

        public final void c() {
            a(d.FOCUS_GAINED);
        }

        public final void c(Runnable runnable) {
            Handler handler = this.a;
            if (handler != null) {
                Message.obtain(handler, runnable).sendToTarget();
                a(d.SURFACE_ACQUIRED);
            }
        }

        public final void d() {
            a(d.FOCUS_LOST);
        }

        public final void d(Runnable runnable) {
            Handler handler = this.a;
            if (handler != null) {
                Message.obtain(handler, runnable).sendToTarget();
            }
        }

        public final void e() {
            a(d.URL_ACTIVATED);
        }

        public final void run() {
            setName("UnityMain");
            Looper.prepare();
            this.a = new Handler(new Handler.Callback() {
                private void a() {
                    if (e.this.d == b.c && e.this.c) {
                        UnityPlayer.this.nativeFocusChanged(true);
                        e.this.d = b.a;
                    }
                }

                public final boolean handleMessage(Message message) {
                    if (message.what != UnityPlayer.RUN_STATE_CHANGED_MSG_CODE) {
                        return false;
                    }
                    d dVar = (d) message.obj;
                    if (dVar == d.NEXT_FRAME) {
                        e.this.e--;
                        UnityPlayer.this.executeGLThreadJobs();
                        if (!e.this.b || !e.this.c) {
                            return true;
                        }
                        if (e.this.h >= 0) {
                            if (e.this.h == 0) {
                                if (UnityPlayer.this.getSplashEnabled()) {
                                    UnityPlayer.this.DisableStaticSplashScreen();
                                }
                                if (UnityPlayer.this.mActivity != null && UnityPlayer.this.getAutoReportFullyDrawnEnabled()) {
                                    UnityPlayer.this.mActivity.reportFullyDrawn();
                                }
                            }
                            e.this.h--;
                        }
                        if (!UnityPlayer.this.isFinishing() && !UnityPlayer.this.nativeRender()) {
                            UnityPlayer.this.finish();
                        }
                    } else if (dVar == d.QUIT) {
                        Looper.myLooper().quit();
                    } else if (dVar == d.RESUME) {
                        e.this.b = true;
                    } else if (dVar == d.PAUSE) {
                        e.this.b = false;
                    } else if (dVar == d.SURFACE_LOST) {
                        e.this.c = false;
                    } else {
                        if (dVar == d.SURFACE_ACQUIRED) {
                            e.this.c = true;
                        } else if (dVar == d.FOCUS_LOST) {
                            if (e.this.d == b.a) {
                                UnityPlayer.this.nativeFocusChanged(false);
                            }
                            e.this.d = b.b;
                        } else if (dVar == d.FOCUS_GAINED) {
                            e.this.d = b.c;
                        } else if (dVar == d.URL_ACTIVATED) {
                            UnityPlayer.this.nativeSetLaunchURL(UnityPlayer.this.getLaunchURL());
                        } else if (dVar == d.ORIENTATION_ANGLE_CHANGE) {
                            UnityPlayer.this.nativeOrientationChanged(e.this.f, e.this.g);
                        }
                        a();
                    }
                    if (e.this.b && e.this.e <= 0) {
                        Message.obtain(e.this.a, UnityPlayer.RUN_STATE_CHANGED_MSG_CODE, d.NEXT_FRAME).sendToTarget();
                        e.this.e++;
                    }
                    return true;
                }
            });
            Looper.loop();
        }
    }

    private abstract class f implements Runnable {
        private f() {
        }

        /* synthetic */ f(UnityPlayer unityPlayer, byte b) {
            this();
        }

        public abstract void a();

        public final void run() {
            if (!UnityPlayer.this.isFinishing()) {
                a();
            }
        }
    }

    static {
        new l().a();
    }

    public UnityPlayer(Context context) {
        this(context, (IUnityPlayerLifecycleEvents) null);
    }

    public UnityPlayer(Context context, IUnityPlayerLifecycleEvents iUnityPlayerLifecycleEvents) {
        super(context);
        this.mHandler = new Handler();
        this.mInitialScreenOrientation = -1;
        this.mMainDisplayOverride = false;
        this.mIsFullscreen = true;
        this.mState = new m();
        this.m_Events = new ConcurrentLinkedQueue();
        this.mKillingIsMyBusiness = null;
        this.mOrientationListener = null;
        this.m_MainThread = new e(this, (byte) 0);
        this.m_AddPhoneCallListener = false;
        this.m_PhoneCallListener = new c(this, (byte) 0);
        this.m_ARCoreApi = null;
        this.m_FakeListener = new a();
        this.m_Camera2Wrapper = null;
        this.m_HFPStatus = null;
        this.m_AudioVolumeHandler = null;
        this.m_OrientationLockListener = null;
        this.m_launchUri = null;
        this.m_NetworkConnectivity = null;
        this.m_UnityPlayerLifecycleEvents = null;
        this.mProcessKillRequested = true;
        this.mSoftInputDialog = null;
        this.m_UnityPlayerLifecycleEvents = iUnityPlayerLifecycleEvents == null ? this : iUnityPlayerLifecycleEvents;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            this.mActivity = activity;
            currentActivity = activity;
            this.mInitialScreenOrientation = activity.getRequestedOrientation();
            this.m_launchUri = this.mActivity.getIntent().getData();
        }
        this.mContext = context;
        EarlyEnableFullScreenIfEnabled();
        this.mNaturalOrientation = getNaturalOrientation(getResources().getConfiguration().orientation);
        if (this.mActivity != null && getSplashEnabled()) {
            j jVar = new j(this.mContext, j.a.a()[getSplashMode()]);
            this.m_SplashScreen = jVar;
            addView(jVar);
        }
        if (currentActivity != null) {
            this.m_PersistentUnitySurface = new h(this.mContext);
        }
        preloadJavaPlugins();
        String loadNative = loadNative(getUnityNativeLibraryPath(this.mContext));
        if (!m.c()) {
            f.Log(6, "Your hardware does not support this application.");
            AlertDialog.Builder positiveButton = new AlertDialog.Builder(this.mContext).setTitle("Failure to initialize!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public final void onClick(DialogInterface dialogInterface, int i) {
                    UnityPlayer.this.finish();
                }
            });
            AlertDialog create = positiveButton.setMessage("Your hardware does not support this application." + "\n\n" + loadNative + "\n\n Press OK to quit.").create();
            create.setCancelable(false);
            create.show();
            return;
        }
        initJni(context);
        this.mState.c(true);
        SurfaceView CreateGlView = CreateGlView();
        this.mGlView = CreateGlView;
        CreateGlView.setContentDescription(GetGlViewContentDescription(context));
        addView(this.mGlView);
        bringChildToFront(this.m_SplashScreen);
        this.mQuitting = false;
        hideStatusBar();
        this.m_TelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.m_ClipboardManager = (ClipboardManager) this.mContext.getSystemService("clipboard");
        this.m_Camera2Wrapper = new Camera2Wrapper(this.mContext);
        this.m_HFPStatus = new HFPStatus(this.mContext);
        this.m_MainThread.start();
    }

    private SurfaceView CreateGlView() {
        SurfaceView surfaceView = new SurfaceView(this.mContext);
        surfaceView.setId(this.mContext.getResources().getIdentifier("unitySurfaceView", "id", this.mContext.getPackageName()));
        if (IsWindowTranslucent()) {
            surfaceView.getHolder().setFormat(-3);
            surfaceView.setZOrderOnTop(true);
        } else {
            surfaceView.getHolder().setFormat(-1);
        }
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public final void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                UnityPlayer.this.updateGLDisplay(0, surfaceHolder.getSurface());
                UnityPlayer.this.sendSurfaceChangedEvent();
            }

            public final void surfaceCreated(SurfaceHolder surfaceHolder) {
                UnityPlayer.this.updateGLDisplay(0, surfaceHolder.getSurface());
                if (UnityPlayer.this.m_PersistentUnitySurface != null) {
                    UnityPlayer.this.m_PersistentUnitySurface.a((ViewGroup) UnityPlayer.this);
                }
            }

            public final void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (UnityPlayer.this.m_PersistentUnitySurface != null) {
                    UnityPlayer.this.m_PersistentUnitySurface.a(UnityPlayer.this.mGlView);
                }
                UnityPlayer.this.updateGLDisplay(0, (Surface) null);
            }
        });
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        return surfaceView;
    }

    /* access modifiers changed from: private */
    public void DisableStaticSplashScreen() {
        runOnUiThread(new Runnable() {
            public final void run() {
                UnityPlayer unityPlayer = UnityPlayer.this;
                unityPlayer.removeView(unityPlayer.m_SplashScreen);
                j unused = UnityPlayer.this.m_SplashScreen = null;
            }
        });
    }

    private void EarlyEnableFullScreenIfEnabled() {
        View decorView;
        Activity activity = this.mActivity;
        if (activity != null && activity.getWindow() != null) {
            if ((getLaunchFullscreen() || this.mActivity.getIntent().getBooleanExtra("android.intent.extra.VR_LAUNCH", false)) && (decorView = this.mActivity.getWindow().getDecorView()) != null) {
                decorView.setSystemUiVisibility(7);
            }
        }
    }

    private String GetGlViewContentDescription(Context context) {
        return context.getResources().getString(context.getResources().getIdentifier("game_view_content_description", "string", context.getPackageName()));
    }

    private boolean IsWindowTranslucent() {
        Activity activity = this.mActivity;
        if (activity == null) {
            return false;
        }
        TypedArray obtainStyledAttributes = activity.getTheme().obtainStyledAttributes(new int[]{16842840});
        boolean z = obtainStyledAttributes.getBoolean(0, false);
        obtainStyledAttributes.recycle();
        return z;
    }

    public static void UnitySendMessage(String str, String str2, String str3) {
        if (!m.c()) {
            f.Log(5, "Native libraries not loaded - dropping message for " + str + "." + str2);
            return;
        }
        try {
            nativeUnitySendMessage(str, str2, str3.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException unused) {
        }
    }

    private void checkResumePlayer() {
        Activity activity = this.mActivity;
        if (this.mState.e(activity != null ? MultiWindowSupport.getAllowResizableWindow(activity) : false)) {
            this.mState.d(true);
            queueGLThreadEvent((Runnable) new Runnable() {
                public final void run() {
                    UnityPlayer.this.nativeResume();
                    UnityPlayer.this.runOnUiThread(new Runnable() {
                        public final void run() {
                            if (UnityPlayer.this.m_PersistentUnitySurface != null) {
                                UnityPlayer.this.m_PersistentUnitySurface.b(UnityPlayer.this);
                                UnityPlayer.this.m_PersistentUnitySurface.b();
                            }
                        }
                    });
                }
            });
            this.m_MainThread.b();
        }
    }

    /* access modifiers changed from: private */
    public void finish() {
        Activity activity = this.mActivity;
        if (activity != null && !activity.isFinishing()) {
            this.mActivity.finish();
        }
    }

    private boolean getARCoreEnabled() {
        try {
            return getApplicationInfo().metaData.getBoolean(ARCORE_ENABLE_METADATA_NAME);
        } catch (Exception unused) {
            return false;
        }
    }

    private ApplicationInfo getApplicationInfo() {
        return this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), 128);
    }

    /* access modifiers changed from: private */
    public boolean getAutoReportFullyDrawnEnabled() {
        try {
            return getApplicationInfo().metaData.getBoolean(AUTO_REPORT_FULLY_DRAWN_ENABLE_METADATA_NAME);
        } catch (Exception unused) {
            return false;
        }
    }

    private boolean getLaunchFullscreen() {
        try {
            return getApplicationInfo().metaData.getBoolean(LAUNCH_FULLSCREEN);
        } catch (Exception unused) {
            return false;
        }
    }

    private int getNaturalOrientation(int i) {
        int rotation = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation();
        if ((rotation == 0 || rotation == 2) && i == 2) {
            return 0;
        }
        return ((rotation == 1 || rotation == 3) && i == 1) ? 0 : 1;
    }

    private String getProcessName() {
        int myPid = Process.myPid();
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo next : runningAppProcesses) {
            if (next.pid == myPid) {
                return next.processName;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean getSplashEnabled() {
        try {
            return getApplicationInfo().metaData.getBoolean(SPLASH_ENABLE_METADATA_NAME);
        } catch (Exception unused) {
            return false;
        }
    }

    private static String getUnityNativeLibraryPath(Context context) {
        return context.getApplicationInfo().nativeLibraryDir;
    }

    private void hideStatusBar() {
        Activity activity = this.mActivity;
        if (activity != null) {
            activity.getWindow().setFlags(1024, 1024);
        }
    }

    private final native void initJni(Context context);

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        return logLoadLibMainError(r0, r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0015, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
        java.lang.System.loadLibrary("main");
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0015 A[ExcHandler: SecurityException (r2v7 'e' java.lang.SecurityException A[CUSTOM_DECLARE]), Splitter:B:1:0x0011] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String loadNative(java.lang.String r2) {
        /*
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r2)
            java.lang.String r1 = "/libmain.so"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.System.load(r0)     // Catch:{ UnsatisfiedLinkError -> 0x0017, SecurityException -> 0x0015 }
            goto L_0x001c
        L_0x0015:
            r2 = move-exception
            goto L_0x002f
        L_0x0017:
            java.lang.String r1 = "main"
            java.lang.System.loadLibrary(r1)     // Catch:{ UnsatisfiedLinkError -> 0x0038, SecurityException -> 0x0015 }
        L_0x001c:
            boolean r2 = com.unity3d.player.NativeLoader.load(r2)
            if (r2 == 0) goto L_0x0028
            com.unity3d.player.m.a()
            java.lang.String r2 = ""
            return r2
        L_0x0028:
            java.lang.String r2 = "NativeLoader.load failure, Unity libraries were not loaded."
            r0 = 6
            com.unity3d.player.f.Log(r0, r2)
            return r2
        L_0x002f:
            java.lang.String r2 = r2.toString()
            java.lang.String r2 = logLoadLibMainError(r0, r2)
            return r2
        L_0x0038:
            r2 = move-exception
            java.lang.String r2 = r2.toString()
            java.lang.String r2 = logLoadLibMainError(r0, r2)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.unity3d.player.UnityPlayer.loadNative(java.lang.String):java.lang.String");
    }

    private static String logLoadLibMainError(String str, String str2) {
        String str3 = "Failed to load 'libmain.so'\n\n" + str2;
        f.Log(6, str3);
        return str3;
    }

    private final native void nativeApplicationUnload();

    private final native boolean nativeDone();

    /* access modifiers changed from: private */
    public final native void nativeFocusChanged(boolean z);

    private final native boolean nativeInjectEvent(InputEvent inputEvent);

    /* access modifiers changed from: private */
    public final native boolean nativeIsAutorotationOn();

    /* access modifiers changed from: private */
    public final native void nativeLowMemory();

    /* access modifiers changed from: private */
    public final native void nativeMuteMasterAudio(boolean z);

    /* access modifiers changed from: private */
    public final native void nativeOrientationChanged(int i, int i2);

    /* access modifiers changed from: private */
    public final native boolean nativePause();

    /* access modifiers changed from: private */
    public final native void nativeRecreateGfxState(int i, Surface surface);

    /* access modifiers changed from: private */
    public final native boolean nativeRender();

    /* access modifiers changed from: private */
    public final native void nativeReportKeyboardConfigChanged();

    private final native void nativeRestartActivityIndicator();

    /* access modifiers changed from: private */
    public final native void nativeResume();

    /* access modifiers changed from: private */
    public final native void nativeSendSurfaceChangedEvent();

    /* access modifiers changed from: private */
    public final native void nativeSetInputArea(int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public final native void nativeSetInputSelection(int i, int i2);

    /* access modifiers changed from: private */
    public final native void nativeSetInputString(String str);

    /* access modifiers changed from: private */
    public final native void nativeSetKeyboardIsVisible(boolean z);

    /* access modifiers changed from: private */
    public final native void nativeSetLaunchURL(String str);

    /* access modifiers changed from: private */
    public final native void nativeSoftInputCanceled();

    /* access modifiers changed from: private */
    public final native void nativeSoftInputClosed();

    /* access modifiers changed from: private */
    public final native void nativeSoftInputLostFocus();

    private static native void nativeUnitySendMessage(String str, String str2, byte[] bArr);

    private void pauseUnity() {
        reportSoftInputStr((String) null, 1, true);
        if (this.mState.f()) {
            if (m.c()) {
                final Semaphore semaphore = new Semaphore(0);
                this.m_MainThread.a(isFinishing() ? new Runnable() {
                    public final void run() {
                        UnityPlayer.this.shutdown();
                        semaphore.release();
                    }
                } : new Runnable() {
                    public final void run() {
                        if (UnityPlayer.this.nativePause()) {
                            boolean unused = UnityPlayer.this.mQuitting = true;
                            UnityPlayer.this.shutdown();
                            semaphore.release(2);
                            return;
                        }
                        semaphore.release();
                    }
                });
                try {
                    if (!semaphore.tryAcquire(4, TimeUnit.SECONDS)) {
                        f.Log(5, "Timeout while trying to pause the Unity Engine.");
                    }
                } catch (InterruptedException unused) {
                    f.Log(5, "UI thread got interrupted while trying to pause the Unity Engine.");
                }
                if (semaphore.drainPermits() > 0) {
                    destroy();
                }
            }
            this.mState.d(false);
            this.mState.b(true);
            if (this.m_AddPhoneCallListener) {
                this.m_TelephonyManager.listen(this.m_PhoneCallListener, 0);
            }
        }
    }

    private static void preloadJavaPlugins() {
        try {
            Class.forName("com.unity3d.JavaPluginPreloader");
        } catch (ClassNotFoundException unused) {
        } catch (LinkageError e2) {
            f.Log(6, "Java class preloading failed: " + e2.getMessage());
        }
    }

    private void queueGLThreadEvent(f fVar) {
        if (!isFinishing()) {
            queueGLThreadEvent((Runnable) fVar);
        }
    }

    /* access modifiers changed from: private */
    public void sendSurfaceChangedEvent() {
        if (m.c() && this.mState.e()) {
            this.m_MainThread.d(new Runnable() {
                public final void run() {
                    UnityPlayer.this.nativeSendSurfaceChangedEvent();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void shutdown() {
        this.mProcessKillRequested = nativeDone();
        this.mState.c(false);
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [android.view.ViewParent] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void swapViews(android.view.View r5, android.view.View r6) {
        /*
            r4 = this;
            com.unity3d.player.m r0 = r4.mState
            boolean r0 = r0.d()
            r1 = 0
            if (r0 != 0) goto L_0x000e
            r4.pause()
            r0 = 1
            goto L_0x000f
        L_0x000e:
            r0 = 0
        L_0x000f:
            if (r5 == 0) goto L_0x0030
            android.view.ViewParent r2 = r5.getParent()
            boolean r3 = r2 instanceof com.unity3d.player.UnityPlayer
            if (r3 == 0) goto L_0x001e
            r3 = r2
            com.unity3d.player.UnityPlayer r3 = (com.unity3d.player.UnityPlayer) r3
            if (r3 == r4) goto L_0x0030
        L_0x001e:
            boolean r3 = r2 instanceof android.view.ViewGroup
            if (r3 == 0) goto L_0x0027
            android.view.ViewGroup r2 = (android.view.ViewGroup) r2
            r2.removeView(r5)
        L_0x0027:
            r4.addView(r5)
            r4.bringChildToFront(r5)
            r5.setVisibility(r1)
        L_0x0030:
            if (r6 == 0) goto L_0x0040
            android.view.ViewParent r5 = r6.getParent()
            if (r5 != r4) goto L_0x0040
            r5 = 8
            r6.setVisibility(r5)
            r4.removeView(r6)
        L_0x0040:
            if (r0 == 0) goto L_0x0045
            r4.resume()
        L_0x0045:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.unity3d.player.UnityPlayer.swapViews(android.view.View, android.view.View):void");
    }

    private static void unloadNative() {
        if (m.c()) {
            if (NativeLoader.unload()) {
                m.b();
                return;
            }
            throw new UnsatisfiedLinkError("Unable to unload libraries from libmain.so");
        }
    }

    private boolean updateDisplayInternal(final int i, final Surface surface) {
        if (!m.c() || !this.mState.e()) {
            return false;
        }
        final Semaphore semaphore = new Semaphore(0);
        AnonymousClass21 r1 = new Runnable() {
            public final void run() {
                UnityPlayer.this.nativeRecreateGfxState(i, surface);
                semaphore.release();
            }
        };
        if (i == 0) {
            e eVar = this.m_MainThread;
            if (surface == null) {
                eVar.b(r1);
            } else {
                eVar.c(r1);
            }
        } else {
            r1.run();
        }
        if (surface != null || i != 0) {
            return true;
        }
        try {
            if (semaphore.tryAcquire(4, TimeUnit.SECONDS)) {
                return true;
            }
            f.Log(5, "Timeout while trying detaching primary window.");
            return true;
        } catch (InterruptedException unused) {
            f.Log(5, "UI thread got interrupted while trying to detach the primary window from the Unity Engine.");
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void updateGLDisplay(int i, Surface surface) {
        if (!this.mMainDisplayOverride) {
            updateDisplayInternal(i, surface);
        }
    }

    /* access modifiers changed from: protected */
    public void addPhoneCallListener() {
        this.m_AddPhoneCallListener = true;
        this.m_TelephonyManager.listen(this.m_PhoneCallListener, 32);
    }

    public boolean addViewToPlayer(View view, boolean z) {
        swapViews(view, z ? this.mGlView : null);
        boolean z2 = true;
        boolean z3 = view.getParent() == this;
        boolean z4 = z && this.mGlView.getParent() == null;
        boolean z5 = this.mGlView.getParent() == this;
        if (!z3 || (!z4 && !z5)) {
            z2 = false;
        }
        if (!z2) {
            if (!z3) {
                f.Log(6, "addViewToPlayer: Failure adding view to hierarchy");
            }
            if (!z4 && !z5) {
                f.Log(6, "addViewToPlayer: Failure removing old view from hierarchy");
            }
        }
        return z2;
    }

    public void configurationChanged(Configuration configuration) {
        SurfaceView surfaceView = this.mGlView;
        if (surfaceView instanceof SurfaceView) {
            surfaceView.getHolder().setSizeFromLayout();
        }
        o oVar = this.mVideoPlayerProxy;
        if (oVar != null) {
            oVar.c();
        }
    }

    public void destroy() {
        h hVar = this.m_PersistentUnitySurface;
        if (hVar != null) {
            hVar.a();
            this.m_PersistentUnitySurface = null;
        }
        Camera2Wrapper camera2Wrapper = this.m_Camera2Wrapper;
        if (camera2Wrapper != null) {
            camera2Wrapper.a();
            this.m_Camera2Wrapper = null;
        }
        HFPStatus hFPStatus = this.m_HFPStatus;
        if (hFPStatus != null) {
            hFPStatus.a();
            this.m_HFPStatus = null;
        }
        NetworkConnectivity networkConnectivity = this.m_NetworkConnectivity;
        if (networkConnectivity != null) {
            networkConnectivity.b();
            this.m_NetworkConnectivity = null;
        }
        this.mQuitting = true;
        if (!this.mState.d()) {
            pause();
        }
        this.m_MainThread.a();
        try {
            this.m_MainThread.join(4000);
        } catch (InterruptedException unused) {
            this.m_MainThread.interrupt();
        }
        BroadcastReceiver broadcastReceiver = this.mKillingIsMyBusiness;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
        this.mKillingIsMyBusiness = null;
        if (m.c()) {
            removeAllViews();
        }
        if (this.mProcessKillRequested) {
            this.m_UnityPlayerLifecycleEvents.onUnityPlayerQuitted();
            kill();
        }
        unloadNative();
    }

    /* access modifiers changed from: protected */
    public void disableLogger() {
        f.a = true;
    }

    public boolean displayChanged(int i, Surface surface) {
        if (i == 0) {
            this.mMainDisplayOverride = surface != null;
            runOnUiThread(new Runnable() {
                public final void run() {
                    if (UnityPlayer.this.mMainDisplayOverride) {
                        UnityPlayer unityPlayer = UnityPlayer.this;
                        unityPlayer.removeView(unityPlayer.mGlView);
                        return;
                    }
                    UnityPlayer unityPlayer2 = UnityPlayer.this;
                    unityPlayer2.addView(unityPlayer2.mGlView);
                }
            });
        }
        return updateDisplayInternal(i, surface);
    }

    /* access modifiers changed from: protected */
    public void executeGLThreadJobs() {
        while (true) {
            Runnable runnable = (Runnable) this.m_Events.poll();
            if (runnable != null) {
                runnable.run();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getClipboardText() {
        ClipData primaryClip = this.m_ClipboardManager.getPrimaryClip();
        return primaryClip != null ? primaryClip.getItemAt(0).coerceToText(this.mContext).toString() : "";
    }

    /* access modifiers changed from: protected */
    public String getKeyboardLayout() {
        i iVar = this.mSoftInputDialog;
        if (iVar == null) {
            return null;
        }
        return iVar.a();
    }

    /* access modifiers changed from: protected */
    public String getLaunchURL() {
        Uri uri = this.m_launchUri;
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int getNetworkConnectivity() {
        if (!PlatformSupport.NOUGAT_SUPPORT) {
            return 0;
        }
        if (this.m_NetworkConnectivity == null) {
            this.m_NetworkConnectivity = new NetworkConnectivity(this.mContext);
        }
        return this.m_NetworkConnectivity.a();
    }

    public String getNetworkProxySettings(String str) {
        String str2;
        String str3;
        if (str.startsWith("http:")) {
            str2 = "http.proxyHost";
            str3 = "http.proxyPort";
        } else {
            if (str.startsWith("https:")) {
                str2 = "https.proxyHost";
                str3 = "https.proxyPort";
            }
            return null;
        }
        String property = System.getProperties().getProperty(str2);
        if (property != null && !"".equals(property)) {
            StringBuilder sb = new StringBuilder(property);
            String property2 = System.getProperties().getProperty(str3);
            if (property2 != null && !"".equals(property2)) {
                sb.append(":");
                sb.append(property2);
            }
            String property3 = System.getProperties().getProperty("http.nonProxyHosts");
            if (property3 != null && !"".equals(property3)) {
                sb.append(10);
                sb.append(property3);
            }
            return sb.toString();
        }
        return null;
    }

    public Bundle getSettings() {
        return Bundle.EMPTY;
    }

    /* access modifiers changed from: protected */
    public int getSplashMode() {
        try {
            return getApplicationInfo().metaData.getInt(SPLASH_MODE_METADATA_NAME);
        } catch (Exception unused) {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int getUaaLLaunchProcessType() {
        String processName = getProcessName();
        return (processName == null || processName.equals(this.mContext.getPackageName())) ? 0 : 1;
    }

    public View getView() {
        return this;
    }

    /* access modifiers changed from: protected */
    public void hideSoftInput() {
        postOnUiThread(new Runnable() {
            public final void run() {
                UnityPlayer.this.reportSoftInputArea(new Rect());
                UnityPlayer.this.reportSoftInputIsVisible(false);
                if (UnityPlayer.this.mSoftInputDialog != null) {
                    UnityPlayer.this.mSoftInputDialog.dismiss();
                    UnityPlayer.this.mSoftInputDialog = null;
                    UnityPlayer.this.nativeReportKeyboardConfigChanged();
                }
            }
        });
    }

    public void init(int i, boolean z) {
    }

    /* access modifiers changed from: protected */
    public boolean initializeGoogleAr() {
        if (this.m_ARCoreApi != null || this.mActivity == null || !getARCoreEnabled()) {
            return false;
        }
        GoogleARCoreApi googleARCoreApi = new GoogleARCoreApi();
        this.m_ARCoreApi = googleARCoreApi;
        googleARCoreApi.initializeARCore(this.mActivity);
        if (this.mState.d()) {
            return false;
        }
        this.m_ARCoreApi.resumeARCore();
        return false;
    }

    public boolean injectEvent(InputEvent inputEvent) {
        if (!m.c()) {
            return false;
        }
        return nativeInjectEvent(inputEvent);
    }

    /* access modifiers changed from: protected */
    public boolean isFinishing() {
        if (this.mQuitting) {
            return true;
        }
        Activity activity = this.mActivity;
        if (activity != null) {
            this.mQuitting = activity.isFinishing();
        }
        return this.mQuitting;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0005, code lost:
        r0 = r0.getCallingPackage();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isUaaLUseCase() {
        /*
            r3 = this;
            android.app.Activity r0 = r3.mActivity
            r1 = 0
            if (r0 == 0) goto L_0x0019
            java.lang.String r0 = r0.getCallingPackage()
            if (r0 == 0) goto L_0x0019
            android.content.Context r2 = r3.mContext
            java.lang.String r2 = r2.getPackageName()
            boolean r0 = r0.equals(r2)
            if (r0 == 0) goto L_0x0019
            r0 = 1
            return r0
        L_0x0019:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.unity3d.player.UnityPlayer.isUaaLUseCase():boolean");
    }

    /* access modifiers changed from: protected */
    public void kill() {
        Process.killProcess(Process.myPid());
    }

    /* access modifiers changed from: protected */
    public boolean loadLibrary(String str) {
        try {
            System.loadLibrary(str);
            return true;
        } catch (Exception | UnsatisfiedLinkError unused) {
            return false;
        }
    }

    public void lowMemory() {
        if (m.c()) {
            queueGLThreadEvent((Runnable) new Runnable() {
                public final void run() {
                    UnityPlayer.this.nativeLowMemory();
                }
            });
        }
    }

    public void newIntent(Intent intent) {
        this.m_launchUri = intent.getData();
        this.m_MainThread.e();
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        return injectEvent(motionEvent);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return injectEvent(keyEvent);
    }

    public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
        return injectEvent(keyEvent);
    }

    public boolean onKeyMultiple(int i, int i2, KeyEvent keyEvent) {
        return injectEvent(keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return injectEvent(keyEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return injectEvent(motionEvent);
    }

    public void onUnityPlayerQuitted() {
    }

    public void onUnityPlayerUnloaded() {
    }

    public void pause() {
        GoogleARCoreApi googleARCoreApi = this.m_ARCoreApi;
        if (googleARCoreApi != null) {
            googleARCoreApi.pauseARCore();
        }
        o oVar = this.mVideoPlayerProxy;
        if (oVar != null) {
            oVar.a();
        }
        AudioVolumeHandler audioVolumeHandler = this.m_AudioVolumeHandler;
        if (audioVolumeHandler != null) {
            audioVolumeHandler.a();
            this.m_AudioVolumeHandler = null;
        }
        OrientationLockListener orientationLockListener = this.m_OrientationLockListener;
        if (orientationLockListener != null) {
            orientationLockListener.a();
            this.m_OrientationLockListener = null;
        }
        pauseUnity();
    }

    /* access modifiers changed from: protected */
    public void pauseJavaAndCallUnloadCallback() {
        runOnUiThread(new Runnable() {
            public final void run() {
                UnityPlayer.this.pause();
                UnityPlayer.this.windowFocusChanged(false);
                UnityPlayer.this.m_UnityPlayerLifecycleEvents.onUnityPlayerUnloaded();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void postOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /* access modifiers changed from: package-private */
    public void queueGLThreadEvent(Runnable runnable) {
        if (m.c()) {
            if (Thread.currentThread() == this.m_MainThread) {
                runnable.run();
            } else {
                this.m_Events.add(runnable);
            }
        }
    }

    public void quit() {
        destroy();
    }

    public void removeViewFromPlayer(View view) {
        swapViews(this.mGlView, view);
        boolean z = true;
        boolean z2 = view.getParent() == null;
        boolean z3 = this.mGlView.getParent() == this;
        if (!z2 || !z3) {
            z = false;
        }
        if (!z) {
            if (!z2) {
                f.Log(6, "removeViewFromPlayer: Failure removing view from hierarchy");
            }
            if (!z3) {
                f.Log(6, "removeVireFromPlayer: Failure agging old view to hierarchy");
            }
        }
    }

    public void reportError(String str, String str2) {
        f.Log(6, str + ": " + str2);
    }

    /* access modifiers changed from: protected */
    public void reportSoftInputArea(final Rect rect) {
        queueGLThreadEvent((f) new f() {
            public final void a() {
                UnityPlayer.this.nativeSetInputArea(rect.left, rect.top, rect.right, rect.bottom);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void reportSoftInputIsVisible(final boolean z) {
        queueGLThreadEvent((f) new f() {
            public final void a() {
                UnityPlayer.this.nativeSetKeyboardIsVisible(z);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void reportSoftInputSelection(final int i, final int i2) {
        queueGLThreadEvent((f) new f() {
            public final void a() {
                UnityPlayer.this.nativeSetInputSelection(i, i2);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void reportSoftInputStr(final String str, final int i, final boolean z) {
        if (i == 1) {
            hideSoftInput();
        }
        queueGLThreadEvent((f) new f() {
            public final void a() {
                if (z) {
                    UnityPlayer.this.nativeSoftInputCanceled();
                } else {
                    String str = str;
                    if (str != null) {
                        UnityPlayer.this.nativeSetInputString(str);
                    }
                }
                if (i == 1) {
                    UnityPlayer.this.nativeSoftInputClosed();
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void requestUserAuthorization(String str) {
        if (str != null && !str.isEmpty() && this.mActivity != null) {
            UnityPermissions.ModalWaitForPermissionResponse modalWaitForPermissionResponse = new UnityPermissions.ModalWaitForPermissionResponse();
            UnityPermissions.requestUserPermissions(this.mActivity, new String[]{str}, modalWaitForPermissionResponse);
            modalWaitForPermissionResponse.waitForResponse();
        }
    }

    public void resume() {
        GoogleARCoreApi googleARCoreApi = this.m_ARCoreApi;
        if (googleARCoreApi != null) {
            googleARCoreApi.resumeARCore();
        }
        this.mState.b(false);
        o oVar = this.mVideoPlayerProxy;
        if (oVar != null) {
            oVar.b();
        }
        checkResumePlayer();
        if (m.c()) {
            nativeRestartActivityIndicator();
        }
        if (this.m_AudioVolumeHandler == null) {
            this.m_AudioVolumeHandler = new AudioVolumeHandler(this.mContext);
        }
        if (this.m_OrientationLockListener == null && m.c()) {
            this.m_OrientationLockListener = new OrientationLockListener(this.mContext);
        }
    }

    /* access modifiers changed from: package-private */
    public void runOnAnonymousThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    /* access modifiers changed from: package-private */
    public void runOnUiThread(Runnable runnable) {
        Activity activity = this.mActivity;
        if (activity != null) {
            activity.runOnUiThread(runnable);
        } else if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            this.mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    /* access modifiers changed from: protected */
    public void setCharacterLimit(final int i) {
        runOnUiThread(new Runnable() {
            public final void run() {
                if (UnityPlayer.this.mSoftInputDialog != null) {
                    UnityPlayer.this.mSoftInputDialog.a(i);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setClipboardText(String str) {
        this.m_ClipboardManager.setPrimaryClip(ClipData.newPlainText("Text", str));
    }

    /* access modifiers changed from: protected */
    public void setHideInputField(final boolean z) {
        runOnUiThread(new Runnable() {
            public final void run() {
                if (UnityPlayer.this.mSoftInputDialog != null) {
                    UnityPlayer.this.mSoftInputDialog.a(z);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setSelection(final int i, final int i2) {
        runOnUiThread(new Runnable() {
            public final void run() {
                if (UnityPlayer.this.mSoftInputDialog != null) {
                    UnityPlayer.this.mSoftInputDialog.a(i, i2);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setSoftInputStr(final String str) {
        runOnUiThread(new Runnable() {
            public final void run() {
                if (UnityPlayer.this.mSoftInputDialog != null && str != null) {
                    UnityPlayer.this.mSoftInputDialog.a(str);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showSoftInput(String str, int i, boolean z, boolean z2, boolean z3, boolean z4, String str2, int i2, boolean z5, boolean z6) {
        final String str3 = str;
        final int i3 = i;
        final boolean z7 = z;
        final boolean z8 = z2;
        final boolean z9 = z3;
        final boolean z10 = z4;
        final String str4 = str2;
        final int i4 = i2;
        final boolean z11 = z5;
        final boolean z12 = z6;
        postOnUiThread(new Runnable() {
            public final void run() {
                UnityPlayer.this.mSoftInputDialog = new i(UnityPlayer.this.mContext, this, str3, i3, z7, z8, z9, str4, i4, z11, z12);
                UnityPlayer.this.mSoftInputDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public final void onCancel(DialogInterface dialogInterface) {
                        UnityPlayer.this.nativeSoftInputLostFocus();
                        UnityPlayer.this.reportSoftInputStr((String) null, 1, false);
                    }
                });
                UnityPlayer.this.mSoftInputDialog.show();
                UnityPlayer.this.nativeReportKeyboardConfigChanged();
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean showVideoPlayer(String str, int i, int i2, int i3, boolean z, int i4, int i5) {
        if (this.mVideoPlayerProxy == null) {
            this.mVideoPlayerProxy = new o(this);
        }
        boolean a2 = this.mVideoPlayerProxy.a(this.mContext, str, i, i2, i3, z, (long) i4, (long) i5, new o.a() {
            public final void a() {
                o unused = UnityPlayer.this.mVideoPlayerProxy = null;
            }
        });
        if (a2) {
            runOnUiThread(new Runnable() {
                public final void run() {
                    if (UnityPlayer.this.nativeIsAutorotationOn() && UnityPlayer.this.mActivity != null) {
                        ((Activity) UnityPlayer.this.mContext).setRequestedOrientation(UnityPlayer.this.mInitialScreenOrientation);
                    }
                }
            });
        }
        return a2;
    }

    /* access modifiers changed from: protected */
    public boolean skipPermissionsDialog() {
        Activity activity = this.mActivity;
        if (activity != null) {
            return UnityPermissions.skipPermissionsDialog(activity);
        }
        return false;
    }

    public boolean startOrientationListener(int i) {
        String str;
        if (this.mOrientationListener != null) {
            str = "Orientation Listener already started.";
        } else {
            AnonymousClass17 r0 = new OrientationEventListener(this.mContext, i) {
                public final void onOrientationChanged(int i) {
                    UnityPlayer.this.m_MainThread.a(UnityPlayer.this.mNaturalOrientation, i);
                }
            };
            this.mOrientationListener = r0;
            if (r0.canDetectOrientation()) {
                this.mOrientationListener.enable();
                return true;
            }
            str = "Orientation Listener cannot detect orientation.";
        }
        f.Log(5, str);
        return false;
    }

    public boolean stopOrientationListener() {
        OrientationEventListener orientationEventListener = this.mOrientationListener;
        if (orientationEventListener == null) {
            f.Log(5, "Orientation Listener was not started.");
            return false;
        }
        orientationEventListener.disable();
        this.mOrientationListener = null;
        return true;
    }

    /* access modifiers changed from: protected */
    public void toggleGyroscopeSensor(boolean z) {
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        Sensor defaultSensor = sensorManager.getDefaultSensor(11);
        if (z) {
            sensorManager.registerListener(this.m_FakeListener, defaultSensor, 1);
        } else {
            sensorManager.unregisterListener(this.m_FakeListener);
        }
    }

    public void unload() {
        nativeApplicationUnload();
    }

    public void windowFocusChanged(boolean z) {
        this.mState.a(z);
        if (this.mState.e()) {
            i iVar = this.mSoftInputDialog;
            if (iVar == null || iVar.a) {
                if (z) {
                    this.m_MainThread.c();
                } else {
                    this.m_MainThread.d();
                }
                checkResumePlayer();
            }
        }
    }
}
