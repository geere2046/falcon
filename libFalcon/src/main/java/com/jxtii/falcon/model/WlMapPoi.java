package com.jxtii.falcon.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/5/17.
 */
public class WlMapPoi extends DataSupport {

    private int id;
    private String wlId;
    private String poiId;
    private String poiName;
    private int mapAreaType;
    private double lat;
    private double lng;
    private int pointOrder;

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

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
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

    public int getPointOrder() {
        return pointOrder;
    }

    public void setPointOrder(int pointOrder) {
        this.pointOrder = pointOrder;
    }

    public int getMapAreaType() {
        return mapAreaType;
    }

    public void setMapAreaType(int mapAreaType) {
        this.mapAreaType = mapAreaType;
    }
}
