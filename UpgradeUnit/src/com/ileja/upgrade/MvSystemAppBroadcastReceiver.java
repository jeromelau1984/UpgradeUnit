package com.ileja.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ileja.upgrade.util.AILog;

public class MvSystemAppBroadcastReceiver extends BroadcastReceiver{
    private static final String TAG = "MvSystemAppBroadcastReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        AILog.d(TAG, "onReceive(), action:" + action);
        if ("android.ileja.upgrade.mvsystemapp".equals(action)) {
        	if (Utils.isFileExist(Utils.PREDEFINE_SCRIPT_FILE)) {
        		startMvService(context);
        	}
        }
    }
    
    private void startMvService(Context context) {
        Intent cService = new Intent(context, MvService.class);
        context.startService(cService);
    }
}
