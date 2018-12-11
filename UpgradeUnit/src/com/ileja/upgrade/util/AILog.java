package com.ileja.upgrade.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.ileja.upgrade.MainActivity;
import com.ileja.upgrade.UpgradeApp;

public class AILog {
	// quite dangerous to rely on BuildConfig.DEBUG and ADT to do the right
	// thing,
	// because there are bugs in the build system
	// that cause exported signed release builds to be built with
	// BuildConfig.DEBUG set to true!
	public static final boolean IS_DEBUG = true;// BuildConfig.DEBUG

	public static void v(String tag, String msg) {
		if (IS_DEBUG) {
			Log.v(tag, msg);
		}
		writeFile(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (IS_DEBUG) {
			Log.d(tag, msg);
		}
		writeFile(tag, msg);
	}

	public static void i(String tag, String msg) {
		if (IS_DEBUG) {
			Log.i(tag, msg);
		}
		writeFile(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (IS_DEBUG) {
			Log.w(tag, msg);
		}
		writeFile(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (IS_DEBUG) {
			Log.e(tag, msg);
		}
		writeFile(tag, msg);
	}

	public static void writeFile(String tag, String msg) {
		FileUtil.saveStringToFile(
				getExternalCacheDir(UpgradeApp.getContext())
						+ MainActivity.AILOG_FILE,sdf.format(System.currentTimeMillis())+" (" + tag + ") " + msg+"\n", true);
	}
	
	 public static File getExternalCacheDir(Context paramContext){
	    if ((Build.VERSION.SDK_INT >= 8) && ("mounted".equals(Environment.getExternalStorageState())))
	      return paramContext.getExternalCacheDir();
	    return null;
	  }

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"MM-dd HH:mm:ss.SSS", Locale.US);
}