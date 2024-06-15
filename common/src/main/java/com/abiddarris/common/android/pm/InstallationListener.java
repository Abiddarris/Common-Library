package com.abiddarris.common.android.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.widget.Toast;

class InstallationListener extends BroadcastReceiver {
    
    private boolean received;
    
    public boolean isReceived() {
        return this.received;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -2);
        
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                Intent confirmIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                context.startActivity(confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            default : 
                received = true;
                synchronized(this) {
                    notifyAll();
                }
        }
    }

}