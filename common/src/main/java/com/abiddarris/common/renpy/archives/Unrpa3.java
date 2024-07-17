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
package com.abiddarris.common.renpy.archives;

import static com.abiddarris.common.renpy.internal.Pickle.loads;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.InflaterInputStream;

class Unrpa3 extends Unrpa {
    
    private String header;
    private InputStream stream;
    
    Unrpa3(String header, InputStream stream) {
        this.header = header;
        this.stream = stream;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected List<RpaEntry> initEntries() throws IOException {
        List<RpaEntry> entries = new ArrayList<>();
        String[] components = header.split("\\s+");
        
        int offset = Integer.parseInt(components[1], 16);
        int key = Integer.parseInt(components[2], 16);
        
        stream.readNBytes(offset - header.length() - 1);
        
        InflaterInputStream inflater = new InflaterInputStream(stream);
        Map map = ((Map)loads(inflater, "bytes"));
        map.forEach((k, v) -> {
             List value = (List) v;   
             ((List)value.remove(0))
                 .forEach(value::add);    
             value.add(0, (int)value.remove(0) ^ key);
             value.add(1, (int)value.remove(1) ^ key);
             
             entries.add(new RpaEntry3((String)k, (List)v));
        });
        
        return entries;
    }

}
