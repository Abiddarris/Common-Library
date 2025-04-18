package com.abiddarris.common.android.tasks.v2.vm;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.abiddarris.common.android.tasks.v2.TaskManager;
import com.abiddarris.common.android.tasks.v2.dialog.DialogProgressPublisherManager;

import java.util.Objects;

public class TaskViewModel extends ViewModel {

    private TaskManager taskManager;
    private DialogProgressPublisherManager dialogManager;

    public void attach(FragmentActivity activity) {
        if (taskManager == null) {
            taskManager = new TaskManager(activity);
            dialogManager = new DialogProgressPublisherManager(activity);
        }

        taskManager.setContext(activity);
        dialogManager.attach(activity);
    }


    public void attach(Fragment fragment) {
        if (taskManager == null) {
            taskManager = new TaskManager(fragment.getContext());
            dialogManager = new DialogProgressPublisherManager(fragment);
        }

        taskManager.setContext(fragment.getContext());
        dialogManager.attach(fragment);
    }

    public DialogProgressPublisherManager getDialogManager() {
        return dialogManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * Always call this method in {@link Fragment#onDestroy} or {@link FragmentActivity#onDestroy()}
     */
    public void detach() {
        dialogManager.invalidate();
    }

    @Override
    protected void onCleared() {
        taskManager.shutdown(true);

        super.onCleared();
    }

    @NonNull
    public static TaskViewModel getInstance(FragmentActivity activity) {
        return getInstance(activity, TaskViewModel.class);
    }

    @NonNull
    public static TaskViewModel getInstance(Fragment fragment) {
        return getInstance(fragment, TaskViewModel.class);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends TaskViewModel> T getInstance(FragmentActivity activity, Class<T> viewModel) {
        TaskViewModel model = getInstanceInternal(activity, viewModel);
        model.attach(activity);

        return (T) model;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends TaskViewModel> T getInstance(Fragment fragment, Class<T> viewModel) {
        TaskViewModel model = getInstanceInternal(fragment, viewModel);
        model.attach(fragment);

        return (T) model;
    }

    private static <T extends TaskViewModel> @NonNull TaskViewModel getInstanceInternal(ViewModelStoreOwner owner, Class<T> viewModel) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(viewModel);

        return new ViewModelProvider(owner)
                .get(viewModel);
    }
}
