package com.jxtii.falcon.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.jxtii.falcon.model.AlarmInfo;
import com.jxtii.falcon.model.FenceRecord;
import com.jxtii.falcon.model.UserWlinfo;
import com.jxtii.falcon.util.CommUtil;
import com.jxtii.falcon.util.DateStr;
import com.jxtii.falcon.util.LogEnum;
import com.jxtii.falcon.util.WriteLog;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by huangyc on 2016/5/13.
 */
public class TaskReceiver extends BroadcastReceiver {

    String TAG = TaskReceiver.class.getSimpleName();
    Context ctx = null;

    public void onReceive(Context context, Intent intent) {

        ctx = context;

        if (CommUtil.START_INTENT.equals(intent.getAction())) {
            logAndWrite("receive START_INTENT", LogEnum.INFO, false);
            Boolean flag = CommUtil.isServiceRunning(context, CommUtil.TASK_SERVICE);
            if (flag) {
                logAndWrite("TASK_SERVICE is alive", LogEnum.WARN, true);
            } else {
                logAndWrite("TASK_SERVICE is dead", LogEnum.WARN, true);
                startTaskService();
            }
        } else if (CommUtil.STOP_INTENT.equals(intent.getAction())) {
            logAndWrite("receive STOP_INTENT", LogEnum.INFO, true);
            stopTaskService();
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            logAndWrite("BOOT_COMPLETED", LogEnum.INFO, true);
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            long triggerAtTime = System.currentTimeMillis() + 2 * 60 * 1000;
            long interval = 15 * 60 * 1000;
            Intent intentBoot = new Intent();
            intentBoot.setAction(CommUtil.START_INTENT);
            intentBoot.setPackage(ctx.getPackageName());
            PendingIntent pt = PendingIntent.getBroadcast(ctx, 0, intentBoot, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, pt);
        } else if("android.intent.action.USER_PRESENT".equals(intent.getAction())){
            logAndWrite("USER_PRESENT", LogEnum.INFO, true);
            Boolean flag = CommUtil.isServiceRunning(context, CommUtil.TASK_SERVICE);
            if (flag) {
                logAndWrite("TASK_SERVICE is alive", LogEnum.WARN, true);
            } else {
                logAndWrite("TASK_SERVICE is dead", LogEnum.WARN, true);
                startTaskService();
            }
        } else if (CommUtil.START_FENCE.equals(intent.getAction())) {
            logAndWrite("receive START_FENCE", LogEnum.INFO, true);
            List<FenceRecord> listFr = DataSupport.where("status=?", CommUtil.STATUS_VAILD).find(FenceRecord.class);
            if (listFr != null && listFr.size() > 0) {
                for (FenceRecord fr : listFr) {
                    if (TextUtils.isEmpty(fr.getStopTime())) {
                        fr.setStopTime(DateStr.yyyymmddHHmmssStr());
                    }
                    fr.setStatus(CommUtil.STATUS_INVAILD);
                    fr.update(fr.getId());
                }
            }
            FenceRecord fr = new FenceRecord();
            fr.setStartTime(DateStr.yyyymmddHHmmssStr());
            fr.setStatus(CommUtil.STATUS_VAILD);
            fr.save();
        } else if (CommUtil.STOP_FENCE.equals(intent.getAction())) {
            logAndWrite("receive STOP_FENCE", LogEnum.INFO, true);
            int aiCount = DataSupport.count(AlarmInfo.class);
            if (aiCount > 0) {
                DataSupport.deleteAll(AlarmInfo.class);
            }
            int frCount = DataSupport.count(FenceRecord.class);
            if (frCount > 0) {
                DataSupport.deleteAll(FenceRecord.class);
            }
            int uwCount = DataSupport.count(UserWlinfo.class);
            if (uwCount > 0) {
                DataSupport.deleteAll(UserWlinfo.class);
            }
            stopTaskService();
        }
    }

    void startTaskService() {
        logAndWrite("startTaskService " + ctx.getPackageName(), LogEnum.INFO, false);
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        intent.putExtra("interval", CommUtil.LOC_FREQ);
        ctx.startService(intent);
    }

    void stopTaskService() {
        logAndWrite("stopTaskService " + ctx.getPackageName(), LogEnum.INFO, false);
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        ctx.stopService(intent);

    }

    /**
     * 记录本地日志
     *
     * @param log
     */
    void writeLog(final String log) {
        new Thread() {
            public void run() {
                WriteLog.getInstance().write(TAG, log);
            }
        }.start();
    }

    /**
     * 打印和记录日志
     *
     * @param log
     * @param level
     * @param needWrite
     */
    void logAndWrite(String log,LogEnum level,Boolean needWrite) {
        switch (level) {
            case VERBOSE:
                Log.v(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case DEBUG:
                Log.d(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case INFO:
                Log.i(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case WARN:
                Log.w(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
        }
    }
}
