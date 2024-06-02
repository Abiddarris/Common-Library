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
package com.abiddarris.common.android.about;

import static android.widget.LinearLayout.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.abiddarris.common.android.fragments.TextFragment;

/**
 * {@code Fragment} that shows about section
 */
public class AboutFragment extends TextFragment {
    
    private static final String HEADER = "header";
    private static final String ATTRIBUTIONS = "attributions";
    
    public static AboutFragment newAboutFragment(String header, Attribution[] attributions) {
        var fragment = new AboutFragment();
        var args = new Bundle();
        
        args.putString(HEADER, header);
        args.putParcelableArray(ATTRIBUTIONS, attributions);
        
        fragment.setArguments(args);
        
        return fragment;
    }
    
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
        String header = getArguments().getString(HEADER);
        Attribution[] attributions = (Attribution[])getArguments()
            .getParcelableArray(ATTRIBUTIONS);
        
        RecyclerView attributionList = new RecyclerView(getContext());
        AttributionAdapter adapter = new AttributionAdapter(getContext(), attributions);
                
        attributionList.setNestedScrollingEnabled(false);
        attributionList.setAdapter(adapter);    
        attributionList.setLayoutManager(new LinearLayoutManager(getContext()));     
        
        setText(header);
        
        getBinding().scrollViewChild
            .addView(attributionList, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
    
}
