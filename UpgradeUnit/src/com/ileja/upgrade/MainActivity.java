package com.ileja.upgrade;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ProgressBar;

import com.ileja.upgrade.util.AILog;
import com.ileja.upgrade.util.DeviceInfo;
import com.ileja.upgrade.util.FileUtil;
import com.ileja.upgrade.util.PmInstallHelper;
import com.ileja.upgrade.util.WakeLockHelper;
import android.os.SystemProperties;
import android.util.Log;
import android.os.PowerManager.WakeLock;

/**
 * 升级模块 多线程安装/storage/sdcard0/carrobotUpgrade/目录下apk文件，如某个apk安装失败，
 * 即认为该apk是低版本或已损坏文件，尝试安装所有apk后，将/data/carrobot/usr目录删除并重启
 * 
 * @author jerome
 *
 */
public class MainActivity extends Activity implements Runnable {

	private static final String TAG = "CarrobotUpgradeUnit";

	private InstalledReceiver installedReceiver;
	
    public static final boolean IS_WRITE_AILOG_TO_FILE = true;
    public static final String AILOG_FILE = "launcherAILog.txt";
	
	protected static final String POSTFIX = ".apk";
//	private static final String BASE_PATH = "/storage/sdcard0";
//	protected static final String UPGRADE_FILE = "/carrobotUpgrade/";
	protected static final String BASE_PATH = "/data/carrobot";
	protected static final String UPGRADE_FILE = "/usr/";
	private static final String RM_FLAG = "rmSemaphore.flag";

	private File file;
	private Vector<String> apkContainer;

	private AtomicInteger mCount = new AtomicInteger(0);

	private Thread heartbeadThread;
	private boolean stop = false;
	
	private static int totalSize = 0;
	private ProgressBar pb;

	private static final String LEJIA_APP_UPGRADE_RUNNING = "lejia_app_upgrade_running";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemProperties.set(LEJIA_APP_UPGRADE_RUNNING, "1");
		Log.i(TAG, "[Jerome_append_upgrade] onCreate getprop : " + SystemProperties.get(LEJIA_APP_UPGRADE_RUNNING));
		registReceiver();
		setContentView(R.layout.activity_main);
		WakeLockHelper.acquireBrightWakeLock(UpgradeApp.getContext());
		init();
	}
	
	private void init() {
		try {
			apkContainer = GetApkFileName(BASE_PATH + UPGRADE_FILE);
		} catch (Exception e) {
			AILog.e(TAG, "init GetApkFileName Exception : " + e.toString());
			exit();
		}
		
		AILog.e(TAG, "init apkContainer : " + apkContainer.size());
		if (apkContainer.size() > 0) {
			if (heartbeadThread == null) {
				heartbeadThread = new Thread(this);
				heartbeadThread.start();
			}
			stepSuInstall();
			initPb();
		} else {
			exit();
		}
	}
	
	/**
	 * 根据cpu数量、主频、总文件大小 , 估算pm安装时间 UI展示 , 经验值
	 */
	private void initPb(){
		mProgress = 0;
		int cpuCores = DeviceInfo.getNumberOfCPUCores();
		int cpuFreq = DeviceInfo.getCPUMaxFreqKHz();
		totalSize = (cpuCores > 0 && cpuFreq > 0) ? totalSize/(cpuCores*cpuFreq) : 0;
		totalSize *= 2;
		AILog.i(TAG, "initPb totalSize : " + totalSize);
		if(null == pb) pb = (ProgressBar) findViewById(R.id.pb);
		if(null != pb) pb.setMax(100);
	}

	private void stepSuInstall() {
		AILog.e(TAG, "stepSuInstall...");
		for (final String appName : apkContainer) {
			new Thread() {
				public void run() {
					StringBuffer sb = new StringBuffer(BASE_PATH);
					sb.append(UPGRADE_FILE).append(appName);
					file = new File(sb.toString());
					AILog.d(TAG, "stepSuInstall sb : " + sb.toString() + " ; file.exists() : " + file.exists());
					if (file.exists()) {
						String pmResult = installOnBackgroundByPM(sb.toString());
						AILog.i(TAG, "installOnBackgroundByPM pmResult : " + pmResult);
						
						if(!TextUtils.isEmpty(pmResult)){
							mCount.addAndGet(1);
						}
					}
				};
			}.start();
		}
	}

	private void exit() {
		
		try {
			FileUtil.delFolder(BASE_PATH + UPGRADE_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			AILog.e(TAG, "exit delFolder : " + e.toString());
		}
		
		 Message msg = new Message();
		 msg.what = MSG_UPDATE_UI;
		 msg.arg1 = 100;
		 if(null != mHandler) mHandler.sendMessage(msg);
		
		 
		AILog.e(TAG, "-------------------exit step1----------------");

		SystemProperties.set(LEJIA_APP_UPGRADE_RUNNING, "0");
		Log.i(TAG, "[Jerome_append_upgrade] exit step1 getprop : " + SystemProperties.get(LEJIA_APP_UPGRADE_RUNNING));
		WakeLockHelper.releaseWakeLock();

		PmInstallHelper.getIns().shutDown(UpgradeApp.getContext());
//		try {
//			Intent intent = new Intent();
//			intent.setComponent(new ComponentName("com.ileja.carrobot","com.ileja.carrobot.activity.SplashActivity"));
//			startActivity(intent);
//		} catch (Exception e) {
//			AILog.e(TAG, "exit e : " + e.toString());
//		} finally{
//			AILog.e(TAG, "-------------------exit step2----------------");
//			finish();
//			AILog.e(TAG, "-------------------exit step3----------------");
//			android.os.Process.killProcess(android.os.Process.myPid());
//			PmInstallHelper.getIns().shutDown(UpgradeApp.getContext());
//		}
	}

	private void registReceiver() {
		installedReceiver = new InstalledReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PACKAGE_ADDED");
		filter.addAction("android.intent.action.PACKAGE_REMOVED");
		filter.addDataScheme("package");
		registerReceiver(installedReceiver, filter);
	}

	public String installOnBackgroundByPM(String apkAbsolutePath) {
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
			AILog.e(TAG, "installOnBackgroundByPM IOException : " + e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			AILog.e(TAG, "installOnBackgroundByPM Exception : " + e.toString());
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
				AILog.e(TAG, "installOnBackgroundByPM finally IOException : " + e.toString());
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public static Vector<String> GetApkFileName(String fileAbsolutePath) {
		Vector<String> vecFile = new Vector<String>();
		if (TextUtils.isEmpty(fileAbsolutePath))
			return vecFile;

		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();
		if (null == file || null == subFile || subFile.length == 0)
			return vecFile;
		for (int iFileLength = 0, count = subFile.length; iFileLength < count; iFileLength++) {
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getName();
				if (filename.trim().toLowerCase().endsWith(POSTFIX)) {
					if(filename.contains("LejiaUpgrade")) continue;
					if(!vecFile.contains(filename) && subFile[iFileLength].length() != 0){
						AILog.i(TAG, "GetApkFileName file.length() : " + subFile[iFileLength].length());
						totalSize += subFile[iFileLength].length();
						vecFile.add(filename);
					}
				}
			}
		}
		return vecFile;
	}

	@Override
	public void onDestroy() {
		stop = true;
		if(null != pb) pb = null;
		if (installedReceiver != null) {
			unregisterReceiver(installedReceiver);
			installedReceiver = null;
		}
		if (null != apkContainer) {
			apkContainer.clear();
			apkContainer = null;
		}
		if (null != heartbeadThread) {
			heartbeadThread.interrupt();
			heartbeadThread = null;
		}
		if (null != file)
			file = null;
		if(null != mHandler){
//			Looper.myLooper().quitSafely();
//			mHandler.getLooper().quitSafely();
//			mHandler = null;
		}
		SystemProperties.set(LEJIA_APP_UPGRADE_RUNNING, "0");
		Log.i(TAG, "[Jerome_append_upgrade] onDestroy getprop : " + SystemProperties.get(LEJIA_APP_UPGRADE_RUNNING));
		WakeLockHelper.releaseWakeLock();
		super.onDestroy();
	}

	public class InstalledReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
				String packageName = intent.getDataString();
				if (packageName.contains(":")) {
					packageName = packageName.replace("package:", "");
				}

				AILog.i(TAG, "InstalledReceiver onReceive packageName : "
						+ packageName);

//				if (null != apkContainer && apkContainer.size() > 0) {
//					mCount.addAndGet(1);
//					AILog.i(TAG, "installed : " + packageName + " ; mCount : "
//							+ mCount + " ; apkContainer.size() : "
//							+ apkContainer.size());
//				}

//				if (mCount.get() == apkContainer.size()) {
//					AILog.i(TAG, "mCount == apkContainer.size()");
//					try {
//						FileUtil.delFolder(BASE_PATH + UPGRADE_FILE);
//					} catch (Exception e) {
//						e.printStackTrace();
//						AILog.e(TAG, "delFolder Exception : " + e.toString());
//					}
//				}
			}

			if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
				String packageName = intent.getDataString();
				AILog.i(TAG, "uninstalled :" + packageName);
			}
		}
	}

	@Override
	public void run() {
		Timer timer = new Timer();
		timer.schedule(new HeartBeatTask(), 1000, 1000);
		while (stop) {
			timer.cancel();
			timer = null;
		}
	}
	
	private void sendPbMsg(){
		int finalProgress = totalSize > 0 ? 100*mProgress/totalSize : 0;
		if(finalProgress <= 100){
			Message msg = new Message();
			msg.what = MSG_UPDATE_UI;
			msg.arg1 = finalProgress;
			if(null != mHandler) mHandler.sendMessage(msg);
		}
	}
	
	private void delFolderLogic(){
		File file = new File(BASE_PATH + UPGRADE_FILE);
		File[] subFile = file.listFiles();
		 AILog.i(TAG,"HeartBeatTask...(null == subFile || subFile.length == 0) : " + (null == subFile || subFile.length == 0));
		
//			if (mCount.get() == apkContainer.size()) {
//				AILog.i(TAG, "HeartBeatTask mCount == apkContainer.size()");
//				try {
//					FileUtil.delFolder(BASE_PATH + UPGRADE_FILE);
//				} catch (Exception e) {
//					e.printStackTrace();
//					AILog.e(TAG, "delFolder Exception : " + e.toString());
//				}
//				exit();
//				stop = true;
//			}
		 
		 if (null == subFile || subFile.length == 0 || mCount.get() == apkContainer.size()) {
			AILog.i(TAG, "HeartBeatTask (null == subFile || subFile.length == 0)");
//			try {
//				FileUtil.delFolder(BASE_PATH + UPGRADE_FILE);
//			} catch (Exception e) {
//				e.printStackTrace();
//				AILog.e(TAG, "HeartBeatTask delFolder Exception : " + e.toString());
//			}
			exit();
			stop = true;
		}
	}

	class HeartBeatTask extends java.util.TimerTask {

		@Override
		public void run() {
			sendPbMsg();
			delFolderLogic();
		}
	}
	
	private static final int MSG_UPDATE_UI = 0x01;
	private int mProgress = 0;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				if(null == pb) pb = (ProgressBar) findViewById(R.id.pb);
				mProgress++;
				int tempProgress = msg.arg1;
				AILog.i(TAG, "handleMessage mProgress : " + tempProgress);
				if(null != pb) pb.setProgress(tempProgress);
				break;
			}
		};
	};

}
