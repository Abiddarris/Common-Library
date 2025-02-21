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

import static com.abiddarris.python3.Pickle.loads;

import com.abiddarris.python3.PythonDict;
import com.abiddarris.python3.PythonObject;

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
        PythonObject map = ((PythonObject)loads(inflater, "bytes"));
        map.forEach(k -> {
            PythonObject value = map.getItem(k);
            PythonObject item = value.callAttribute("pop");

            List jItem = new ArrayList<>();
            item.forEach(jItem::add);

            System.out.println(k + " " + jItem);

            jItem.add(0, ((PythonObject)jItem.remove(0)).toInt() ^ key);
            jItem.add(1, ((PythonObject)jItem.remove(1)).toInt() ^ key);

            PythonObject start = (PythonObject) jItem.remove(2);
            int[] jStart = new int[start.length()];
            for (int i = 0; i < start.length(); i++) {
                jStart[i] = start.getItem(i).toInt();
            }
            jItem.add(2, jStart);

            entries.add(new RpaEntry3(k.toString(), jItem));
        });
        
        return entries;
    }

}
