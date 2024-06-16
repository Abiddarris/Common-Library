package com.abiddarris.common.android.pm;

import static android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.widget.Toast;

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