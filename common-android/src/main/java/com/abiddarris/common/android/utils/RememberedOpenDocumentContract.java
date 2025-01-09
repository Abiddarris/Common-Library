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
package com.abiddarris.common.android.utils;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.ACTION_OPEN_DOCUMENT;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A custom implementation of the {@link ActivityResultContract} that allows opening documents and
 * remembering the last opened document URI across app sessions.
 *
 * This contract is used for launching a document picker intent, where the user can select one or
 * more documents, and the last selected document's URI is remembered and pre-populated for the next
 * document selection. It saves the URI using SharedPreferences.
 *
 * <p>Requires API level 19 (KITKAT) or higher for the document picker intent.</p>
 *
 * @see ActivityResultContract
 */
public class RememberedOpenDocumentContract extends ActivityResultContract<Void, List<Uri>> {

    private final Context context;
    private final String key;

    /**
     * Creates a new instance of the {@link RememberedOpenDocumentContract}.
     *
     * @param context The context of the application or activity. This is used for accessing SharedPreferences.
     * @param key The key used for storing and retrieving the last opened document URI from SharedPreferences.
     */
    public RememberedOpenDocumentContract(Context context, String key) {
        this.context = context;
        this.key = key;
    }

    /**
     * Creates an intent for the document picker, with the option to pre-populate it with the last opened document URI.
     *
     * <p>This method creates an intent that launches a document picker. If a URI was previously remembered,
     * it will be set as the initial URI in the intent to guide the user to the most recently opened document.</p>
     *
     * <p>Override this method if you need to customize the returned Intent
     *
     * @param context The context to use for creating the intent.
     * @param unused Unused parameter, as this contract doesn't require input.
     * @return The intent used to launch the document picker.
     * @see Intent
     * @see DocumentsContract#EXTRA_INITIAL_URI
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {
        Intent intent = new Intent(ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");

        Uri uri = getLastOpenedUri();
        if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        }

        return intent;
    }

    @Override
    public final List<Uri> parseResult(int code, @Nullable Intent intent) {
        if (code != RESULT_OK || intent == null) {
            return null;
        }

        Set<Uri> uris = new LinkedHashSet<>();
        if (intent.getData() != null) {
            uris.add(intent.getData());
        }


        ClipData clipData = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? intent.getClipData() : null;
        if (clipData == null) {
            ArrayList<Uri> uriList = new ArrayList<>(uris);
            if (!uriList.isEmpty()) {
                setLastUriOpened(uriList.get(0));
            }
            return uriList;
        }

        for (int i = 0; i < clipData.getItemCount(); i++) {
            uris.add(clipData.getItemAt(i).getUri());
        }

        ArrayList<Uri> uriList = new ArrayList<>(uris);
        if (!uriList.isEmpty()) {
            setLastUriOpened(uriList.get(0));
        }

        return uriList;

    }

    private Uri getLastOpenedUri() {
        String uriString = context.getSharedPreferences("remembered_open_document_contract", MODE_PRIVATE)
                .getString(key, null);
        return uriString == null ? null : Uri.parse(uriString);
    }

    private void setLastUriOpened(Uri uri) {
        context.getSharedPreferences("remembered_open_document_contract", MODE_PRIVATE)
                .edit()
                .putString(key, uri.toString())
                .apply();
    }
}