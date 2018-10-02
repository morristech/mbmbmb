package android.arch.lifecycle;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class ViewModelStores {
    private ViewModelStores() {
    }

    @MainThread
    public static ViewModelStore of(@NonNull FragmentActivity fragmentActivity) {
        return HolderFragment.holderFragmentFor(fragmentActivity).getViewModelStore();
    }

    @MainThread
    public static ViewModelStore of(@NonNull Fragment fragment) {
        return HolderFragment.holderFragmentFor(fragment).getViewModelStore();
    }
}
