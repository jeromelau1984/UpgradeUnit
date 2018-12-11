package com.ileja.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";

	public static final String PREDEFINE_SCRIPT_FILE = "/system/etc/carrobotUp.sh";

	public static String readTxtFile(String filePath) {
		String lineTxt = null;
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferedReader = new BufferedReader(read);
				lineTxt = bufferedReader.readLine();
				read.close();
			} else {
				throw new RuntimeException("Invalid file path:" + filePath);
			}
		} catch (Exception e) {
			Log.e(TAG, "Read file(" + filePath + ") meets exception");
			e.printStackTrace();
		}

		if (lineTxt != null) {
			lineTxt.trim();
		}

		return lineTxt;
	}

	public static boolean isFileExist(String filePath) {
		boolean bExist = false;

		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			bExist = true;
		} else {
			bExist = false;
		}

		Log.d(TAG, "isFileExist(" + filePath + ") " + bExist);
		return bExist;
	}

	public static void writeTxtFile(String filePath, String context) {
		try {
			File file = new File(filePath);
			if (file.isFile() && file.exists()) {
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(context);
				fileWriter.flush();
				fileWriter.close();
			} else {
				throw new RuntimeException("Invalid file path:" + filePath);
			}
		} catch (Exception e) {
			Log.e(TAG, "Write file(" + filePath + ") meets exception");
			e.printStackTrace();
		}
	}
}
