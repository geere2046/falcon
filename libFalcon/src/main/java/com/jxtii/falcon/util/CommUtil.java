package com.jxtii.falcon.util;

import android.app.ActivityManager;
import android.content.Context;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by huangyc on 2016/5/13.
 */
public class CommUtil {

    static final String TAG = CommUtil.class.getSimpleName();

    public static final String START_INTENT = "com.jxtii.falcon.task_receiver";
    public static final String STOP_INTENT = "com.jxtii.falcon.stop_receiver";
    public static final String TASK_SERVICE = "com.jxtii.falcon.service.TaskService";
    public static final String TASK_SERVICE_ACTION = "com.jxtii.falcon.task_service";
    public static final int LOC_FREQ = 60000;
    public static final String START_FENCE = "com.jxtii.falcon.start_fence";
    public static final String STOP_FENCE = "com.jxtii.falcon.stop_fence";
    public static final String STATUS_VAILD = "1";//有效
    public static final String STATUS_EXPIRE = "2";//过期
    public static final String STATUS_INVAILD = "3";//失效
    public static final String STATUS_CLOSING = "4";//关闭中
    //TODO 网络通讯参数 需放到so中
    public static final String NAME_SPACE = "http://ep.wqsm.gaf.com/";
    public static final String WS_URL = "http://mi.zjwq.net/PubService.ws";
    public static final int CIRCLE = 1;
    public static final int RECTANGLE = 2;
    public static final int POLYGON = 3;
    public static final int LEAVE = 0;
    public static final int ENTER = 1;
    public static final int IN_OUT = 2;
    public static final int TYPE_ALARM = 1;
    public static final int TYPE_TIME = 2;
    public static final int TYPE_CON = 3;


    /**
     * 判断服务是否在运行
     *
     * @param mContext
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 从float转化为str
     * @param fl
     * @param su 保留小数点后精度位数，小于1时取整数
     * @return
     */
    public static String floatToStr(float fl, int su) {
        String str = su < 1 ? "0" : ".";
        for (int i = 0; i < su; i++) {
            str += "0";
        }
        DecimalFormat decimalFormat = new DecimalFormat(str);
        String p = decimalFormat.format(fl);
        return p;
    }

    /**
     * 取最大公约数
     * @param list
     * @return
     */
    public static int getGcd(List<Integer> list) {
        int res = 0;
        if (list != null && list.size() > 1) {
            Collections.sort(list, new Comparator<Integer>() {
                public int compare(Integer lhs, Integer rhs) {
                    return lhs >= rhs ? 1 : -1;
                }
            });
            int min = list.get(0);
            for (int match = min; min > 0; match--) {
                int con = 0;
                for (Integer in : list) {
                    if (in % match == 0)
                        con += 1;
                }
                if (con == list.size()) {
                    res = match;
                    break;
                }
            }
        }
        return res;
    }
}
