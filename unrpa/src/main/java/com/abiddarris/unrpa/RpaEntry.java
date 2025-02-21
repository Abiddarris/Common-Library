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
package com.abiddarris.unrpa;

import java.io.IOException;
import java.io.InputStream;

public abstract class RpaEntry extends InputStream {

    private String name;
    
    protected RpaEntry(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    protected abstract void init(InputStream stream);
    
    protected abstract long getDesiredLength();
    
    protected abstract long getDesiredOffset();
    
    public abstract long getSize();

    @Override
    public abstract int read() throws IOException;

    @Override
    public abstract int read(byte[] b, int off, int len) throws IOException;
    
    @Override
    public abstract long skip(long n) throws IOException;

    @Override
    public abstract int available() throws IOException;

    @Override
    public abstract void close() throws IOException;
}
