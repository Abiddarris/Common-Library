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

import static com.abiddarris.common.renpy.internal.PythonObject.AttributeError;
import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.newBoolean;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

public class BuiltinsImpl {
    
    private static PythonObject len(PythonObject obj) {
        return obj.callTypeAttribute("__len__");
    }
    
    private static PythonObject isSubclass(PythonObject cls, PythonObject base) {
        return base.callTypeAttribute("__subclasscheck__", cls);
    }
    
    private static PythonObject boolNew(PythonObject cls, PythonObject obj) {
        ObjectWrapper<PythonObject> returnValue = new ObjectWrapper<>();
        tryExcept(() -> {
            returnValue.setObject(obj.callTypeAttribute("__bool__"));
        }).onExcept((e) -> {
            tryExcept(() -> {
                returnValue.setObject(newBoolean(
                                obj.callTypeAttribute("__len___")
                                   .toInt() != 0));
            }).onExcept((e1) -> {
                returnValue.setObject(True);
            }, AttributeError).execute();
        }, AttributeError).execute();
        
        return returnValue.getObject();
    }
    
    private static void boolInit(PythonObject cls, PythonObject obj) {
    }
    
    private static PythonObject typeNew(PythonObject cls, PythonObject args) {
        if(args.length() == 1)  {
            return args.getItem(newInt(0))
                .getAttribute("__class__");
        }
        
        if(args.length() != 3) {
            TypeError.call().raise();
        }
        
        PythonObject name = args.getItem(newInt(0));
        PythonObject bases = args.getItem(newInt(1));
        PythonObject attributes = args.getItem(newInt(2));
        
        if(bases.length() == 0) {
            bases = newTuple(object);
        }
        
        PythonObject self = new PythonObject();
        self.setAttribute("__class__", cls);
        self.setAttribute("__name__", name);
        self.setAttribute("__bases__", bases);
       
        return self;
    }
    
    private static void typeInit(PythonObject self, PythonObject args) {
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
            .append(self.getAttribute("__name__").toString())
            .append(" object>")
            .toString());
    }
    
    private static PythonObject strNew(PythonObject cls, PythonObject obj) {
        return obj.callTypeAttribute("__str__");
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
}