/***********************************************************************************
 * Copyright 2024 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.common.android.dialogs;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.abiddarris.common.utils.ObjectWrapper;
import com.abiddarris.common.utils.ObservableValue;
import com.abiddarris.common.utils.Observer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BaseDialogFragment<Result> extends DialogFragment {
    
    private static final String ENABLE_POSITIVE_BUTTON = "enablePositiveButton";
    private static final String ENABLE_NEUTRAL_BUTTON = "enableNeutralButton";
    private static final String RESULT_CALLED = "resultCalled";
    private static final String RESULT_LISTENER = "resultListener";

    private BaseDialogViewModel model;
    private List<Runnable> onShowListeners = new ArrayList<>();
    private Map<String, ObservableValue<?>> variables = new HashMap<>();

    @Override
    @CallSuper
    @MainThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        saveVariable(RESULT_CALLED, false);
        
        model = new ViewModelProvider(this)
            .get(BaseDialogViewModel.class);
        variables = model.attach(variables, this);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = newDialogBuilder();
        if(builder == null) {
            throw new NullPointerException("newDialogBuilder() cannot return null");
        }
        
        onCreateDialog(builder, savedInstanceState);
        
        AlertDialog dialog = builder.create();
        onDialogCreated(dialog, savedInstanceState);
        
        return dialog;
    }

    /**
     * Called when dialog is showed to the user.
     */
    @CallSuper
    public void onShowDialog() {
        for (Runnable onShowListen : onShowListeners) {
            onShowListen.run();
        }
    }

    @Nullable
    public Result showForResultAndBlock(@NonNull FragmentManager manager) {
        ObjectWrapper<Result> lock = new ObjectWrapper<>();
        ObjectWrapper<Boolean> called = new ObjectWrapper<>(false);
        showForResult(manager, (val) -> {
            lock.setObject(val);
            called.setObject(true);
               
            synchronized(lock) {
                lock.notifyAll();
            }
        });
        if(!called.getObject()) {
            synchronized(lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        
        return lock.getObject();
    }
    
    public void showForResult(@NonNull FragmentManager manager, @Nullable Consumer<Result> resultListener) {
        saveVariable(RESULT_LISTENER, resultListener);
        
        show(manager, null);
    }
    
    protected synchronized void sendResult(@Nullable Result result) {
        Consumer<Result> listener = getVariable(RESULT_LISTENER);
        if(listener == null) {
            return;
        }
        if(this.<Boolean>getVariable(RESULT_CALLED, false)) {
            throw new IllegalStateException("Cannot call sendResult more than once");
        }
        
        saveVariable(RESULT_CALLED, true);
        
        listener.accept(result);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getVariable(@Nullable String name) {
        return getVariable(name, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getVariable(@Nullable String name, T defaultVal) {
        ObservableValue<?> observableValue = variables.get(name);
        if (observableValue == null) {
            return defaultVal;
        }
        return (T) observableValue.getValue();
    }

    @Nullable
    public <T> T saveVariable(@Nullable String name, @Nullable T obj) {
        ObservableValue<T> observableValue = this.<T>getObservableValue(name);
        T oldValue = observableValue.getValue();
        observableValue.setValue(obj);

        return oldValue;
    }

    protected <T> void observe(String name, Observer<T> observer) {
        this.<T>getObservableValue(name).addObserver(observer);
    }

    @SuppressWarnings("unchecked")
    private <T> ObservableValue<T> getObservableValue(String name) {
        ObservableValue<T> observableValue = (ObservableValue<T>) variables.get(name);
        if (observableValue == null) {
            observableValue = new ObservableValue<>(null);
            variables.put(name, observableValue);
        }

        return observableValue;
    }
    
    public void enablePositiveButton(boolean enabled) {
        saveVariable(ENABLE_POSITIVE_BUTTON, enabled);
        
        enableButtonInternal(BUTTON_POSITIVE, enabled);
    }

    public void enableNeutralButton(boolean enabled) {
        saveVariable(ENABLE_NEUTRAL_BUTTON, enabled);

        enableButtonInternal(BUTTON_NEUTRAL, enabled);
    }
    
    private void enableButtonInternal(int buttonType, boolean enabled) {
        AlertDialog dialog = (AlertDialog)getDialog();
        if(dialog == null) {
            return;
        }

        Button button = dialog.getButton(buttonType);
        if(button == null) {
            addOnShowListener(() -> enableButtonInternal(buttonType, enabled));
            return;
        }
        button.setEnabled(enabled);
    }
    
    @Nullable
    protected Result getDefaultResult() {
        return null;
    }
    
    /**
     * Returns new {@code MaterialAlertDialogBuilder}.
     * Class that override this method must returns non null 
     * {@code MaterialAlertDialogBuilder}.
     *
     * @return new {@code MaterialAlertDialogBuilder}.
     */
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new DialogBuilder(getContext());
    }
    
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
    }
    
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        dialog.setOnShowListener(dialog0 -> onShowDialog());

        Boolean enablePositiveButton = getVariable(ENABLE_POSITIVE_BUTTON);
        if(enablePositiveButton != null) {
            addOnShowListener(() -> enablePositiveButton(enablePositiveButton));
        }

        Boolean enableNeutralButton = getVariable(ENABLE_NEUTRAL_BUTTON);
        if(enableNeutralButton != null) {
            addOnShowListener(() -> enableNeutralButton(enableNeutralButton));
        }
    }

    @CallSuper
    @Override
    public void onDestroy() {
        super.onDestroy();

        for (ObservableValue<?> value : variables.values()) {
            value.clearObservers();
        }
    }

    private void addOnShowListener(Runnable runnable) {
        onShowListeners.add(runnable);
    }
    
    public static class BaseDialogViewModel extends ViewModel {
        
        private Map<String, ObservableValue<?>> variables = null;
        private BaseDialogFragment fragment;
        
        private Map<String, ObservableValue<?>> attach(Map<String, ObservableValue<?>> variables, BaseDialogFragment fragment) {
            this.fragment = fragment;
            
            if(this.variables == null) {
                this.variables = variables;
            }
            return this.variables;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected void onCleared() {
            super.onCleared();
            
            try {
                fragment.sendResult(fragment.getDefaultResult());
            } catch (IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
        
    }
}
