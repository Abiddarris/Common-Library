package com.abiddarris.common.android.preferences;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class DialogCommunicator extends ViewModel {
    
    private List<DialogPreference> preferences = new ArrayList<>();
    
    void add(DialogPreference preference) {
        preferences.add(preference);
    }
    
    DialogPreference find(String key) {
        for(DialogPreference preference : preferences) {
            if(preference.getKey().equals(key)) {
                return preference;
            }
        }
        return null;
    }
    
    void clear() {
        preferences.clear();
    }
    
}
