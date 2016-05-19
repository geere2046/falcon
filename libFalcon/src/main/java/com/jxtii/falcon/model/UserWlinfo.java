package com.jxtii.falcon.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/5/17.
 */
public class UserWlinfo extends DataSupport{

    private int id;
    private String wlId;
    private String wlName;
    private int freq;
    private int subFreq;
    private String startTime;
    private String endTime;
    private String warnStyle;
    private String receiveTels;
    private int criteria;
    private int duration;
    private int cfCount;
    private String isUpPower;

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

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public int getSubFreq() {
        return subFreq;
    }

    public void setSubFreq(int subFreq) {
        this.subFreq = subFreq;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getWarnStyle() {
        return warnStyle;
    }

    public void setWarnStyle(String warnStyle) {
        this.warnStyle = warnStyle;
    }

    public String getReceiveTels() {
        return receiveTels;
    }

    public void setReceiveTels(String receiveTels) {
        this.receiveTels = receiveTels;
    }

    public int getCriteria() {
        return criteria;
    }

    public void setCriteria(int criteria) {
        this.criteria = criteria;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCfCount() {
        return cfCount;
    }

    public void setCfCount(int cfCount) {
        this.cfCount = cfCount;
    }

    public String getIsUpPower() {
        return isUpPower;
    }

    public void setIsUpPower(String isUpPower) {
        this.isUpPower = isUpPower;
    }
}
