package com.ileja.upgrade.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

public class FileUtil {

	private static final String TAG = "FileUtil";
	private static ExecutorService mSingleThreadExecutor = Executors
			.newSingleThreadExecutor();

	public static String getCacheAbsolutePath(Context context) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir
		boolean shouldUseExternalCache = Environment.MEDIA_MOUNTED
				.equals(Environment.getExternalStorageState())
				|| !isExternalStorageRemovable();

		File fileCacheDir = shouldUseExternalCache ? getExternalCacheDir(context)
				: context.getCacheDir();

		if (fileCacheDir == null) {
			fileCacheDir = context.getCacheDir();
		}

		final String cachePath = fileCacheDir.getPath();

		return cachePath + File.separator;
	}

	/**
	 * Check if external storage is built-in or removable.
	 * 
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@TargetApi(9)
	public static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/**
	 * Get the external app cache directory.
	 * 
	 * @param context
	 *            The context to use
	 * @return The external cache dir
	 */
	@TargetApi(8)
	public static File getExternalCacheDir(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				return context.getExternalCacheDir();
			}
		}

		return null;
	}

	/**
	 * 将文本写入到文件中
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void saveStringToFile(String fileName, String content) {
		File file = new File(fileName);
		FileWriter fw = null;
		try {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			// fixed HuaWei C8813D file not found exception
			if (parent == null || !parent.exists()) {
				return;
			}
			fw = new FileWriter(file);
			fw.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取文本文件内容
	 * 
	 * @param filename
	 * @return
	 */
	public static String readFileContent(String filename) {
		File file = new File(filename);
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		FileReader fr = null;
		BufferedReader br = null;
		StringBuilder content = new StringBuilder();
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				content.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return content.toString();
	}

	/**
	 * 将文本写入到文件中
	 * 
	 * @param filePath
	 * @param content
	 * @param isAppend
	 *            是否追加
	 */
	public static void saveStringToFile(final String filePath,
			final String content, final boolean isAppend) {
		mSingleThreadExecutor.submit(new Runnable() {

			@Override
			public synchronized void run() {
				if (TextUtils.isEmpty(filePath)
						|| filePath.matches("null\\/.*")) {
					AILog.w(TAG, "The file to be saved is null!");
					return;
				}
				FileWriter fw = null;
				try {
					File file = new File(filePath);
					File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}

					fw = new FileWriter(file, isAppend);
					fw.write(content);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fw != null) {
							fw.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 删除目录下所有文件
	 * 
	 * @param root
	 */
	public static void deleteAllFiles(File root) {
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) { // 判断是否为文件夹
					deleteAllFiles(f);
					try {
						f.delete();
					} catch (Exception e) {
					}
				} else {
					if (f.exists()) { // 判断是否存在
						try {
							f.delete();
						} catch (Exception e) {
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * 删除文件夹
	 * 
	 * @param folderPath
	 *            folderPath 文件夹完整绝对路径
	 */

	public static void delFolder(String folderPath) throws Exception {

		// 删除完里面所有内容

		delAllFile(folderPath);

		String filePath = folderPath;

		filePath = filePath.toString();

		File myFilePath = new File(filePath);

		// 删除空文件夹

		myFilePath.delete();

	}

	/**
	 * 
	 * 删除指定文件夹下所有文件
	 * 
	 * @param path
	 *            文件夹完整绝对路径
	 */

	public static boolean delAllFile(String path) {

		boolean flag = false;

		File file = new File(path);

		if (!file.exists()) {

			return flag;

		}

		if (!file.isDirectory()) {

			return flag;

		}

		String[] tempList = file.list();

		File temp = null;

		for (int i = 0; i < tempList.length; i++) {

			if (path.endsWith(File.separator)) {

				temp = new File(path + tempList[i]);

			} else {

				temp = new File(path + File.separator + tempList[i]);

			}

			if (temp.isFile()) {

				temp.delete();

			}

			if (temp.isDirectory()) {

				// 先删除文件夹里面的文件

				delAllFile(path + "/" + tempList[i]);

				// 再删除空文件夹

				try {
					delFolder(path + "/" + tempList[i]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				flag = true;

			}

		}

		return flag;

	}

}
