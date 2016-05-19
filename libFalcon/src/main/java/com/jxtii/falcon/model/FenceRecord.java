package com.jxtii.falcon.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/5/16.
 */
public class FenceRecord  extends DataSupport {

    private int id;
    private String startTime;
    private String stopTime;
    private String status;//1有效2过期3失效

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
