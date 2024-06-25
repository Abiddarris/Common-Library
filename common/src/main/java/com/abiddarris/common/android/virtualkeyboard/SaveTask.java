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

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

class SaveTask extends TaskDialog {

    private VirtualKeyboard keyboard;
    private File dest;

    SaveTask(VirtualKeyboard keyboard, File dest) {
        this.keyboard = keyboard;
        this.dest = dest;
    }

    @Override
    public void execute() throws Exception {
        JSONObject object = keyboard.save();
        String data = object.toString(4);
        
        try (BufferedWriter writer = 
                new BufferedWriter(new FileWriter(dest))) {
            writer.write(data);
            writer.flush();
        }
    }

    @Override
    protected DialogFragment newDialog() {
        return new SaveTaskDialog();
    }

    @Override
    protected String getTag() {
        return "saveTaskDialog";
    }
}
