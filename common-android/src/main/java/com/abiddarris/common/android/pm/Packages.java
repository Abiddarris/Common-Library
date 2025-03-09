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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.Session;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.PackageInfoFlags;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Packages {
    
    private static final String ACTION_INSTALLED = "com.abiddarris.common.android.ACTION_INSTALLED";
    
    public static boolean isInstalled(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            manager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }
    
    public static InstallationResult installPackage(Context context, File file) throws IOException {
    	try (InputStream stream = new BufferedInputStream(new FileInputStream(file))){
            return installPackage(context, stream);
        } 
    }
    
    @TargetApi(21)
    public static InstallationResult installPackage(Context context, InputStream stream) throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager()
            .getPackageInstaller();
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
        try {
            context.registerReceiver(listener, new IntentFilter(ACTION_INSTALLED));

            Intent intent = new Intent(ACTION_INSTALLED);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            
            session.commit(pendingIntent.getIntentSender());
            
            synchronized(listener) {
                if(listener.getStatus() == -1) {
                    try {
                        listener.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return new InstallationResult(
                listener.getStatus(), listener.getMessage());
        } finally {
            context.unregisterReceiver(listener);
        }
        
    }

    public static boolean isAllowedToInstallPackage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

}
