package com.ileja.upgrade.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	private static Toast mToast = null;
	/**
	 * toast是否启用
	 */
	private static boolean ENABLE = false;

	private ToastUtils() {

	}

	private static void init(Context context, String toast) {
		if (!ENABLE) {
			return;
		}
		mToast = Toast.makeText(context.getApplicationContext(), toast,
				Toast.LENGTH_SHORT);
	}

	public static void showToast(Context context, String toast) {
		if (!ENABLE) {
			return;
		}
		try {
			if (null == mToast) {
				synchronized (ToastUtils.class) {
					if (null == mToast) {
						init(context, toast);
					}
				}
			} else {
				mToast.setText(toast);
			}
			mToast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void cancel() {
		if (!ENABLE) {
			return;
		}
		if (null != mToast) {
			mToast.cancel();
		}
	}

	public synchronized static void release() {
		if (!ENABLE) {
			return;
		}
		cancel();
		mToast = null;
	}

}
