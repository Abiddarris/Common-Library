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

import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.KEYS;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.abiddarris.common.R;
import com.abiddarris.common.android.view.MoveableViewsGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VirtualKeyboard extends MoveableViewsGroup {
    
    private Button editButton;
    private Key focus;
    private List<Key> keys = new ArrayList<>();
    private KeyListener listener;
    
    public VirtualKeyboard(Context context) {
        super(context);
        
        editButton = new Button(getContext());
        editButton.setVisibility(GONE);
        editButton.setText(R.string.edit);
        editButton.setOnClickListener(v -> {
            var dialog = EditButtonDialog.newInstance(focus);
            dialog.show(((FragmentActivity)getContext()).getSupportFragmentManager(), null);
        });
        
        addView(editButton);
    }
    
    @Override
    protected boolean onChildTouch(View view, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                focus = null;
            
                editButton.setVisibility(GONE);
                break;
            case MotionEvent.ACTION_UP :
                focus = keys.stream()
                    .filter(key -> key.getButton() == view)
                    .findFirst()
                    .get();
            
                editButton.setVisibility(VISIBLE);
                editButton.setX(view.getX() + 
                                view.getWidth() / 2 -
                                editButton.getWidth() / 2);
                editButton.setY(view.getY() + view.getHeight());
                editButton.bringToFront();
        }
        return super.onChildTouch(view, event);
    }
    
    @Override
    public void setEdit(boolean edit) {
        super.setEdit(edit);
        
        if(!edit) {
            editButton.setVisibility(GONE);
        }
    }
    
    protected void sendKeyEvent(Event event, int keycode) {
        var listener = getKeyListener();
        if(listener != null) {
            listener.onKey(event, keycode);
        }
    }
    
    public Key addButton() {
        Key key = new Key();
        key.init(getContext());
        
        keys.add(key);
        
        addMoveableView(
            key.getButton(), 
            new LayoutParams(100, 100),
            new TouchListener(this, key)
        );
        
        return key;
    }
    
    public void removeButton(Key key) {
        removeView(key.getButton());
      
        keys.remove(key);
    }
    
    public KeyListener getKeyListener() {
        return this.listener;
    }
    
    public void setKeyListener(KeyListener listener) {
        this.listener = listener;
    }
    
    public JSONObject save() throws JSONException {
        JSONObject header = new JSONObject();
        JSONArray array = new JSONArray();
        
        for(var key : keys) {
        	array.put(key.save());
        }
        
        header.put(KEYS, array);
        
        return header;
    }
    
    public void clearKeys() {
        for(Key key : keys) {
        	removeButton(key);
        }
    }
    
    public void load(JSONObject keyboard) throws JSONException {
        clearKeys(); 
        
        JSONArray keys = keyboard.getJSONArray(KEYS);
        for(int i = 0; i < keys.length(); ++i) {
        	Key key = addButton();
            JSONObject keyJSON = keys.getJSONObject(i);
            
            key.load(keyJSON);
        }
    }
}
