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

import static com.abiddarris.common.stream.InputStreams.readLine;
import static com.abiddarris.common.stream.InputStreams.skipExact;
import static com.abiddarris.common.utils.Compares.compareLong;

import static java.util.Collections.sort;

import com.abiddarris.common.stream.IndependentCloseInputStream;
import com.abiddarris.common.stream.LimitedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public abstract class Unrpa {
    
    private InputStream stream;
    private long posAfterEntryFullyRead;
    private RpaEntry entry;
    private Queue<RpaEntry> entries = new ArrayDeque<>();
    
    protected abstract List<RpaEntry> initEntries() throws IOException;
    
    public void init(InputStream stream) throws IOException {
        this.stream = stream;
        
        List<RpaEntry> entries = initEntries();
        sort(entries, (e1, e2) -> compareLong(e1.getDesiredOffset(), e2.getDesiredOffset()));
        
        this.entries.addAll(entries);
    }
    
    public RpaEntry nextEntry() throws IOException {
        RpaEntry entry = entries.poll();
        if(entry == null) {
            return entry;
        }
        
        long offset = entry.getDesiredOffset();
        long length = entry.getDesiredLength();
        
        long n = offset;
        if(this.entry != null) {
            n = offset - posAfterEntryFullyRead;
            
            this.entry.close();
        }
        
        posAfterEntryFullyRead = offset + length;
        skipExact(stream, n);
        
        entry.init(
            new IndependentCloseInputStream(
                new LimitedInputStream(stream, length)
            )
        );
        
        this.entry = entry;
        
        return entry;
    }
    
    public static Unrpa create(InputStream stream) throws IOException {
        String header = new String(readLine(stream));
        if (header.startsWith("RPA-3.0")) {
            return new Unrpa3(header, stream);
        }
        throw new UnknownArchiveException();
    }
    
}
