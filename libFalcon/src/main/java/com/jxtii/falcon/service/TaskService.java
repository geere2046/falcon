package com.jxtii.falcon.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.gaf.lbs.electFence.domain.AreaTypeEnum;
import com.gaf.lbs.electFence.domain.PointInfo;
import com.gaf.lbs.electFence.domain.SpatialEnum;
import com.gaf.lbs.electFence.util.SpatialJudgmentUtil;
import com.jxtii.falcon.bean.LocInfo;
import com.jxtii.falcon.bean.PubData;
import com.jxtii.falcon.bean.PubDataList;
import com.jxtii.falcon.bean.ResultBean;
import com.jxtii.falcon.core.AMAPLocalizer;
import com.jxtii.falcon.model.AlarmInfo;
import com.jxtii.falcon.model.FenceRecord;
import com.jxtii.falcon.model.UserWlinfo;
import com.jxtii.falcon.model.WlMapPoi;
import com.jxtii.falcon.util.CommUtil;
import com.jxtii.falcon.util.DateStr;
import com.jxtii.falcon.util.LogEnum;
import com.jxtii.falcon.util.WriteLog;
import com.jxtii.falcon.webservice.RestWebserviceClient;
import com.jxtii.falcon.webservice.WebserviceClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huangyc on 2016/5/13.
 */
public class TaskService extends Service {

    String TAG = TaskService.class.getSimpleName();
    Context ctx;
    AMAPLocalizer amapLocalizer;
    Timer mTimer;
    TimerTask mTimerTask;
    TimerTask mTimerTaskSc;
    TimerTask mTimerTaskTc;
    PowerManager.WakeLock m_wakeLockObj;
    int interval = 0;//检查用户是否开启围栏频率
    int taskInterval = 0;//用户调度任务频率
    int alarmInterval = 0;//用户告警任务频率
    LocInfo locInfo;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        logAndWrite("onCreateService", LogEnum.WARN, true);
        ctx = TaskService.this;
        amapLocalizer = AMAPLocalizer.getInstance(ctx);
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            logAndWrite("onStartCommand intent is null", LogEnum.WARN, true);
        } else {
            interval = intent.getIntExtra("interval", 60 * 1000);//没有配置则默认每分钟输出定位
            logAndWrite("onStartCommand interval = " + interval, LogEnum.WARN, true);
            if (amapLocalizer != null){
                logAndWrite("amapLocalizer != null", LogEnum.WARN, false);//TODO 测试时允许网络定位
                amapLocalizer.setLocationManager(true, "high", interval);
            }else{
                logAndWrite("amapLocalizer == null", LogEnum.WARN, false);
                amapLocalizer = AMAPLocalizer.getInstance(ctx);
                amapLocalizer.setLocationManager(true, "high", interval);//TODO 测试时允许网络定位
            }
            stopTimer();
            if (mTimer == null)
                mTimer = new Timer();
            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    public void run() {
                        acquireWakeLock(ctx);
                        judgeCond();
                        releaseWakeLock();
                    }
                };
            }
            mTimer.scheduleAtFixedRate(mTimerTask, 1 * 1000,
                    2 * 60 *1000);//TODO 默认15分钟去检查一次用户当天是否开启围栏
        }
        return START_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void receiveAMapLocation(AMapLocation amapLocation){
        logAndWrite("AMapLocation is " + amapLocation.toStr(), LogEnum.INFO, false);
        LocInfo li = new LocInfo();
        li.setLatitude(amapLocation.getLatitude());
        li.setLongitude(amapLocation.getLongitude());
        li.setGpstime(DateStr.yyyymmddHHmmssStr());
        this.locInfo = li;
    }

    /**
     * 检查是否开启任务
     */
    void judgeCond() {
        try {
            List<FenceRecord> listFr = DataSupport.where("status = ?", CommUtil.STATUS_VAILD).order("startTime desc").limit(1).find(FenceRecord.class);
            if (listFr != null && listFr.size() > 0) {

                //获取用户当天有效围栏信息
                int umCount = DataSupport.count(UserWlinfo.class);
                if(umCount>0){
                    DataSupport.deleteAll(UserWlinfo.class);
                }
                FenceRecord fr = listFr.get(0);
                logAndWrite("FenceRecord:" + fr.toString(), LogEnum.INFO, false);
                Map<String, Object> req = new HashMap<String, Object>();
                req.put("sqlKey", "sql_tg_wl_info");
                req.put("sqlType", "sql");
                req.put("rUserId", "ff8080814211ed9601421322430200d1");//TODO 补充userId获取渠道
                String reqStr = JSON.toJSONString(req);
                PubDataList list = new WebserviceClient().loadDataList(reqStr);
                if (list != null && "00".equals(list.getCode())) {
                    List<Map<String, Object>> dataList = list.getData();
                    if (dataList != null && dataList.size() > 0) {
                        for (Map<String, Object> data : dataList) {
//                            for (Map.Entry<String, Object> inner : data.entrySet()) {
//                                logAndWrite(inner.getKey() + ":" + inner.getValue(), LogEnum.INFO, false);
//                            }
                            UserWlinfo uw = new UserWlinfo();
                            uw.setWlId(data.get("ID").toString());
                            uw.setWlName(data.get("WL_NAME").toString());
                            uw.setFreq(Integer.valueOf(data.get("FREQUENCY").toString()));
                            uw.setSubFreq(Integer.valueOf(data.get("SUB_FREQUENCY").toString()));
                            uw.setStartTime(data.get("START_TIME").toString());
                            uw.setEndTime(data.get("END_TIME").toString());
                            uw.setWarnStyle(data.get("WARN_STYLE").toString());
                            uw.setReceiveTels(data.get("RECEIVE_TELS").toString());
                            uw.setCriteria(Integer.valueOf(data.get("CRITERIA").toString()));
                            uw.setDuration(Integer.valueOf(data.get("DURATION").toString()));
                            uw.setCfCount(Integer.valueOf(data.get("CFCOUNT").toString()));
                            uw.setIsUpPower(data.get("END_TIME").toString());
                            uw.save();
                        }
                    } else {
                        logAndWrite("sql_tg_wl_info getData is null", LogEnum.INFO, false);
                    }
                } else {
                    logAndWrite("sql_tg_wl_info:code=" + list.getCode(), LogEnum.INFO, false);
                }

                //根据围栏信息开启调度任务
                final List<UserWlinfo> listEw = DataSupport.findAll(UserWlinfo.class);
                if(listEw!=null&&listEw.size()>0){
                    List<Integer> listInt = new ArrayList<Integer>();
                    for(UserWlinfo uwReq:listEw){
                        listInt.add(uwReq.getFreq());
                    }
                    taskInterval = CommUtil.getGcd(listInt) * 60 * 1000;

                    if (mTimer == null)
                        mTimer = new Timer();
                    if (mTimerTaskSc == null) {
                        mTimerTaskSc = new TimerTask() {
                            public void run() {
                                acquireWakeLock(ctx);
                                judgePos(listEw);
                                releaseWakeLock();
                            }
                        };
                        mTimer.scheduleAtFixedRate(mTimerTaskSc, 1 * 1000,
                                taskInterval);
                        if (mTimerTask != null) {//开启调度任务判断电子围栏后关闭(检查用户是否启动围栏指令)的任务
                            mTimerTask.cancel();
                            mTimerTask = null;
                        }
                    }
                }else{
                    logAndWrite("未取到当天有效围栏信息", LogEnum.INFO, true);
                }
            } else {
                logAndWrite("未取到围栏开始指令", LogEnum.INFO, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现调度任务判断电子围栏
     * @param listEw
     */
    void judgePos(List<UserWlinfo> listEw) {
        try {

            if (listEw != null && listEw.size() > 0) {
                for (UserWlinfo uw : listEw) {
                    String wlId = uw.getWlId();
                    int count = DataSupport.where("wlId = ?", wlId).count(WlMapPoi.class);
                    if (count == 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("sqlKey", "sql_tg_wl_poi");
                        map.put("sqlType", "sql");
                        map.put("rWlid", wlId);
                        String reqStr = JSON.toJSONString(map);
                        PubDataList list = new WebserviceClient().loadDataList(reqStr);
                        if (list != null && "00".equals(list.getCode())) {
                            List<Map<String, Object>> dataList = list.getData();
                            if (dataList != null && dataList.size() > 0) {
                                for (Map<String, Object> data : dataList) {
                                    for (Map.Entry<String, Object> inner : data.entrySet()) {
                                        logAndWrite(inner.getKey() + ";" + inner.getValue(), LogEnum.INFO, false);
                                    }
                                    WlMapPoi wmp = new WlMapPoi();
                                    wmp.setWlId(wlId);
                                    wmp.setPoiId(data.get("ID").toString());
                                    wmp.setPoiName(data.get("POI_NAME").toString());
                                    wmp.setMapAreaType(Integer.valueOf(data.get("MAP_AREA_TYPE").toString()));
                                    wmp.setLat(Double.valueOf(data.get("LATITUDE").toString()));
                                    wmp.setLng(Double.valueOf(data.get("LONGITUDE").toString()));
                                    wmp.setPointOrder(Integer.valueOf(data.get("POINT_ORDER").toString()));
                                    wmp.save();
                                }
                            } else {
                                logAndWrite("sql_tg_wl_poi getData is null", LogEnum.INFO, false);
                            }
                        } else {
                            logAndWrite("sql_tg_wl_poi:code=" + list.getCode(), LogEnum.INFO, false);
                        }
                    }else{
                        logAndWrite("wlId.count=" + count, LogEnum.INFO, false);
                    }
                }

                for (UserWlinfo uw : listEw) {
                    String now = DateStr.HHmmssStr().replaceAll(":","");
                    long span = DateStr.timeSpanSecondHHmmss(locInfo.getGpstime(),DateStr.yyyymmddHHmmssStr());
                    if(now.compareTo(uw.getStartTime())>=0 && now.compareTo(uw.getEndTime())<0&&span<120){
                        String wlId = uw.getWlId();
                        List<WlMapPoi> coList = DataSupport.where("wlId = ?", wlId).order("poiId,pointOrder").find(WlMapPoi.class);
                        if (coList.size() > 0) {

                            LocInfo thisLi = new LocInfo();
                            thisLi = this.locInfo;

                            String id = "";
                            int areaTypeReq = 0;
                            int cou = 0;
                            List<PointInfo> listPi = new ArrayList<>();
                            Map<String,String> union = new HashMap<>();
                            for (WlMapPoi map : coList) {
                                cou++;
                                String mapId = map.getPoiId();
                                double lat = map.getLat();
                                double lng = map.getLng();
                                int areaType = map.getMapAreaType();
                                PointInfo pi = null;
                                if ("".equals(id)) {
                                    id = mapId;
                                    areaTypeReq = areaType;
                                    pi = new PointInfo();
                                    pi.setLat(thisLi.getLatitude());
                                    pi.setLng(thisLi.getLongitude());
                                    listPi.add(pi);
                                    pi = new PointInfo();
                                    pi.setLat(lat);
                                    pi.setLng(lng);
                                    listPi.add(pi);
                                } else if (id.equals(mapId)) {
                                    pi = new PointInfo();
                                    pi.setLat(lat);
                                    pi.setLng(lng);
                                    listPi.add(pi);
                                    if (cou == coList.size()) {
                                        union.put(id, getGeoResult(listPi, areaTypeReq));
                                    }
                                } else {
                                    union.put(id, getGeoResult(listPi, areaTypeReq));
                                    id = mapId;
                                    areaTypeReq = areaType;
                                    pi = new PointInfo();
                                    pi.setLat(thisLi.getLatitude());
                                    pi.setLng(thisLi.getLongitude());
                                    listPi.add(pi);
                                    pi = new PointInfo();
                                    pi.setLat(lat);
                                    pi.setLng(lng);
                                    listPi.add(pi);
                                }
                            }
                            if(uw.getCriteria()==CommUtil.LEAVE){
                                int co = 0;
                                for(Map.Entry<String,String> entry : union.entrySet()){
                                    if(entry.getValue().equals("EXTERIOR"))
                                        co+=1;
                                }
                                if(co==union.size()){//全部区域都离开才算离开围栏
                                    union.clear();
                                    AlarmInfo ai = new AlarmInfo();
                                    ai.setWlId(uw.getWlId());
                                    ai.setWlName(uw.getWlName());
                                    ai.setAction("EXTERIOR");
                                    ai.setLng(thisLi.getLongitude());
                                    ai.setLat(thisLi.getLatitude());
                                    ai.setGpstime(thisLi.getGpstime());
                                    ai.setStatus(CommUtil.STATUS_VAILD);
                                    ai.save();
                                    startAlarm();
                                }else{//检测到进入围栏则失效历史数据
                                    List<AlarmInfo> aiList = DataSupport.where("wlId = ? and status = ?", uw.getWlId(), CommUtil.STATUS_VAILD).order("gpstime desc").find(AlarmInfo.class);
                                    if (aiList != null && aiList.size() > 0) {
                                        for(AlarmInfo ai :aiList){
                                            ContentValues values = new ContentValues();
                                            values.put("status", CommUtil.STATUS_EXPIRE);
                                            DataSupport.update(AlarmInfo.class,values, ai.getId());
                                        }
                                    }
                                }
                            }else if(uw.getCriteria()==CommUtil.ENTER){
                                int co = 0;
                                for(Map.Entry<String,String> entry : union.entrySet()){
                                    if(entry.getValue().equals("INTERIOR"))
                                        co+=1;
                                }
                                if(co>0){//只要进入一个区域就算进入围栏
                                    union.clear();
                                    AlarmInfo ai = new AlarmInfo();
                                    ai.setWlId(uw.getWlId());
                                    ai.setWlName(uw.getWlName());
                                    ai.setAction("INTERIOR");
                                    ai.setLng(thisLi.getLongitude());
                                    ai.setLat(thisLi.getLatitude());
                                    ai.setGpstime(thisLi.getGpstime());
                                    ai.setStatus(CommUtil.STATUS_VAILD);
                                    ai.save();
                                    startAlarm();
                                }else{//检测到离开围栏则失效历史数据
                                    List<AlarmInfo> aiList = DataSupport.where("wlId = ? and status = ?", uw.getWlId(), CommUtil.STATUS_VAILD).order("gpstime desc").find(AlarmInfo.class);
                                    if (aiList != null && aiList.size() > 0) {
                                        for(AlarmInfo ai :aiList){
                                            ContentValues values = new ContentValues();
                                            values.put("status", CommUtil.STATUS_EXPIRE);
                                            DataSupport.update(AlarmInfo.class,values, ai.getId());
                                        }
                                    }
                                }
                            }else if(uw.getCriteria()==CommUtil.IN_OUT) { //进出需要根据上次的位置结果去判断，不一样则记录
                                int co = 0;
                                int coSc = 0;
                                for (Map.Entry<String, String> entry : union.entrySet()) {
                                    if (entry.getValue().equals("INTERIOR"))
                                        co += 1;
                                    if (entry.getValue().equals("EXTERIOR"))
                                        coSc += 1;
                                }
                                List<AlarmInfo> aiList = DataSupport.where("wlId = ? and (status = ? or status = ?)", uw.getWlId(), CommUtil.STATUS_VAILD, CommUtil.STATUS_SIGN).order("gpstime desc").find(AlarmInfo.class);
                                Boolean flag = true;
                                String action = "";
                                if (aiList != null && aiList.size() > 0) {
                                    AlarmInfo last = aiList.get(0);
                                    if ("INTERIOR".equals(last.getAction())) {
                                        if (coSc != union.size()) {
                                            flag = false;
                                        } else {
                                            action = "EXTERIOR";
                                        }
                                    } else if ("EXTERIOR".equals(last.getAction())) {
                                        if (co == 0) {
                                            flag = false;
                                        } else {
                                            action = "INTERIOR";
                                        }
                                    }
                                } else {
                                    if (co > 0)
                                        action = "INTERIOR";
                                    if (coSc == union.size())
                                        action = "EXTERIOR";

                                }
                                if (flag&&!"".equals(action)) {
                                    union.clear();
                                    AlarmInfo ai = new AlarmInfo();
                                    ai.setWlId(uw.getWlId());
                                    ai.setWlName(uw.getWlName());
                                    ai.setAction(action);
                                    ai.setLng(thisLi.getLongitude());
                                    ai.setLat(thisLi.getLatitude());
                                    ai.setGpstime(thisLi.getGpstime());
                                    ai.setStatus(CommUtil.STATUS_VAILD);
                                    ai.save();
                                    startAlarm();
                                }else{
                                    logAndWrite("flag="+flag+";action="+ action, LogEnum.INFO, false);
                                }
                            }else{
                                logAndWrite("uw.getCriteria() is illegal : " + uw.getCriteria(), LogEnum.INFO, false);
                            }
                        }else{
                            logAndWrite("coList.size=0", LogEnum.INFO, false);
                        }
                    }else{
                        logAndWrite("uw time invalid:span="+span+",now="+now, LogEnum.INFO, false);
                    }
                }
            } else {
                logAndWrite("judgePos listId is null", LogEnum.INFO, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动告警任务
     */
    void startAlarm(){
        final List<UserWlinfo> listEw = DataSupport.findAll(UserWlinfo.class);
        if(listEw!=null&&listEw.size()>0){
            List<Integer> listInt = new ArrayList<Integer>();
            for(UserWlinfo uwReq:listEw){
                listInt.add(uwReq.getSubFreq());
            }
            alarmInterval = CommUtil.getGcd(listInt) * 60 * 1000;

            if (mTimer == null)
                mTimer = new Timer();
            if (mTimerTaskTc == null) {
                mTimerTaskTc = new TimerTask() {
                    public void run() {
                        acquireWakeLock(ctx);
                        for(UserWlinfo uw:listEw){
                            List<AlarmInfo> aiList = DataSupport.where("wlId = ? and status = ?", uw.getWlId(), CommUtil.STATUS_VAILD).order("gpstime desc").find(AlarmInfo.class);//进出时忽略标记状态（STATUS_SIGN）数据
                            if(aiList!=null&&aiList.size()>0){
                                String warnStyle = uw.getWarnStyle();
                                int fc = Integer.valueOf(warnStyle.substring(1, 2));
                                int sc = Integer.valueOf(warnStyle.substring(2, 3));
                                if(fc==0&&sc==0){ //告警情况
                                    if(uw.getCriteria()==CommUtil.LEAVE||uw.getCriteria()==CommUtil.ENTER){
                                        if(uw.getDuration()==0){
                                            uploadAlarm(uw,aiList,0);
                                        }else{
                                            String start = aiList.get(aiList.size()-1).getGpstime();
                                            String end = aiList.get(0).getGpstime();
                                            long dis = DateStr.timeSpanSecond(start,end);
                                            if(dis>=uw.getDuration()*60){
                                                uploadAlarm(uw,aiList,new Long(dis/60).intValue());
                                            }
                                        }
                                    }else if(uw.getCriteria()==CommUtil.IN_OUT){
                                        //进出时不考虑延后告警
                                        uploadAlarm(uw,aiList,0);
                                    }
                                }else if(fc ==1 ) { //停留情况
                                    if (uw.getCriteria() == CommUtil.LEAVE || uw.getCriteria() == CommUtil.ENTER) {
                                        if (uw.getDuration() == 0) {
                                            String start = aiList.get(aiList.size() - 1).getGpstime();
                                            String end = aiList.get(0).getGpstime();
                                            long dis = DateStr.timeSpanSecond(start, end);
                                            uploadStick(uw, aiList, new Long(dis / 60).intValue());
                                        } else {
                                            String start = aiList.get(aiList.size() - 1).getGpstime();
                                            String end = aiList.get(0).getGpstime();
                                            long dis = DateStr.timeSpanSecond(start, end);
                                            if (dis >= uw.getDuration() * 60) {
                                                uploadStick(uw, aiList, new Long(dis / 60).intValue());
                                            }
                                        }
                                    } else if (uw.getCriteria() == CommUtil.IN_OUT) {//进出时比对的是最近一次定位结果和现在
                                        if (uw.getDuration() == 0) {
                                            String start = aiList.get(0).getGpstime();
                                            String end = DateStr.yyyymmddHHmmssStr();
                                            long dis = DateStr.timeSpanSecond(start, end);
                                            uploadStick(uw, aiList, new Long(dis / 60).intValue());
                                        } else {
                                            String start = aiList.get(0).getGpstime();
                                            String end = DateStr.yyyymmddHHmmssStr();
                                            long dis = DateStr.timeSpanSecond(start, end);
                                            if (dis >= uw.getDuration() * 60) {
                                                uploadStick(uw, aiList, new Long(dis / 60).intValue());
                                            }
                                        }
                                    }
                                }else if(sc == 1){//记录次数情况
                                    if (uw.getCriteria() == CommUtil.LEAVE || uw.getCriteria() == CommUtil.ENTER) {
                                        //进入或离开不支持记录次数
                                    }else if(uw.getCriteria()==CommUtil.IN_OUT) {
                                        if (aiList.size() >= uw.getCfCount()) {
                                            uploadInout(uw,aiList);
                                        }
                                    }
                                }else{
                                    logAndWrite("warnStyle="+warnStyle, LogEnum.INFO, true);
                                }
                            }
                        }
                        releaseWakeLock();
                    }
                };
                mTimer.scheduleAtFixedRate(mTimerTaskTc, 1 * 1000,
                        alarmInterval);
            }else{
                logAndWrite("mTimerTaskTc is alive", LogEnum.INFO, false);
            }
        }else{
            logAndWrite("未取到当天有效围栏信息", LogEnum.INFO, true);
        }
    }

    /**
     * 上报告警信息
     * @param uw
     * @param aiList
     * @param min 已进入围栏多少分钟
     */
    void uploadAlarm(UserWlinfo uw,List<AlarmInfo> aiList,int min) {
        Boolean upFlag = true;

        Map<String, Object> req = new HashMap<>();

        String username = "黄宇晨";//TODO 补充用户名获取渠道
        String terminalCode = "18079159780";//TODO 补充手机号获取渠道
        String msg = username + "(" + terminalCode + ")";
        if ("EXTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 0 + "");
            msg += "离开";
        } else if ("INTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 1 + "");
            msg += "进入";
        } else {
            upFlag = false;
        }
        if (upFlag) {
            req.put("sqlKey", "sql_tg_save_warn");
            req.put("sqlType", "sql");
            req.put("rComId", "978f6626c0a8021c004741d670f39447");//TODO 补充comId获取渠道
            req.put("rUserId", "ff8080814211ed9601421322430200d1");//TODO 补充userId获取渠道
            req.put("rWlId", uw.getWlId());
            req.put("rWarnStyle", CommUtil.TYPE_ALARM + "");
            req.put("rReceiveTels", uw.getReceiveTels());

            req.put("rLat", aiList.get(0).getLat());
            req.put("rLng", aiList.get(0).getLng());

            ResultBean reBean = new RestWebserviceClient().getGaoDeGeoCodeLocInfo(aiList.get(0).getLat(), aiList.get(0).getLng(), "gcj", ctx);//TODO 正式gps数据入参改为wgs
            String geoAddr = reBean.getDesc();
            String extra = "0".equals(reBean.getCode()) ? ("，地址：" + geoAddr) : "";

            req.put("rAddr", geoAddr);
            req.put("rDuration", min);
            msg += uw.getWlName() + "，时间：" + DateStr.yyyymmddHHmmssFormat(aiList.get(0).getGpstime()) + extra + "。";
            req.put("rWarnContent", msg);
            req.put("rCfcount", 0);

            String reqJson = JSON.toJSONString(req);
            PubData pd = new WebserviceClient().updateData(reqJson);
            if (pd != null && "00".equals(pd.getCode())) {
                for (AlarmInfo ai : aiList) {
                    ContentValues values = new ContentValues();
                    if(uw.getCriteria() == CommUtil.IN_OUT){
                        values.put("status", CommUtil.STATUS_SIGN);
                    }else{
                        values.put("status", CommUtil.STATUS_INVAILD);
                    }
                    DataSupport.update(AlarmInfo.class,values, ai.getId());
                }
            } else {
                logAndWrite("sql_save_warn_record resp invalid", LogEnum.INFO, false);
            }
        }
    }

    void uploadStick(UserWlinfo uw,List<AlarmInfo> aiList,int min) {
        Boolean upFlag = true;

        Map<String, Object> req = new HashMap<>();

        String username = "黄宇晨";//TODO 补充用户名获取渠道
        String terminalCode = "18079159780";//TODO 补充手机号获取渠道
        String msg = "截至"+DateStr.yyyymmddHHmmssFormat(aiList.get(0).getGpstime()) + "。"+ username + "(" + terminalCode + ")";
        if ("EXTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 0 + "");
            msg += "已经离开";
        } else if ("INTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 1 + "");
            msg += "已经进入";
        } else {
            upFlag = false;
        }
        if (upFlag) {
            req.put("sqlKey", "sql_tg_save_warn");
            req.put("sqlType", "sql");
            req.put("rComId", "978f6626c0a8021c004741d670f39447");//TODO 补充comId获取渠道
            req.put("rUserId", "ff8080814211ed9601421322430200d1");//TODO 补充userId获取渠道
            req.put("rWlId", uw.getWlId());
            req.put("rWarnStyle", CommUtil.TYPE_TIME + "");
            req.put("rReceiveTels", uw.getReceiveTels());

            req.put("rLat", aiList.get(0).getLat());
            req.put("rLng", aiList.get(0).getLng());

            ResultBean reBean = new RestWebserviceClient().getGaoDeGeoCodeLocInfo(aiList.get(0).getLat(), aiList.get(0).getLng(), "gcj", ctx);//TODO 正式gps数据入参改为wgs
            String geoAddr = reBean.getDesc();

            req.put("rAddr", geoAddr);
            req.put("rDuration", min);
            msg += uw.getWlName() + "超过" + min +"分钟。";
            req.put("rWarnContent", msg);
            req.put("rCfcount", 0);

            String reqJson = JSON.toJSONString(req);
            PubData pd = new WebserviceClient().updateData(reqJson);
            if (pd != null && "00".equals(pd.getCode())) {
                for (AlarmInfo ai : aiList) {
                    //检测停留不需要失效数据
                }
            } else {
                logAndWrite("sql_save_warn_record resp invalid", LogEnum.INFO, false);
            }
        }
    }

    /**
     * 上报进出次数
     * @param uw
     * @param aiList
     */
    void uploadInout(UserWlinfo uw,List<AlarmInfo> aiList) {
        Boolean upFlag = true;

        Map<String, Object> req = new HashMap<>();

        String username = "黄宇晨";//TODO 补充用户名获取渠道
        String terminalCode = "18079159780";//TODO 补充手机号获取渠道
        String msg = username + "(" + terminalCode + ")";
        String msgSc = "";
        if ("EXTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 0 + "");
            msgSc = "当前状态：围栏外。";
        } else if ("INTERIOR".equals(aiList.get(0).getAction())) {
            req.put("rCriteria", 1 + "");
            msgSc = "当前状态：围栏内。";
        } else {
            upFlag = false;
        }
        if (upFlag) {
            req.put("sqlKey", "sql_tg_save_warn");
            req.put("sqlType", "sql");
            req.put("rComId", "978f6626c0a8021c004741d670f39447");//TODO 补充comId获取渠道
            req.put("rUserId", "ff8080814211ed9601421322430200d1");//TODO 补充userId获取渠道
            req.put("rWlId", uw.getWlId());
            req.put("rWarnStyle", CommUtil.TYPE_CON + "");
            req.put("rReceiveTels", uw.getReceiveTels());

            req.put("rLat", aiList.get(0).getLat());
            req.put("rLng", aiList.get(0).getLng());

            ResultBean reBean = new RestWebserviceClient().getGaoDeGeoCodeLocInfo(aiList.get(0).getLat(), aiList.get(0).getLng(), "gcj", ctx);//TODO 正式gps数据入参改为wgs
            String geoAddr = reBean.getDesc();
            String extra = "0".equals(reBean.getCode()) ? ("当前位置：" + geoAddr+"。") : "。";

            req.put("rAddr", geoAddr);
            req.put("rDuration", 0);
            msg += "截至"+ DateStr.yyyymmddHHmmssFormat(aiList.get(0).getGpstime())+ "已进出" + uw.getWlName() + aiList.size()+"次，"+msgSc+ extra;
            req.put("rWarnContent", msg);
            req.put("rCfcount", aiList.size());

            String reqJson = JSON.toJSONString(req);
            PubData pd = new WebserviceClient().updateData(reqJson);
            if (pd != null && "00".equals(pd.getCode())) {
                for (AlarmInfo ai : aiList) {
                    ContentValues values = new ContentValues();
                    values.put("status", CommUtil.STATUS_SIGN);
                    DataSupport.update(AlarmInfo.class,values, ai.getId());
                }
            } else {
                logAndWrite("sql_save_warn_record resp invalid", LogEnum.INFO, false);
            }
        }
    }

    String getGeoResult(List<PointInfo> listPi, int areaType) {
        String res = "";
        SpatialEnum spEnum;
        if (areaType == CommUtil.RECTANGLE) {
            spEnum = SpatialJudgmentUtil.spatialLocation(
                    AreaTypeEnum.Rectangle, listPi);
        } else if (areaType == CommUtil.CIRCLE) {
            spEnum = SpatialJudgmentUtil.spatialLocation(AreaTypeEnum.Circle,
                    listPi);
        } else if (areaType == CommUtil.POLYGON) {
            spEnum = SpatialJudgmentUtil.spatialLocation(AreaTypeEnum.Polygon,
                    listPi);
        }else{
            spEnum = SpatialEnum.Error;
        }
        listPi.clear();
        switch (spEnum) {
            case Error:
                res = "ERROR";
                break;
            case Interior:
                res = "INTERIOR";
                break;
            case Intersection:
                res = "INTERIOR";
                break;
            case Exterior:
                res = "EXTERIOR";
                break;
            default:
                res = "OTHER";
                break;
        }
        return res;
    }

    public void onDestroy() {
        logAndWrite("onDestroy", LogEnum.INFO, false);
        super.onDestroy();
        stopSelfSevice();
    }

    public void onLowMemory() {
        logAndWrite("onLowMemory", LogEnum.INFO, false);
        super.onLowMemory();
    }

    public void onTrimMemory(int level) {
        logAndWrite("onTrimMemory", LogEnum.INFO, false);
        super.onTrimMemory(level);
    }

    void stopSelfSevice() {
        logAndWrite("stopSelfSevice", LogEnum.INFO, true);
        if (amapLocalizer != null) {
            amapLocalizer.setLocationManager(false, "", 0);
        }
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(CommUtil.START_INTENT);
        intent.setPackage(ctx.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                intent, 0);
        long triggerAtTime = SystemClock.elapsedRealtime() + 30 * 1000;
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                pendingIntent);
        stopTimer();
        EventBus.getDefault().unregister(this);
        this.stopSelf();
    }

    void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    void acquireWakeLock(Context cxt) {
        logAndWrite(">>>>>>点亮屏幕", LogEnum.VERBOSE, false);
        if (m_wakeLockObj == null) {
            PowerManager pm = (PowerManager) cxt
                    .getSystemService(Context.POWER_SERVICE);
            m_wakeLockObj = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, TAG);
            m_wakeLockObj.acquire();
        }
    }

    void releaseWakeLock() {
        logAndWrite(">>>>>>取消点亮", LogEnum.VERBOSE, false);
        if (m_wakeLockObj != null && m_wakeLockObj.isHeld()) {
            m_wakeLockObj.setReferenceCounted(false);
            m_wakeLockObj.release();
            m_wakeLockObj = null;
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
