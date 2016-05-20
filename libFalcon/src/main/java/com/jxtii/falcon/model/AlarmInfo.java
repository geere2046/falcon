package com.jxtii.falcon.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/5/18.
 */
public class AlarmInfo extends DataSupport{

    private int id;
    private String wlId;
    private String wlName;
    private String action;
    private double lat;
    private double lng;
    private String gpstime;
    private String status;// 参考CommUtil.STATUS_VAILD等

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWlId() {
        return wlId;
    }

    public void setWlId(String wlId) {
        this.wlId = wlId;
    }

    public String getWlName() {
        return wlName;
    }

    public void setWlName(String wlName) {
        this.wlName = wlName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getGpstime() {
        return gpstime;
    }

    public void setGpstime(String gpstime) {
        this.gpstime = gpstime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
