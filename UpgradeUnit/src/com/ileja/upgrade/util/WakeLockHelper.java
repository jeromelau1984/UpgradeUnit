package com.ileja.upgrade.util;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

public class WakeLockHelper {

    static WakeLock globalWakeLock = null;

    /**
     * 取消锁屏
     */
    public static void setUnlocked(Activity context) {
        Window win = context.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.flags |= (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        win.setAttributes(winParams);
    }

    /**
     * 申请锁,点亮屏幕
     */
    public static void acquireBrightWakeLock(Context context) {

        if(globalWakeLock != null){
            return;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // 获取电源管理器对象
        WakeLock brightWakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "ileja-bright");
        // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        brightWakeLock.acquire();

        globalWakeLock = brightWakeLock;
    }

    /**
     * 申请锁,保持cpu运转
     */
    public static WakeLock acquireKeepCPURunningWakeLockWhenScreenOff(Context context) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // 获取电源管理器对象
        WakeLock keepRunningWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "keepRunning");
        // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        keepRunningWakeLock.acquire();
        return keepRunningWakeLock;
    }

    /**
     * 释放锁
     */
    public static void releaseWakeLock() {
        if (globalWakeLock != null && globalWakeLock.isHeld()) {
            globalWakeLock.release();
            globalWakeLock = null;
        }
    }

}
