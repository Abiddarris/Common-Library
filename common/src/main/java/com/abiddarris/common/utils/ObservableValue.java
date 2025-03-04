/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
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
package com.abiddarris.common.utils;

import java.util.HashSet;
import java.util.Set;

public class ObservableValue<T> {

    private final Set<Observer<T>> observers = new HashSet<>();

    private T value;

    public ObservableValue() {
        this(null);
    }

    public ObservableValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;

        for (var observer : observers) {
            observer.onChanged(value);
        }
    }

    public void addObserver(Observer<T> observer) {
        if (observers.add(observer)) {
            observer.onChanged(value);
        }
    }

    public boolean removeObserver(Observer<T> observer) {
        return observers.remove(observer);
    }

    public void clearObservers() {
        observers.clear();
    }
}
