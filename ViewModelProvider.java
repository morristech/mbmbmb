package android.arch.lifecycle;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

public class ViewModelProvider {
    private static final String DEFAULT_KEY = "android.arch.lifecycle.ViewModelProvider.DefaultKey";
    private final Factory mFactory;
    private final ViewModelStore mViewModelStore;

    public interface Factory {
        @NonNull
        <T extends ViewModel> T create(@NonNull Class<T> cls);
    }

    public static class NewInstanceFactory implements Factory {
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> cls) {
            StringBuilder stringBuilder;
            try {
                return (ViewModel) cls.newInstance();
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
            }
        }
    }

    public ViewModelProvider(@NonNull ViewModelStoreOwner viewModelStoreOwner, @NonNull Factory factory) {
        this(viewModelStoreOwner.getViewModelStore(), factory);
    }

    public ViewModelProvider(@NonNull ViewModelStore viewModelStore, @NonNull Factory factory) {
        this.mFactory = factory;
        this.mViewModelStore = viewModelStore;
    }

    @NonNull
    public <T extends ViewModel> T get(@NonNull Class<T> cls) {
        String canonicalName = cls.getCanonicalName();
        if (canonicalName == null) {
            throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("android.arch.lifecycle.ViewModelProvider.DefaultKey:");
        stringBuilder.append(canonicalName);
        return get(stringBuilder.toString(), cls);
    }

    @MainThread
    @NonNull
    public <T extends ViewModel> T get(@NonNull String str, @NonNull Class<T> cls) {
        T t = this.mViewModelStore.get(str);
        if (cls.isInstance(t)) {
            return t;
        }
        cls = this.mFactory.create(cls);
        this.mViewModelStore.put(str, cls);
        return cls;
    }
}
