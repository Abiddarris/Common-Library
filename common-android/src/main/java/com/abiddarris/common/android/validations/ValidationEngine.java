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
package com.abiddarris.common.android.validations;

import android.widget.EditText;

import com.abiddarris.common.android.utils.TextListener;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationEngine {

    private boolean state = true;
    private Map<TextInputLayout, Boolean> states = new HashMap<>();
    private ValidationChangedListener validationChangedListener;

    public void addEditText(TextInputLayout layout, Validator validator) {
        layout.getEditText()
            .addTextChangedListener(TextListener.newTextListener(editable -> {
                String message = validator.isValid(editable.toString());

                boolean error = message != null;
                layout.setErrorEnabled(error);
                layout.setError(error ? message : "");
                    
                states.put(layout, !error);

                checkOverall();
            }));
        states.put(layout, true);
    }

    public boolean isValid() {
        return state;
    }

    public ValidationChangedListener getValidationChangedListener() {
        return this.validationChangedListener;
    }

    public void setValidationChangedListener(ValidationChangedListener validationChangedListener) {
        this.validationChangedListener = validationChangedListener;
    }
    
    private void checkOverall() {
        boolean state = states.values().stream().reduce((b1, b2) -> b1 && b2).orElse(this.state);

        if (this.state == state) {
            return;
        }

        this.state = state;
        
        ValidationChangedListener listener;
        if((listener =getValidationChangedListener()) != null) {
            listener.onChanged(state);
        }
    }
}
