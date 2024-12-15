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

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends ViewModel {
    
    private ExecutorService executor; 
    private List<Task> tasks = new LinkedList<>();
    private ViewModelStoreOwner owner;
    
    @NonNull
    public static TaskViewModel getInstance(ViewModelStoreOwner owner) {
        return getInstance(owner, TaskViewModel.class);
    }
    
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T extends TaskViewModel> T getInstance(ViewModelStoreOwner owner, Class<T> viewModel) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(viewModel);
        
        TaskViewModel model = new ViewModelProvider(owner)
            .get(viewModel);
        model.attachOwner(owner);
        
        return (T) model;
    }

    /**
     * Returns the owner of this {@code TaskViewModel}
     *
     * @return the owner of this {@code TaskViewModel}
     */
    public ViewModelStoreOwner getOwner() {
        return owner;
    }

    /**
     * Returns the owner of this {@code TaskViewModel}
     *
     * @return the owner of this {@code TaskViewModel}
     */
    public ViewModelStoreOwner getOwner() {
        return owner;
    }
    
    public void execute(@NonNull Task task) {
        Objects.requireNonNull(task);
        
        tasks.add(task);
            
        attachToRunnable(task);
            
        getExecutor().submit(task);
    }    
        
    @Override
    protected void onCleared() {
        super.onCleared();
          
        if(executor != null)   
            executor.shutdown();
    }
    
    @NonNull
    protected ExecutorService newExecutor() {
        return Executors.newSingleThreadExecutor();
    }
    
    void onTaskExecuted(Task task) {
        tasks.remove(task);
    }
    
    @NonNull
    private ExecutorService getExecutor() {
        if(executor == null) {
            var executor = newExecutor();
            
            Objects.requireNonNull(executor);
            
            this.executor = executor;
        }
        return executor;
    }
    
    private void attachOwner(ViewModelStoreOwner owner) {
        this.owner = owner;
        
        attachToRunnables();
    }
    
    private void attachToRunnables() {
        tasks.forEach(this::attachToRunnable);
    }
        
    private void attachToRunnable(Task task) {
        task.onAttach(this, owner);
    }
        
}
