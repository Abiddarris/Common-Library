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
package com.abiddarris.common.logs.factory;

import com.abiddarris.common.logs.Level;
import com.abiddarris.common.logs.Logger;
import com.abiddarris.common.logs.StreamLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FileLogFactory implements LogFactory {

    private OutputStream stream;

    public FileLogFactory(File file) throws FileNotFoundException {
        this.stream = new FileOutputStream(file, true);
    }

    @Override
    public Logger newLogger(Level level, String tag) {
        return new StreamLogger(level, tag, stream);
    }
}
