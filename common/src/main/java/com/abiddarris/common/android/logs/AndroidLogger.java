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
package com.abiddarris.common.android.logs;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.WARN;

import android.util.Log;

import com.abiddarris.common.logs.Level;
import com.abiddarris.common.logs.Logger;

/**
 * {@code Logger} implementation that log to default android logger
 *
 * @since 1.0
 * @author Abiddarris
 */
public class AndroidLogger extends Logger {
    
    private int level;
    
    public AndroidLogger(Level level, String tag) {
        super(level, tag);
        
        this.level = determineAndroidLevel();
        
    }
    
    private int determineAndroidLevel() {
        switch(getLevel()) {
            case DEBUG :
                return DEBUG;
            case INFO :
                return INFO;
            case WARNING :
                return WARN;
            case ERROR :
                return ERROR;
            default : 
                return DEBUG;
        }
    }
    
    @Override
    public void log(String string) {
        Log.println(level, getTag(), string);
    }
    
}
