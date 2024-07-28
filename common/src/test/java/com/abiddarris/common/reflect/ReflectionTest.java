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
package com.abiddarris.common.reflect;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ReflectionTest {
    
    @Test
    public void findMethodInTheSameClass() throws NoSuchMethodException {
        Method method = Reflections.findMethodByName(ReflectionTest.class, "findMethodInTheSameClass");
        
        assertEquals(
            ReflectionTest.class
                .getDeclaredMethod("findMethodInTheSameClass"),
            method);
    }
    
    @Test
    public void findMethodInTheClassParent() throws NoSuchMethodException {
        Method method = Reflections.findMethodByName(Dog.class, "blink");
        
        assertEquals(
            Animal.class.getDeclaredMethod("blink"),
            method);
    }
    
    @Test
    public void findNonExistMethod() {
        Method method = Reflections.findMethodByName(Dog.class, "jump");
        
        assertNull(method);
    }
    
    class Animal {
        public void blink() {}
    }
    
    class Dog extends Animal {
    }
}
