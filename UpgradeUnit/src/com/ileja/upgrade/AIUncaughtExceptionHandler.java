package com.ileja.upgrade;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.ileja.upgrade.util.AILog;
import com.ileja.upgrade.util.FileUtil;
import com.ileja.upgrade.util.PmInstallHelper;

public class AIUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = AIUncaughtExceptionHandler.class.getSimpleName();

	@SuppressWarnings("unused")
	private Context mContext;
	public static AIUncaughtExceptionHandler instance;
	
	public AIUncaughtExceptionHandler(Context context) {
		mContext = context;
	}

    public synchronized static AIUncaughtExceptionHandler getInstance(Context context) {
        if (instance == null) {
            instance = new AIUncaughtExceptionHandler(context);
        }
        return instance;
    }
	
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    
	private void exit() {
		AILog.e(TAG, "-------------------exit step1----------------");
		
		try {
			FileUtil.delFolder(MainActivity.BASE_PATH + MainActivity.UPGRADE_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			AILog.e(TAG, "exit delFolder : " + e.toString());
		}
		
		PmInstallHelper.getIns().shutDown(UpgradeApp.getContext());
//		try {
//			Intent intent = new Intent();
//			intent.setComponent(new ComponentName("com.ileja.carrobot","com.ileja.carrobot.activity.SplashActivity"));
//			UpgradeApp.getContext().startActivity(intent);
//		} catch (Exception e) {
//			AILog.e(TAG, "exit e : " + e.toString());
//		} finally{
//			AILog.e(TAG, "-------------------exit step2----------------");
//			AILog.e(TAG, "-------------------exit step3----------------");
//			android.os.Process.killProcess(android.os.Process.myPid());
//		}
	}
    
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		String stackTrace = getStackTrace(ex);
		AILog.e(TAG, stackTrace);
		exit();
	}

	public static String getStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
}