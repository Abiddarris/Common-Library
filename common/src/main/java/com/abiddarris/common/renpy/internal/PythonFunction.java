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

import static com.abiddarris.common.utils.Exceptions.toUncheckException;

import com.abiddarris.common.renpy.internal.signature.BadSignatureError;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PythonFunction extends PythonObject {

    private Method method;
    private PythonSignature signature;

    public PythonFunction(Method method, PythonSignature signature) {
        this.signature = signature;
        this.method = method;
        
        int paramCount = method.getParameterCount();
        int signatureParamCount = signature.getParamaterSize();
        if(paramCount != signatureParamCount) {
            throw new BadSignatureError(String.format("Method %s takes %s %s but given signature takes %s %s",
                    method.getName(), paramCount, paramCount > 1 ? "arguments" : "argument",
                    signatureParamCount, signatureParamCount > 1 ? "arguments" : "argument"));
        }
        
        setAttributeDirectly("__class__", function);
        
        if(!method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    @Override
    public PythonObject call(PythonParameter parameter) {
        PythonObject[] args = signature.parseArguments(method, parameter);
        try {
            PythonObject object = (PythonObject)method.invoke(null, (Object[])args);
            return object != null ? object : None;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof Error) {
                throw (Error)cause;
            }
            throw toUncheckException(cause);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
