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

import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.ALIGNMENT;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.KEYCODE;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.NAME;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.SIZE;
import static com.abiddarris.common.android.virtualkeyboard.Keycode.valueOf;
import static org.json.JSONObject.NULL;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;

import com.abiddarris.common.android.databinding.LayoutButtonBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class Key {

    private Alignment alignment = new Alignment(this);
    private Button button;
    private int id;
    private Keycode keycode;
    private Size size = new Size(this);
    
    protected JSONObject save() throws JSONException {
        var keycode = getKeycode();
        
        JSONObject key = new JSONObject();
        key.put(NAME, button.getText());
        key.put(KEYCODE, keycode != null ? keycode.name() : JSONObject.NULL);
        key.put(ALIGNMENT, alignment.save());
        key.put(SIZE, size.save());
        
        return key;
    }
    
    void load(JSONObject object) throws JSONException {
        getButton().setText(
            object.getString(NAME)
        );
        Object keycode = object.get(KEYCODE);
        this.keycode = keycode == NULL ? null : valueOf(String.valueOf(keycode));
        
        size.load(object.getJSONObject(SIZE));
        alignment.load(object.getJSONObject(ALIGNMENT));
    }
    
    public void init(Context context) {
        button = LayoutButtonBinding.inflate(LayoutInflater.from(context)).getRoot();
    }
    
    public Button getButton() {
        return button;
    }
    
    public Alignment getAlignment() {
        return alignment;
    }
    
    public Size getSize() {
        return size;
    }
    
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Keycode getKeycode() {
        return this.keycode;
    }

    public void setKeycode(Keycode keycode) {
        this.keycode = keycode;
    }
    
}
