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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class BaseDialogFragment<Result> extends DialogFragment {
    
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final String ID = "id";
    private static final String RESULT_CALLED = "resultCalled";
    private static final String RESULT_LISTENER = "resultListener";
    private static final Map<String, Map<String, Object>> NON_SERIALIZABLE_OBJECTS = new HashMap<>();
    private static final Random RANDOM = new Random();
    
    private BaseDialogViewModel model;
    private Map<String, Object> variables = new HashMap<>();
    
    @Override
    @CallSuper
    @MainThread
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        System.out.println("Returns result for " + getClass() + " with value of : " + lock.getObject());
        return lock.getObject();
    }
    
    public void showForResult(@NonNull FragmentManager manager, @Nullable Consumer<Result> resultListener) {
        saveVariable(RESULT_LISTENER, resultListener);
        
        show(manager, null);
    }
    
    protected synchronized void sendResult(@Nullable Result result) {
        System.out.println("send result called for " + getClass() + " with hash " + hashCode()  );
        System.out.println(NON_SERIALIZABLE_OBJECTS);
        
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
    protected <T> T getVariable(@Nullable String name) {
        return getVariable(name, null);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> T getVariable(@Nullable String name, T defaultVal) {
        return (T)variables.getOrDefault(name, defaultVal);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    protected <T> T saveVariable(@Nullable String name, @Nullable T obj) {
        return (T)variables.put(name, obj);
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
    }
    
    public static class BaseDialogViewModel extends ViewModel {
        
        private Map<String, Object> variables = null;
        private BaseDialogFragment fragment;
        
        private Map<String, Object> attach(Map<String, Object> variables, BaseDialogFragment fragment) {
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
