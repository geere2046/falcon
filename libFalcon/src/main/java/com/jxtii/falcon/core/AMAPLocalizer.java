package com.jxtii.falcon.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jxtii.falcon.util.CommUtil;
import com.jxtii.falcon.util.LogEnum;
import com.jxtii.falcon.util.WriteLog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by huangyc on 2016/5/13.
 */
public class AMAPLocalizer implements AMapLocationListener {

    String TAG = AMAPLocalizer.class.getSimpleName();
    Context ctx;
    AMapLocationClient aMapLocationClient = null;
    volatile static AMAPLocalizer instance = null;
    public String locinfo = null;

    AMAPLocalizer(Context ctx) {
        this.ctx = ctx;
        if (this.aMapLocationClient == null)
            this.aMapLocationClient = new AMapLocationClient(ctx);
        this.aMapLocationClient.setLocationListener(this);
    }

    public static AMAPLocalizer getInstance(Context ctx) {
        if (instance == null) {
            synchronized (AMAPLocalizer.class) {
                if (instance == null) {
                    instance = new AMAPLocalizer(ctx);
                }
            }
        }
        return instance;
    }

    public static void destroyInstance(Context ctx) {
        if (instance != null) {
            synchronized (AMAPLocalizer.class) {
                instance = null;
            }
        }
    }

    /**
     * 开启或关闭定位
     *
     * @param swit
     *            开启或销毁定位，单次定位无需销毁
     * @param locMode
     *            定位模式
     * @param minTime
     *            定位间隔
     */
    public void setLocationManager(Boolean swit, String locMode, long minTime) {
        logAndWrite("swit is " + swit + " " + (this.aMapLocationClient != null), LogEnum.INFO, false);
        if (swit) {
            if(this.aMapLocationClient == null){
                this.aMapLocationClient = new AMapLocationClient(ctx);
                this.aMapLocationClient.setLocationListener(this);
            }
            setLocationOption(locMode, minTime);
            if(!this.aMapLocationClient.isStarted()){// aMapLocationClient.isStarted()有bug，无论是否启动成功都返回false
                logAndWrite("AMapLocationClient start", LogEnum.INFO, false);
                this.aMapLocationClient.startLocation();
            }else{
                logAndWrite("AMapLocationClient haved start", LogEnum.INFO, false);
            }
        } else {
            if (this.aMapLocationClient != null) {
                this.aMapLocationClient.unRegisterLocationListener(this);
                this.aMapLocationClient.stopLocation();
                this.aMapLocationClient.onDestroy();
            }
            this.aMapLocationClient = null;
            instance = null;
        }
    }

    void setLocationOption(String locMode, long minTime) {
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        if (TextUtils.isEmpty(locMode)) {
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            logAndWrite("Device_Sensors", LogEnum.INFO, false);
        } else {
            if ("low".equalsIgnoreCase(locMode)) {
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                logAndWrite("Battery_Saving", LogEnum.INFO, false);
            } else if ("gps".equalsIgnoreCase(locMode)) {
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
                logAndWrite("Device_Sensors", LogEnum.INFO, false);
            } else {
                //设置该选项将延长定位返回时长30s，仅在高精度模式单次定位下有效
                locationOption.setGpsFirst(true);
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                logAndWrite("Hight_Accuracy", LogEnum.INFO, false);
            }
        }
        if (minTime < 900) {
            locationOption.setOnceLocation(true);
        } else {
            locationOption.setOnceLocation(false);
            locationOption.setInterval(minTime);
        }
        locationOption.setMockEnable(false);//default false
        locationOption.setWifiActiveScan(true);//default true
        locationOption.setHttpTimeOut(15000);
        locationOption.setKillProcess(true);//default false
        locationOption.setNeedAddress(true);//default true
        locationOption.setOnceLocation(false);//default false
        aMapLocationClient.setLocationOption(locationOption);
    }

    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            EventBus.getDefault().post(amapLocation);//TODO 测试时开放网络定位数据
            String extra = "";
            if ("gps".equals(amapLocation.getProvider())) {

//                EventBus.getDefault().post(amapLocation);//只传递GPS数据

                extra = "定位结果来自GPS信号";
                if (amapLocation.getSatellites() != 0)
                    extra += (",连接" + amapLocation.getSatellites() + "颗卫星");
                if (amapLocation.hasAccuracy())
                    extra += (",精度：" + amapLocation.getAccuracy() + "米");
                if (amapLocation.hasSpeed())
                    extra += (",速度:"
                            + CommUtil.floatToStr(
                            amapLocation.getSpeed() * 18 / 5, 1) + "公里/时");
                if (amapLocation.hasAltitude())
                    extra += (",海拔:"
                            + CommUtil.floatToStr(
                            (float) amapLocation.getAltitude(), 1) + "米");
                if (amapLocation.hasBearing()) {
                    extra += (",方向:北偏东" + amapLocation.getBearing() + "度");
                }
            } else if ("lbs".equals(amapLocation.getProvider())) {
                if (amapLocation.hasAccuracy()) {
                    Float acc = amapLocation.getAccuracy();
                    if (acc <= 100.0) {
                        extra = "定位结果来自WIFI信号";
                    } else {
                        extra = "定位结果来自基站信号";
                    }
                    extra += (",精度：" + acc + "米");
                } else {
                    extra = "定位结果来自网络定位信号";
                }
            }
            extra += ",定位通道1";
            extra += amapLocation.getLocationType();
            extra += amapLocation.getLocationDetail();
            Double geoLat = amapLocation.getLatitude();
            Double geoLng = amapLocation.getLongitude();
            locinfo = geoLat + ";" + geoLng + ";定位器不做地址解析;"
                    + amapLocation.getProvider() + ";" + extra;
//            logAndWrite("locinfo = " + locinfo, LogEnum.INFO, false);
        } else if (amapLocation != null && amapLocation.getErrorCode() != 0) {
            logAndWrite("amapLocation.getErrorCode() = "
                    + amapLocation.getErrorCode()
                    + " amapLocation.getErrorInfo() = "
                    + amapLocation.getErrorInfo(), LogEnum.WARN, true);
            locinfo = amapLocation.getErrorCode() + "_"
                    + amapLocation.getErrorInfo();
        } else {
            logAndWrite("获取定位信息失败", LogEnum.WARN, true);
            locinfo = "获取定位信息失败";
        }
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
                Log.v(TAG, log);
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
