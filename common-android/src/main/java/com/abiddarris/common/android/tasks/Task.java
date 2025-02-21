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
package com.abiddarris.common.android.tasks;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelStoreOwner;

import com.abiddarris.common.android.dialogs.ExceptionDialog;
import com.abiddarris.common.utils.BaseRunnable;

public abstract class Task implements BaseRunnable {
    
    private Context applicationContext;
    private TaskViewModel model;
    private ViewModelStoreOwner owner;
    
    @CallSuper
    protected void onAttach(@NonNull TaskViewModel model,
                            @NonNull ViewModelStoreOwner owner) {
        this.model = model;
        this.owner = owner;
    }
    
    @Nullable
    public ViewModelStoreOwner getOwner() {
        return owner;
    }
    
    public Context getApplicationContext() {
        if(applicationContext == null) {
            if (owner instanceof Fragment) {
                applicationContext = ((Fragment)owner).getActivity();
            } else {
                applicationContext = (Context) owner;
            }

            applicationContext = applicationContext.getApplicationContext();
        }
        return applicationContext;
    }

    @NonNull
    protected FragmentManager getFragmentManager() {
        var owner = getOwner();
        if(owner instanceof FragmentActivity) {
            return ((FragmentActivity)owner).getSupportFragmentManager();
        }

        if(owner instanceof Fragment) {
            return ((Fragment)owner).getParentFragmentManager();
        }

        throw new ClassCastException();
    }

    public TaskViewModel getModel() {
        return model;
    }
    
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends AppCompatActivity> T getActivity() {
        return (T)owner;
    }
    
    @Override
    public void onThrowableCatched(Throwable throwable) {
        BaseRunnable.super.onThrowableCatched(throwable);

        var dialog = new ExceptionDialog();
        dialog.setThrowable(throwable);
        dialog.show(getFragmentManager(), null);
    }
    
    @Override
    public void onFinally() {
        BaseRunnable.super.onFinally();
        
        model.onTaskExecuted(this);
    }
    
}