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
package com.abiddarris.common.android.virtualkeyboard;

import androidx.fragment.app.DialogFragment;

import com.abiddarris.common.android.tasks.TaskDialog;

import static com.abiddarris.common.stream.InputStreams.readAll;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class LoadTask extends TaskDialog {
   
    private boolean loaded;
    private File src;
    private Exception exception;
    private VirtualKeyboard keyboard;
    
    LoadTask(VirtualKeyboard keyboard, File src) {
        this.keyboard = keyboard;
        this.src = src;
    }
    
    @Override
    public void execute() throws Exception {
        JSONObject keyboard;
        try (BufferedInputStream inputStream = new BufferedInputStream(
                new FileInputStream(src))) {
            keyboard = new JSONObject(
                new String(
                    readAll(inputStream)
                )
            );
        }
        
        getActivity()
            .runOnUiThread(() -> loadKeyboard(keyboard));
        
        if(loaded) {
            return;
        }
        
        synchronized(this) {
            wait();
        }
        
        if(exception != null)
            throw exception;
    }
    
    private void loadKeyboard(JSONObject keyboard) {
        try {
            this.keyboard.load(keyboard);
        } catch (Exception e) {
            exception = e;
        } finally {
            synchronized(this) {
                loaded = true;
                notify();
            }
        }
    }

    @Override
    protected DialogFragment newDialog() {
        return new LoadTaskDialog();
    }

    @Override
    protected String getTag() {
        return "loadKeyboardDialog";
    }
}
