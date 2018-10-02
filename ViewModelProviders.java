package android.arch.lifecycle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.arch.lifecycle.ViewModelProvider.NewInstanceFactory;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class ViewModelProviders {
    @SuppressLint({"StaticFieldLeak"})
    private static DefaultFactory sDefaultFactory;

    public static class DefaultFactory extends NewInstanceFactory {
        private Application mApplication;

        public DefaultFactory(@NonNull Application application) {
            this.mApplication = application;
        }

        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> cls) {
            StringBuilder stringBuilder;
            if (!AndroidViewModel.class.isAssignableFrom(cls)) {
                return super.create(cls);
            }
            try {
                return (ViewModel) cls.getConstructor(new Class[]{Application.class}).newInstance(new Object[]{this.mApplication});
            } catch (Throwable e) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot create an instance of ");
                stringBuilder.append(cls);
                throw new RuntimeException(stringBuilder.toString(), e);
            } catch (Throwable e2) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot create an instance of ");
                stringBuilder.append(cls);
                throw new RuntimeException(stringBuilder.toString(), e2);
            } catch (Throwable e22) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot create an instance of ");
                stringBuilder.append(cls);
                throw new RuntimeException(stringBuilder.toString(), e22);
            } catch (Throwable e222) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot create an instance of ");
                stringBuilder.append(cls);
                throw new RuntimeException(stringBuilder.toString(), e222);
            }
        }
    }

    private static void initializeFactoryIfNeeded(Application application) {
        if (sDefaultFactory == null) {
            sDefaultFactory = new DefaultFactory(application);
        }
    }

    private static Application checkApplication(Activity activity) {
        activity = activity.getApplication();
        if (activity != null) {
            return activity;
        }
        throw new IllegalStateException("Your activity/fragment is not yet attached to Application. You can't request ViewModel before onCreate call.");
    }

    private static Activity checkActivity(Fragment fragment) {
        fragment = fragment.getActivity();
        if (fragment != null) {
            return fragment;
        }
        throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment) {
        initializeFactoryIfNeeded(checkApplication(checkActivity(fragment)));
        return new ViewModelProvider(ViewModelStores.of(fragment), sDefaultFactory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity fragmentActivity) {
        initializeFactoryIfNeeded(checkApplication(fragmentActivity));
        return new ViewModelProvider(ViewModelStores.of(fragmentActivity), sDefaultFactory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Fragment fragment, @NonNull Factory factory) {
        checkApplication(checkActivity(fragment));
        return new ViewModelProvider(ViewModelStores.of(fragment), factory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull FragmentActivity fragmentActivity, @NonNull Factory factory) {
        checkApplication(fragmentActivity);
        return new ViewModelProvider(ViewModelStores.of(fragmentActivity), factory);
    }
}
