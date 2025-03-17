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
package com.abiddarris.common.android.pm;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.Session;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.abiddarris.common.files.Files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Packages {
    
    private static final String ACTION_INSTALLED = "com.abiddarris.common.android.ACTION_INSTALLED";

    public static String[] getAbiFromPackage(Context context, String packageName) throws NameNotFoundException, IOException {
        return getAbiFromPackage(context, packageName, false);
    }

    public static String[] getAbiFromPackage(Context context, String packageName, boolean firstAbiOnly) throws NameNotFoundException, IOException {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo applicationInfo = manager.getApplicationInfo(packageName, 0);
        Set<String> abis = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(Files.openBufferedInput(applicationInfo.sourceDir))){
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String path = entry.getName();
                String prefix = "lib/";
                if (path.startsWith(prefix)) {
                    int archEnd = path.lastIndexOf("/");
                    abis.add(path.substring(prefix.length(), archEnd == -1 ? path.length() : archEnd));
                    if (firstAbiOnly) {
                        return abis.toArray(new String[0]);
                    }
                }
                zis.closeEntry();
            }
        }
        return abis.toArray(new String[0]);
    }

    public static boolean isInstalled(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            manager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }
    
    public static void installPackage(Context context, File file, @Nullable InstallationCallback callback) throws IOException {
    	try (InputStream stream = new BufferedInputStream(new FileInputStream(file))){
            installPackage(context, stream, callback);
        } 
    }
    
    @TargetApi(21)
    public static void installPackage(Context context, InputStream stream, @Nullable InstallationCallback callback) throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

        SessionParams params = new SessionParams(SessionParams.MODE_FULL_INSTALL);
        int sessionId = packageInstaller.createSession(params);
        Session session = packageInstaller.openSession(sessionId);

        try (OutputStream output = new BufferedOutputStream(session.openWrite("PackageInstaller", 0, -1))) {
            byte[] buffer = new byte[65536];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }

        InstallationListener listener = new InstallationListener();
        InstallationCallback internalCallback = (status, message) -> {
            context.unregisterReceiver(listener);
            session.close();

            if (callback != null) {
                callback.installationResult(status, message);
            }
        };

        listener.setCallback(internalCallback);
        ContextCompat.registerReceiver(
                context, listener, new IntentFilter(ACTION_INSTALLED),
                ContextCompat.RECEIVER_EXPORTED
        );

        Intent intent = new Intent(ACTION_INSTALLED);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        session.commit(pendingIntent.getIntentSender());
    }

    public static boolean isAllowedToInstallPackage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static class RequestInstallPackagePermission extends ActivityResultContract<Void, Boolean> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void unused) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + context. getPackageName()));

            return intent;
        }

        @Override
        public Boolean parseResult(int i, @Nullable Intent intent) {
            return i == RESULT_OK;
        }
    }

    @TargetApi(21)
    private static class InstallationListener extends BroadcastReceiver {

        private InstallationCallback internalCallback;

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -2);
            String message = intent.getExtras().getString(EXTRA_STATUS_MESSAGE);

            if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent confirmIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                context.startActivity(confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            }

            internalCallback.installationResult(status, message);
        }

        public void setCallback(InstallationCallback internalCallback) {
            this.internalCallback = internalCallback;
        }
    }
}
