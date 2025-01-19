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

import static com.abiddarris.common.stream.Signs.sign;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.ByteOrder.nativeOrder;

import com.abiddarris.common.annotations.PrivateApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@PrivateApi
public class Struct {
    
    public static Number[] unpack(String format, int[] unsignedBytes) {
        return unpack(format, sign(unsignedBytes));
    } 
    
    public static Number[] unpack(String format, byte[] signedBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(signedBytes);
        buffer.order(nativeOrder());
        
        List<Number> number = new ArrayList<>();
        for(char c : format.toCharArray()) {
        	switch(c) {
                case '<' :
                    buffer.order(LITTLE_ENDIAN);
                    break;
                case 'I' :
                    number.add(buffer.getInt() & 0xFFFFFFFFL);
                    break;
                case 'i' :
                    number.add(buffer.getInt());
                    break;
                case 'H' :
                    number.add(buffer.getShort() & 0xFFFF);
                    break;
                default :
                    throw new IllegalArgumentException("Unknown format : " + c);
            }
        }
        
        return number.toArray(Number[]::new);
    }
    
}

