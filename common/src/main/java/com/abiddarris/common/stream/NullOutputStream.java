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
package com.abiddarris.common.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Write into nothingness
 *
 * @author Abiddarris
 * @since 1.0
 */
public class NullOutputStream extends OutputStream {
    
    @Override
    public void write(int b) throws IOException {
    }
    
    @Override
    public void write(byte[] b) throws IOException {
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
    }
    
}
