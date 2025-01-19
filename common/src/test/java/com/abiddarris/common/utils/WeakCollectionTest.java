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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeakCollectionTest {

    private WeakCollection<String> collection;

    @BeforeEach
    void setUp() {
        collection = new WeakCollection<>();
    }

    @Test
    void testAddElement() {
        assertTrue(collection.add("TestElement"));
        assertEquals(1, collection.size());
    }

    @Test
    void testAddNullElement() {
        assertThrows(NullPointerException.class, () -> collection.add(null));
    }

    @Test
    void testRemoveElement() {
        collection.add("TestElement");
        assertTrue(collection.remove("TestElement"));
        assertEquals(0, collection.size());
    }

    @Test
    void testRemoveNonExistentElement() {
        collection.add("TestElement");
        assertFalse(collection.remove("NonExistent"));
        assertEquals(1, collection.size());
    }

    @Test
    void testSizeAfterClear() {
        collection.add("TestElement1");
        collection.add("TestElement2");
        collection.clear();
        assertEquals(0, collection.size());
    }

    @Test
    void testIterator() {
        collection.add("TestElement1");
        collection.add("TestElement2");

        var iterator = collection.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("TestElement1", iterator.next());

        assertTrue(iterator.hasNext());
        assertEquals("TestElement2", iterator.next());

        assertFalse(iterator.hasNext());
    }

    @Test
    void testSizeAfterGarbageCollection() {
        String element1 = new String("TestElement1");
        String element2 = new String("TestElement2");

        collection.add(element1);
        collection.add(element2);

        assertEquals(2, collection.size());

        // Remove strong references and suggest garbage collection
        element1 = null;
        element2 = null;

        // Suggest garbage collection (this is not guaranteed to run immediately)
        System.gc();

        // Give time for the garbage collector to do its work
        try {
            Thread.sleep(100);  // Sleep for 100 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Size should be 0 if the elements are garbage collected
        assertEquals(0, collection.size());
    }

    @Test
    void testSizeAfterGarbageCollectionWithLiveElement() {
        String element1 = new String("TestElement1");
        String element2 = new String("TestElement2");

        collection.add(element1);
        collection.add(element2);

        assertEquals(2, collection.size());

        // Remove the strong reference to element1 and suggest GC
        element1 = null;

        System.gc();

        // Give time for the garbage collector to do its work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Only element2 should be left, so size should be 1
        assertEquals(1, collection.size());
    }

    @Test
    void testIteratorRemovesExpiredReferences() {
        String element1 = new String("TestElement1");
        String element2 = new String("TestElement2");

        collection.add(element1);
        collection.add(element2);

        // Remove strong references
        element1 = null;
        element2 = null;

        // Suggest garbage collection (this is not guaranteed to run immediately)
        System.gc();

        // Give time for the garbage collector to do its work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Iterator should not return any elements
        var iterator = collection.iterator();
        assertFalse(iterator.hasNext());
    }
}
