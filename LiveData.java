package android.arch.lifecycle;

import android.arch.core.executor.ArchTaskExecutor;
import android.arch.core.internal.SafeIterableMap;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.Lifecycle.State;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Iterator;
import java.util.Map.Entry;

public abstract class LiveData<T> {
    private static final LifecycleOwner ALWAYS_ON = new C06541();
    private static final Object NOT_SET = new Object();
    static final int START_VERSION = -1;
    private int mActiveCount = 0;
    private volatile Object mData = NOT_SET;
    private final Object mDataLock = new Object();
    private boolean mDispatchInvalidated;
    private boolean mDispatchingValue;
    private SafeIterableMap<Observer<T>, LifecycleBoundObserver> mObservers = new SafeIterableMap();
    private volatile Object mPendingData = NOT_SET;
    private final Runnable mPostValueRunnable = new C00082();
    private int mVersion = -1;

    /* renamed from: android.arch.lifecycle.LiveData$2 */
    class C00082 implements Runnable {
        C00082() {
        }

        public void run() {
            Object access$100;
            synchronized (LiveData.this.mDataLock) {
                access$100 = LiveData.this.mPendingData;
                LiveData.this.mPendingData = LiveData.NOT_SET;
            }
            LiveData.this.setValue(access$100);
        }
    }

    /* renamed from: android.arch.lifecycle.LiveData$1 */
    static class C06541 implements LifecycleOwner {
        private LifecycleRegistry mRegistry = init();

        C06541() {
        }

        private LifecycleRegistry init() {
            LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
            lifecycleRegistry.handleLifecycleEvent(Event.ON_CREATE);
            lifecycleRegistry.handleLifecycleEvent(Event.ON_START);
            lifecycleRegistry.handleLifecycleEvent(Event.ON_RESUME);
            return lifecycleRegistry;
        }

        public Lifecycle getLifecycle() {
            return this.mRegistry;
        }
    }

    class LifecycleBoundObserver implements GenericLifecycleObserver {
        public boolean active;
        public int lastVersion = -1;
        public final Observer<T> observer;
        public final LifecycleOwner owner;

        LifecycleBoundObserver(LifecycleOwner lifecycleOwner, Observer<T> observer) {
            this.owner = lifecycleOwner;
            this.observer = observer;
        }

        public void onStateChanged(LifecycleOwner lifecycleOwner, Event event) {
            if (this.owner.getLifecycle().getCurrentState() == State.DESTROYED) {
                LiveData.this.removeObserver(this.observer);
            } else {
                activeStateChanged(LiveData.isActiveState(this.owner.getLifecycle().getCurrentState()));
            }
        }

        void activeStateChanged(boolean z) {
            if (z != this.active) {
                this.active = z;
                int i = 1;
                z = !LiveData.this.mActiveCount;
                LiveData liveData = LiveData.this;
                int access$300 = liveData.mActiveCount;
                if (!this.active) {
                    i = -1;
                }
                liveData.mActiveCount = access$300 + i;
                if (z && this.active) {
                    LiveData.this.onActive();
                }
                if (!(LiveData.this.mActiveCount || this.active)) {
                    LiveData.this.onInactive();
                }
                if (this.active) {
                    LiveData.this.dispatchingValue(this);
                }
            }
        }
    }

    protected void onActive() {
    }

    protected void onInactive() {
    }

    private void considerNotify(LifecycleBoundObserver lifecycleBoundObserver) {
        if (!lifecycleBoundObserver.active) {
            return;
        }
        if (!isActiveState(lifecycleBoundObserver.owner.getLifecycle().getCurrentState())) {
            lifecycleBoundObserver.activeStateChanged(false);
        } else if (lifecycleBoundObserver.lastVersion < this.mVersion) {
            lifecycleBoundObserver.lastVersion = this.mVersion;
            lifecycleBoundObserver.observer.onChanged(this.mData);
        }
    }

    private void dispatchingValue(@Nullable LifecycleBoundObserver lifecycleBoundObserver) {
        if (this.mDispatchingValue) {
            this.mDispatchInvalidated = true;
            return;
        }
        this.mDispatchingValue = true;
        do {
            this.mDispatchInvalidated = false;
            if (lifecycleBoundObserver == null) {
                Iterator iteratorWithAdditions = this.mObservers.iteratorWithAdditions();
                while (iteratorWithAdditions.hasNext()) {
                    considerNotify((LifecycleBoundObserver) ((Entry) iteratorWithAdditions.next()).getValue());
                    if (this.mDispatchInvalidated) {
                        break;
                    }
                }
            }
            considerNotify(lifecycleBoundObserver);
            lifecycleBoundObserver = null;
        } while (this.mDispatchInvalidated);
        this.mDispatchingValue = false;
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<T> observer) {
        if (lifecycleOwner.getLifecycle().getCurrentState() != State.DESTROYED) {
            LifecycleObserver lifecycleBoundObserver = new LifecycleBoundObserver(lifecycleOwner, observer);
            LifecycleBoundObserver lifecycleBoundObserver2 = (LifecycleBoundObserver) this.mObservers.putIfAbsent(observer, lifecycleBoundObserver);
            if (lifecycleBoundObserver2 != null && lifecycleBoundObserver2.owner != lifecycleBoundObserver.owner) {
                throw new IllegalArgumentException("Cannot add the same observer with different lifecycles");
            } else if (lifecycleBoundObserver2 == null) {
                lifecycleOwner.getLifecycle().addObserver(lifecycleBoundObserver);
            }
        }
    }

    @MainThread
    public void observeForever(@NonNull Observer<T> observer) {
        observe(ALWAYS_ON, observer);
    }

    @MainThread
    public void removeObserver(@NonNull Observer<T> observer) {
        assertMainThread("removeObserver");
        LifecycleBoundObserver lifecycleBoundObserver = (LifecycleBoundObserver) this.mObservers.remove(observer);
        if (lifecycleBoundObserver != null) {
            lifecycleBoundObserver.owner.getLifecycle().removeObserver(lifecycleBoundObserver);
            lifecycleBoundObserver.activeStateChanged(false);
        }
    }

    @MainThread
    public void removeObservers(@NonNull LifecycleOwner lifecycleOwner) {
        assertMainThread("removeObservers");
        Iterator it = this.mObservers.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            if (((LifecycleBoundObserver) entry.getValue()).owner == lifecycleOwner) {
                removeObserver((Observer) entry.getKey());
            }
        }
    }

    protected void postValue(T t) {
        synchronized (this.mDataLock) {
            Object obj = this.mPendingData == NOT_SET ? 1 : null;
            this.mPendingData = t;
        }
        if (obj != null) {
            ArchTaskExecutor.getInstance().postToMainThread(this.mPostValueRunnable);
        }
    }

    @MainThread
    protected void setValue(T t) {
        assertMainThread("setValue");
        this.mVersion++;
        this.mData = t;
        dispatchingValue(null);
    }

    @Nullable
    public T getValue() {
        T t = this.mData;
        return t != NOT_SET ? t : null;
    }

    int getVersion() {
        return this.mVersion;
    }

    public boolean hasObservers() {
        return this.mObservers.size() > 0;
    }

    public boolean hasActiveObservers() {
        return this.mActiveCount > 0;
    }

    static boolean isActiveState(State state) {
        return state.isAtLeast(State.STARTED);
    }

    private void assertMainThread(String str) {
        if (!ArchTaskExecutor.getInstance().isMainThread()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot invoke ");
            stringBuilder.append(str);
            stringBuilder.append(" on a background");
            stringBuilder.append(" thread");
            throw new IllegalStateException(stringBuilder.toString());
        }
    }
}
