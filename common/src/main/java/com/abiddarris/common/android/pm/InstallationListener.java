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
import static android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;

@TargetApi(21)
class InstallationListener extends BroadcastReceiver {
    
    private int status = -1;
    private String message;
    
    int getStatus() {
        return status;
    }
    
    String getMessage() {
        return message;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -2);
        String message = intent.getExtras().getString(EXTRA_STATUS_MESSAGE);
        
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent confirmIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                context.startActivity(confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            default : 
                this.status = status;
                this.message = message;
            
                synchronized(this) {
                    notifyAll();
                }
        }
    }

}