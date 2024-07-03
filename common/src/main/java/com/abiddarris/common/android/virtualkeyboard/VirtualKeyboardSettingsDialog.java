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

import android.os.Bundle;

import android.widget.Toast;
import com.abiddarris.common.R;
import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.abiddarris.common.android.tasks.TaskViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.stream.Stream;

public class VirtualKeyboardSettingsDialog extends BaseDialogFragment<Void> {

    private static final String OPTIONS = "options";
    
    static VirtualKeyboardSettingsDialog newInstance(VirtualKeyboardOptions options) {
        var dialog = new VirtualKeyboardSettingsDialog();
        dialog.saveVariable(OPTIONS, options);
        
        return dialog;
    }
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
        
        builder.setItems(R.array.virtual_keyboard_settings_choices, (dialog, which) -> {
            switch(which) {
                case 0 :
                    save();
                    return;
                case 1 :
                    load();
            }
        });
    }
    
    private void save() {
        VirtualKeyboardOptions options = getVariable(OPTIONS);
        var dialog = SaveKeyboardDialog.newInstance(options.getDefaultSaveName());
        
        dialog.showForResult(getParentFragmentManager(), (name) -> {
            if(name == null) return;
            options.setDefaultSaveName(name);
            
            TaskViewModel model = TaskViewModel.getInstance(dialog.getActivity());
            model.execute(new SaveTask(
                        options.getKeyboard(),
                        new File(options.getKeyboardFolderPath(), name + ".json")));
        });
    }
    
    private void load() {
        VirtualKeyboardOptions options = getVariable(OPTIONS);
        File[] keyboards = new File(options.getKeyboardFolderPath())
            .listFiles(file -> {
                return !file.isDirectory() && file.getName().endsWith(".json");
            });
        
        var dialog = new LoadKeyboardSelectorDialog();
        dialog.setItems(
            Stream.of(keyboards)
                .map(File::getName)
                .toArray(String[]::new),
            -1
        );
        dialog.showForResult(getParentFragmentManager(), (index) -> {
            if(index == -1) return;
                
            TaskViewModel model = TaskViewModel.getInstance(dialog.getActivity());
            model.execute(new LoadTask(
                        options.getKeyboard(),
                        keyboards[index]));
        });
    }
}
