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

import static com.abiddarris.common.stream.InputStreams.discardAll;
import static com.abiddarris.common.stream.Signs.sign;

import com.abiddarris.common.stream.StreamSequence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class RpaEntry3 extends RpaEntry {

    private InputStream stream;
    private int[] start;
    private long offset;
    private long length;
    
    RpaEntry3(String name, List components) {
        super(name);
        
        offset = (int)components.get(0);
        length = (int)components.get(1);
        start = (int[])components.get(2);
    }
    
    @Override
    protected void init(InputStream stream) {
        this.stream = new StreamSequence(
            new ByteArrayInputStream(
                sign(start)
            ),
            stream
        );
    }

    @Override
    protected long getDesiredLength() {
        return length;
    }

    @Override
    protected long getDesiredOffset() {
        return offset;
    }

    @Override
    public long getSize() {
        return start.length + length;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }
   
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }
    
    @Override
    public void close() throws IOException {
        discardAll(stream);
    }
}
