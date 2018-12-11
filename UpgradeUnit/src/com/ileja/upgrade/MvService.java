package com.ileja.upgrade;

import java.io.File;
import java.io.IOException;

import com.ileja.upgrade.util.AILog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MvService extends Service {
	private static final String TAG = "MvService";

	private Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		AILog.i(TAG, "onCreate()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AILog.i(TAG, "Received start id " + startId + ": " + intent);
		runScript();
		return START_NOT_STICKY;
	}

	private void runScript() {
		if (Utils.isFileExist(Utils.PREDEFINE_SCRIPT_FILE)) {
			File file = new File(Utils.PREDEFINE_SCRIPT_FILE);
			file.setExecutable(true, false);
			Process proc = null;
			try {
				proc = Runtime.getRuntime().exec("sh " + Utils.PREDEFINE_SCRIPT_FILE);
				proc.waitFor();
				AILog.i(TAG, "[NOTE] Exec: " + Utils.PREDEFINE_SCRIPT_FILE);
				
			} catch (IOException e) {
				e.printStackTrace();
				AILog.e(TAG, "IOException : " + e.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if(null != proc){
					try {
						proc.getOutputStream().close();
						proc.getInputStream().close();
						proc.getErrorStream().close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						proc.destroy();
					}
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
