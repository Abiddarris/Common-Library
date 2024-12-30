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

import android.os.Bundle;
import android.view.View;
import com.abiddarris.common.android.fragments.TextFragment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@code Fragment} that will show license text
 */
public class LicenseFragment extends TextFragment {
    
    private static final String ATTRIBUTION = "attribution";
    
    public static LicenseFragment newLicenseFragment(Attribution attribution) {
        var bundle = new Bundle();
        bundle.putParcelable(ATTRIBUTION, attribution);
        
        var fragment = new LicenseFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
    
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
        if(bundle != null) {
            return;
        }
        
        ExecutorService executors = Executors.newSingleThreadExecutor();
        Attribution attribution = getArguments().getParcelable(ATTRIBUTION);
        
        executors.submit(() -> {
            try {
                loadText(attribution);
            } catch (IOException e) {
                e.printStackTrace();
                    
                getActivity().finish();    
            } finally {
                executors.shutdown();
            }
        });
    }
    
    private void loadText(Attribution attribution) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getContext().getAssets().open(attribution.getLicenseTextAssets())))) {
            
            reader.lines()
                .map(line -> line + "\n")
                .forEach(builder::append);
        }
        
        getActivity().runOnUiThread(() -> setText(builder.toString()));
    }
}
