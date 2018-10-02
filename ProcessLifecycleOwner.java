package android.arch.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle.Event;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public class ProcessLifecycleOwner implements LifecycleOwner {
    @VisibleForTesting
    static final long TIMEOUT_MS = 700;
    private static final ProcessLifecycleOwner sInstance = new ProcessLifecycleOwner();
    private Runnable mDelayedPauseRunnable = new C00091();
    private Handler mHandler;
    private ActivityInitializationListener mInitializationListener = new C06552();
    private boolean mPauseSent = true;
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);
    private int mResumedCounter = 0;
    private int mStartedCounter = 0;
    private boolean mStopSent = true;

    /* renamed from: android.arch.lifecycle.ProcessLifecycleOwner$1 */
    class C00091 implements Runnable {
        C00091() {
        }

        public void run() {
            ProcessLifecycleOwner.this.dispatchPauseIfNeeded();
            ProcessLifecycleOwner.this.dispatchStopIfNeeded();
        }
    }

    /* renamed from: android.arch.lifecycle.ProcessLifecycleOwner$2 */
    class C06552 implements ActivityInitializationListener {
        public void onCreate() {
        }

        C06552() {
        }

        public void onStart() {
            ProcessLifecycleOwner.this.activityStarted();
        }

        public void onResume() {
            ProcessLifecycleOwner.this.activityResumed();
        }
    }

    /* renamed from: android.arch.lifecycle.ProcessLifecycleOwner$3 */
    class C06563 extends EmptyActivityLifecycleCallbacks {
        C06563() {
        }

        public void onActivityCreated(Activity activity, Bundle bundle) {
            ReportFragment.get(activity).setProcessListener(ProcessLifecycleOwner.this.mInitializationListener);
        }

        public void onActivityPaused(Activity activity) {
            ProcessLifecycleOwner.this.activityPaused();
        }

        public void onActivityStopped(Activity activity) {
            ProcessLifecycleOwner.this.activityStopped();
        }
    }

    public static LifecycleOwner get() {
        return sInstance;
    }

    static void init(Context context) {
        sInstance.attach(context);
    }

    void activityStarted() {
        this.mStartedCounter++;
        if (this.mStartedCounter == 1 && this.mStopSent) {
            this.mRegistry.handleLifecycleEvent(Event.ON_START);
            this.mStopSent = false;
        }
    }

    void activityResumed() {
        this.mResumedCounter++;
        if (this.mResumedCounter != 1) {
            return;
        }
        if (this.mPauseSent) {
            this.mRegistry.handleLifecycleEvent(Event.ON_RESUME);
            this.mPauseSent = false;
            return;
        }
        this.mHandler.removeCallbacks(this.mDelayedPauseRunnable);
    }

    void activityPaused() {
        this.mResumedCounter--;
        if (this.mResumedCounter == 0) {
            this.mHandler.postDelayed(this.mDelayedPauseRunnable, TIMEOUT_MS);
        }
    }

    void activityStopped() {
        this.mStartedCounter--;
        dispatchStopIfNeeded();
    }

    private void dispatchPauseIfNeeded() {
        if (this.mResumedCounter == 0) {
            this.mPauseSent = true;
            this.mRegistry.handleLifecycleEvent(Event.ON_PAUSE);
        }
    }

    private void dispatchStopIfNeeded() {
        if (this.mStartedCounter == 0 && this.mPauseSent) {
            this.mRegistry.handleLifecycleEvent(Event.ON_STOP);
            this.mStopSent = true;
        }
    }

    private ProcessLifecycleOwner() {
    }

    void attach(Context context) {
        this.mHandler = new Handler();
        this.mRegistry.handleLifecycleEvent(Event.ON_CREATE);
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new C06563());
    }

    @NonNull
    public Lifecycle getLifecycle() {
        return this.mRegistry;
    }
}
