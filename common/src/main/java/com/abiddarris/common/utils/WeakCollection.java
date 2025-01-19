/***********************************************************************************
 * Copyright 2024 - 2025 Abiddarris
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

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A collection that holds elements using {@link WeakReference}.
 *
 * <p>This collection allows objects to be garbage collected when they are no longer referenced by
 * any strong references, even if they are still present in the collection. It provides typical
 * collection functionality, such as adding (only non null), removing, and iterating over elements,
 * but with the caveat that elements may be removed automatically when they are garbage collected.</p>
 *
 * @param <V> the type of elements in the collection
 */
public class WeakCollection<V> extends AbstractCollection<V> {

    private List<WeakReference<V>> list = new ArrayList<>();

    /**
     * Adds an element to the collection.
     *
     * @param v the element to add
     * @return {@code true} if the element was successfully added
     * @throws NullPointerException if the element is {@code null}
     */
    @Override
    public boolean add(V v) {
        if (v == null) {
            throw new NullPointerException();
        }
        return list.add(new WeakReference<>(v));
    }

    /**
     * Removes all elements from the collection.
     */
    @Override
    public void clear() {
        list.clear();
    }

    /**
     * Returns an iterator over the elements in the collection.
     *
     * <p>The iterator will skip over any elements that have been garbage collected, removing
     * them from the list as it iterates.</p>
     *
     * @return an iterator over the elements in the collection
     */
    @Override
    public Iterator<V> iterator() {
        return new WeakIteratorImpl();
    }

    /**
     * Returns the number of live elements in the collection.
     *
     * <p>The size is calculated by counting non-null elements that have not been garbage collected.
     * call to this method may be expensive because it count the size by iterating the elements.</p>
     *
     * @return the number of live elements in the collection
     */
    @Override
    public int size() {
        Iterator<V> iterator = iterator();
        int count = 0;
        while(iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    private class WeakIteratorImpl implements Iterator<V> {

        private final Iterator<WeakReference<V>> iterator;
        private V currentVal;

        private WeakIteratorImpl() {
            iterator = list.iterator();
        }

        @Override
        public boolean hasNext() {
            if (currentVal != null) {
                return true;
            }

            while (iterator.hasNext()) {
                WeakReference<V> ref = iterator.next();
                currentVal = ref.get();
                if (currentVal == null) {
                    iterator.remove();
                    continue;
                }

                return true;
            }
            return false;
        }

        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            V val = currentVal;
            currentVal = null;

            return val;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
