package android.arch.lifecycle;

import android.arch.core.executor.ArchTaskExecutor;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;

@RestrictTo({Scope.LIBRARY_GROUP})
public abstract class ComputableLiveData<T> {
    private AtomicBoolean mComputing = new AtomicBoolean(false);
    private AtomicBoolean mInvalid = new AtomicBoolean(true);
    @VisibleForTesting
    final Runnable mInvalidationRunnable = new C00053();
    private final LiveData<T> mLiveData = new C06511();
    @VisibleForTesting
    final Runnable mRefreshRunnable = new C00042();

    /* renamed from: android.arch.lifecycle.ComputableLiveData$2 */
    class C00042 implements Runnable {
        C00042() {
        }

        @WorkerThread
        public void run() {
            do {
                boolean z;
                if (ComputableLiveData.this.mComputing.compareAndSet(false, true)) {
                    Object obj = null;
                    z = false;
                    while (ComputableLiveData.this.mInvalid.compareAndSet(true, false)) {
                        try {
                            obj = ComputableLiveData.this.compute();
                            z = true;
                        } catch (Throwable th) {
                            ComputableLiveData.this.mComputing.set(false);
                        }
                    }
                    if (z) {
                        ComputableLiveData.this.mLiveData.postValue(obj);
                    }
                    ComputableLiveData.this.mComputing.set(false);
                } else {
                    z = false;
                }
                if (!z) {
                    return;
                }
            } while (ComputableLiveData.this.mInvalid.get());
        }
    }

    /* renamed from: android.arch.lifecycle.ComputableLiveData$3 */
    class C00053 implements Runnable {
        C00053() {
        }

        @MainThread
        public void run() {
            boolean hasActiveObservers = ComputableLiveData.this.mLiveData.hasActiveObservers();
            if (ComputableLiveData.this.mInvalid.compareAndSet(false, true) && hasActiveObservers) {
                ArchTaskExecutor.getInstance().executeOnDiskIO(ComputableLiveData.this.mRefreshRunnable);
            }
        }
    }

    /* renamed from: android.arch.lifecycle.ComputableLiveData$1 */
    class C06511 extends LiveData<T> {
        C06511() {
        }

        protected void onActive() {
            ArchTaskExecutor.getInstance().executeOnDiskIO(ComputableLiveData.this.mRefreshRunnable);
        }
    }

    @WorkerThread
    protected abstract T compute();

    @NonNull
    public LiveData<T> getLiveData() {
        return this.mLiveData;
    }

    public void invalidate() {
        ArchTaskExecutor.getInstance().executeOnMainThread(this.mInvalidationRunnable);
    }
}
