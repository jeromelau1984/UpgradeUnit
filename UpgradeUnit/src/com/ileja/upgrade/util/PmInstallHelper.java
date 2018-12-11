package com.ileja.upgrade.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public final class PmInstallHelper {

	private static final String TAG = PmInstallHelper.class.getSimpleName();
	
	private static final String BASE_PATH = "/storage/sdcard0";
	protected static final String UPGRADE_FILE = "/carrobotUpgrade/";
	private AtomicInteger mCount = new AtomicInteger(0);
	
	private volatile static PmInstallHelper mInstance;

	public static PmInstallHelper getIns() {
		PmInstallHelper result = mInstance;
		if(result == null){
			synchronized (PmInstallHelper.class) {
				result = mInstance;
				if(result == null){
					result = mInstance = new PmInstallHelper();
				}
			}
		}
		return result;
	}
	
	public void shutDown(Context mActivity){
		if(null == mActivity) throw new IllegalArgumentException("shutDown mActivity must not be null...");
		Intent shutdownIntent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        shutdownIntent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        shutdownIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(shutdownIntent);
	}
	
	/**
	 * multi thread for install
	 */
//	private void stepSuInstall() {
//		Log.e(TAG, "stepSuInstall...");
//		for (final String appName : apkContainer) {
//			new Thread() {
//				public void run() {
//					StringBuffer sb = new StringBuffer(BASE_PATH);
//					sb.append(UPGRADE_FILE).append(appName);
//					file = new File(sb.toString());
//					Log.d(TAG, "stepSuInstall sb : " + sb.toString() + " ; file.exists() : " + file.exists());
//					if (file.exists()) {
//						String pmResult = installOnBackgroundByPM(sb.toString());
//						Log.i(TAG, "installOnBackgroundByPM pmResult : " + pmResult);
//						
//						if(!TextUtils.isEmpty(pmResult)){
//							mCount.addAndGet(1);
//						}
//					}
//				};
//			}.start();
//		}
//	}
	
	/**
	 * 
	 * @param appName /storage/sdcard0/carrobotUpgrade/fileName ; such as CarrobotMain_v1.apk
	 */
	protected void installSingle(String appName) {
		if(TextUtils.isEmpty(appName)) throw new IllegalArgumentException("appName must not be null...");
		StringBuffer sb = new StringBuffer(BASE_PATH);
		sb.append(UPGRADE_FILE).append(appName);
		File file = new File(sb.toString());
		Log.d(TAG, "stepSuInstall sb : " + sb.toString() + " ; file.exists() : " + file.exists());
		if (file.exists()) {
			String pmResult = installOnBackgroundByPM(sb.toString());
			Log.i(TAG, "installOnBackgroundByPM pmResult : " + pmResult);

			if (!TextUtils.isEmpty(pmResult)) {
				mCount.addAndGet(1);
			}
		}
	}

	/**
	 * 
	 * @param apkAbsolutePath /storage/sdcard0/carrobotUpgrade/fileName
	 * @return when return means the child process is terminated
	 */
	private String installOnBackgroundByPM(String apkAbsolutePath) {
		String[] args = { "pm", "install", "-r", apkAbsolutePath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG,"installOnBackgroundByPM IOException : " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "installOnBackgroundByPM Exception : " + e.toString());
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "installOnBackgroundByPM finally IOException : " + e.toString());
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
}
