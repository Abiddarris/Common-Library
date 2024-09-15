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

import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.issubclass;
import static com.abiddarris.common.renpy.internal.PythonObject.newBoolean;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.type;
import static com.abiddarris.common.renpy.internal.Sys.sys;
import static com.abiddarris.common.renpy.internal.core.Functions.bool;
import static com.abiddarris.common.renpy.internal.core.Functions.hasattr;
import static java.util.regex.Pattern.quote;

import com.abiddarris.common.renpy.internal.core.Functions;
import com.abiddarris.common.renpy.internal.imp.Imports;

public class BuiltinsImpl {
    
    static PythonObject importAs(String name) {
        return Imports.importAs(name);
    }
    
    private static PythonObject len(PythonObject obj) {
        return Functions.len(obj);
    }
    
    private static PythonObject hasAttr(PythonObject obj, PythonObject name) {
        return hasattr(obj, name);
    }
    
    private static PythonObject importImpl(PythonObject name) {
        String[] parts = name.toString().split(quote("."));
        importAs(name.toString());
        
        return sys.getAttribute("modules").getItem(newString(parts[0]));
    }
    
    private static PythonObject boolNew(PythonObject cls, PythonObject obj) {
        return bool(obj);
    }

    private static void boolInit(PythonObject cls, PythonObject obj) {
    }
    
    private static PythonObject typeStr(PythonObject self) {
        return newString(new StringBuilder()
            .append("<class '")
            .append(self.getAttribute("__name__").toString())
            .append("'>")
            .toString());
    }
    
    private static PythonObject objectStr(PythonObject self) {
        return newString(new StringBuilder()
            .append("<")
            .append(self.getTypeAttribute("__name__").toString())
            .append(" object>")
            .toString());
    }
    
    private static PythonObject objectInstanceCheck(PythonObject self, PythonObject other) {
        return issubclass.call(type.call(other), self);
    }
    
    private static PythonObject objectNe(PythonObject self, PythonObject other) {
        return newBoolean(!self.equals(other));
    }
    
    private static PythonObject strNew(PythonObject cls, PythonObject obj) {
        PythonString string = new PythonString(obj.toString());
        string.setAttributeDirectly("__class__", cls);
        
        return string;
    }
    
    private static void strInit(PythonObject cls, PythonObject obj) {
    }
    
    private static PythonObject typeSubclassCheck(PythonObject self, PythonObject other) {
        if (self == other) {
            return True;
        }
        
        return hasParent(other, self);
    }
    
    private static PythonObject hasParent(PythonObject target, PythonObject parent) {
        PythonObject parents = target.getAttribute("__bases__");
        int len = parents.length();
        for(int i = 0; i < len; ++i) {
        	PythonObject parent0 = parents.getItem(newInt(i));
            if(parent0 == parent) {
                return True;
            }
            
            PythonObject result = hasParent(parent0, parent);
            if(result.toBoolean()) {
                return True;
            }
        }
        
        return False;
    }
    
    private static PythonObject noneTypeNew(PythonObject cls) {
        return None;
    }
    
    private static void noneTypeInit(PythonObject self) {}
    
    private static void noneTypeCall(PythonObject self) {
        TypeError.call(newString("NoneType object is not callable")).raise();
    }
    
    private static PythonObject noneTypeBool(PythonObject self) {
        return False;
    }
}