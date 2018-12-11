package com.ileja.upgrade.tool;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ileja.upgrade.util.AILog;

public class DeleteOutSDcardReveiver extends BroadcastReceiver {
	private static final String TAG = "DeleteOutSDcardReveiver";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		if(intent.getAction().equals("com.ileja.action.DELECT_OUT_SDCARD_FILE")){
			String path = (String) intent.getExtra("FilePath");
			File deleteFile =new File(path);
			if(deleteFile.exists()){
				deleteFile.delete();
				AILog.i(TAG,"deleteFile.delete() is finish!");
			}else{
				AILog.i(TAG,"deleteFile.exists() is false!");
			}
		}
	}

}
