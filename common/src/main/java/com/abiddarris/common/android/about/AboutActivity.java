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

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.abiddarris.common.R;
import com.abiddarris.common.databinding.ActivityAboutBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for showing about and attributions from assets
 */
public class AboutActivity extends AppCompatActivity {
    
    private static final String ABOUT_FILE_NAME = "about_file_name";
    private static final String ATTRIBUTION_FILE_NAME = "attribution_file_name";
    
    private ActivityAboutBinding binding;
    
    public static Intent newAboutActivity(Context context, String aboutFileName, String attributionFileName) {
        var intent = new Intent(context, AboutActivity.class);
        intent.putExtra(ABOUT_FILE_NAME, aboutFileName);
        intent.putExtra(ATTRIBUTION_FILE_NAME, attributionFileName);
        
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        setTitle(R.string.about);
         
        if(savedInstanceState != null) {
            return;
        }
        
        Bundle extras = getIntent().getExtras();
        String aboutFileName = extras.getString(ABOUT_FILE_NAME);
        String attributionFileName = extras.getString(ATTRIBUTION_FILE_NAME);
        
        if(aboutFileName == null) {
            throw new IllegalArgumentException("aboutFileName cannot be null.");
        }
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                loadText(aboutFileName, attributionFileName);
            } finally {
                executor.shutdown();
            }
        });
    }
    
    private void loadText(String aboutFileName, String attributionFileName) {
        AssetManager assets = getAssets();
        StringBuilder aboutText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(assets.open(aboutFileName)))) {
                    
            reader.lines()
                .map(text -> text + "\n")
                .forEach(aboutText::append);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
                
        StringBuilder attributionsText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(assets.open(attributionFileName)))) {
            
            reader.lines()
                .map(text -> text + "\n")     
                .forEach(attributionsText::append);   
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
                
        Attribution[] attributions = Attribution.parse(attributionsText.toString());
        AboutFragment fragment = AboutFragment.newAboutFragment(aboutText.toString(), attributions);
        
        runOnUiThread(() -> {
            getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.about_fragment, fragment, null)
                .commit();
        });
    }
    
}
