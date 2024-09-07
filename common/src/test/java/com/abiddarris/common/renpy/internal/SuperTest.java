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
package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.Python.*;
import static com.abiddarris.common.renpy.internal.PythonObject.*;

import com.abiddarris.common.utils.ObjectWrapper;
import static org.junit.jupiter.api.Assertions.*;

import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;

import org.junit.jupiter.api.Test;

public class SuperTest {
    
    private static final PythonObject Child;
    
    static {
        JavaModuleLoader.registerLoader("supertest", (supertest) -> {
            PythonObject Parent = ParentImpl.define(supertest);
    
            ChildImpl.define(supertest, Parent);
        });
        PythonObject supertest = __import__.call(newString("supertest"));
        Child = supertest.getAttribute("Child");
    }
    
    private static class ParentImpl {
        
        private static PythonObject define(PythonObject supertest) {
        	ClassDefiner definer = supertest.defineClass("Parent");
            definer.defineFunction("set_number", ParentImpl.class, "setNumber", "self");
            
            return definer.define();
        }
        
        private static void setNumber(PythonObject self) {
            self.setAttribute("a", newInt(10));
        }
    }
    
    private static class ChildImpl {
        
        private static PythonObject define(PythonObject supertest, PythonObject Parent) {
        	ClassDefiner definer = supertest.defineClass("Child", Parent);
            definer.defineFunction("set_number", ChildImpl.class, "setNumber", "self");
            definer.defineFunction("do_something", ChildImpl.class, "doSomething", "self");
            
            return definer.define();
        }
        
        private static void setNumber(PythonObject self) {
            self.setAttribute("a", newInt(100));
        }
        
        private static void doSomething(PythonObject self) {
        }
    }
    
    @Test
    public void superTest() {
        PythonObject child = Child.call();
        child.callAttribute("set_number");
        
        assertEquals(newInt(100), child.getAttribute("a"));
        
        child.getSuper().callAttribute("set_number");
        
        assertEquals(newInt(10), child.getAttribute("a"));
    }
    
    @Test
    public void getNonExistMethodOnParent() {
        PythonObject child = Child.call();
   
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>(false);
        
        tryExcept(() -> child.getSuper().callAttribute("do_something")).
        onExcept((e) -> thrown.setObject(true), AttributeError).execute();
        
        assertTrue(thrown.getObject());
    }
}
