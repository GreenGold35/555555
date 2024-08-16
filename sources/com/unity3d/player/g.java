package com.unity3d.player;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.unity3d.player.UnityPermissions;

public final class g extends Fragment {
    private final IPermissionRequestCallbacks a;
    private final Activity b;
    private final Looper c;

    class a implements Runnable {
        private IPermissionRequestCallbacks b;
        private String c;
        private int d;
        private boolean e;

        a(IPermissionRequestCallbacks iPermissionRequestCallbacks, String str, int i, boolean z) {
            this.b = iPermissionRequestCallbacks;
            this.c = str;
            this.d = i;
            this.e = z;
        }

        public final void run() {
            int i = this.d;
            if (i == -1) {
                if (Build.VERSION.SDK_INT >= 30 || this.e) {
                    this.b.onPermissionDenied(this.c);
                } else {
                    this.b.onPermissionDeniedAndDontAskAgain(this.c);
                }
            } else if (i == 0) {
                this.b.onPermissionGranted(this.c);
            }
        }
    }

    public g() {
        this.a = null;
        this.b = null;
        this.c = null;
    }

    public g(Activity activity, IPermissionRequestCallbacks iPermissionRequestCallbacks) {
        this.a = iPermissionRequestCallbacks;
        this.b = activity;
        this.c = Looper.myLooper();
    }

    /* access modifiers changed from: private */
    public void a(String[] strArr) {
        for (String onPermissionDenied : strArr) {
            this.a.onPermissionDenied(onPermissionDenied);
        }
    }

    public final void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestPermissions(getArguments().getStringArray("PermissionNames"), 96489);
    }

    public final void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (i == 96489) {
            if (strArr.length != 0) {
                int i2 = 0;
                while (i2 < strArr.length && i2 < iArr.length) {
                    IPermissionRequestCallbacks iPermissionRequestCallbacks = this.a;
                    if (!(iPermissionRequestCallbacks == null || this.b == null || this.c == null)) {
                        if (iPermissionRequestCallbacks instanceof UnityPermissions.ModalWaitForPermissionResponse) {
                            iPermissionRequestCallbacks.onPermissionGranted(strArr[i2]);
                        } else {
                            String str = strArr[i2] == null ? "<null>" : strArr[i2];
                            new Handler(this.c).post(new a(this.a, str, iArr[i2], this.b.shouldShowRequestPermissionRationale(str)));
                        }
                    }
                    i2++;
                }
            } else if (!(this.a == null || this.b == null || this.c == null)) {
                final String[] stringArray = getArguments().getStringArray("PermissionNames");
                if (this.a instanceof UnityPermissions.ModalWaitForPermissionResponse) {
                    a(stringArray);
                } else {
                    new Handler(this.c).post(new Runnable() {
                        public final void run() {
                            g.this.a(stringArray);
                        }
                    });
                }
            }
            FragmentTransaction beginTransaction = getActivity().getFragmentManager().beginTransaction();
            beginTransaction.remove(this);
            beginTransaction.commit();
        }
    }
}
