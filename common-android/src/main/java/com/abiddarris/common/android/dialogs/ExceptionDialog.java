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
package com.abiddarris.common.android.dialogs;

import android.os.Bundle;

import com.abiddarris.common.android.R;
import com.abiddarris.common.android.fragments.TextFragment;
import com.abiddarris.common.utils.Exceptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * {@code Dialog} that will show a throwable.
 *
 * @since 1.0
 * @author Abiddarris
 */
public class ExceptionDialog<Result> extends FragmentDialog<Result> {
    
    /**
     * Identifier for throwable that will be shown
     */
    private static final String THROWABLE = "throwable";
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
        
        builder.setTitle(R.string.exception_dialog_title)
            .setPositiveButton(android.R.string.ok, null);
        
        if(savedInstanceState != null) return;
        
        var fragment = new TextFragment();
        fragment.setHighlightLink(false);
        fragment.setScrollableHorizontally(true);

        updateUI(fragment);
        setFragment(fragment);
    }
    
    /**
     * Set {@code Throwable} that will be shown to user.
     *
     * <p>This method can be called even when dialog 
     * not attached.
     *
     * @param throwable Throwable to show
     * @since 1.0
     */
    public void setThrowable(Throwable throwable) {
        saveVariable(THROWABLE, throwable);
        
        TextFragment fragment = getFragment();
        if(fragment != null) {
            updateUI(fragment);
        }
    }
    
    /**
     * Returns throwable that will be shown to user.
     *
     * @return throwable that will be shown to user.
     * @since 1.0
     */
    public Throwable getThrowable() {
        return getVariable(THROWABLE);
    }
    
    private void updateUI(TextFragment fragment) {
        String exceptionText = Exceptions.toString(getThrowable());
        
        fragment.setText(exceptionText);
    }
}
