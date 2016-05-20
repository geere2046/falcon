package com.jxtii.falcon.bean;

public class ParseResultBean {

    protected String code;
    protected String desc = "";
    protected double latitude = 0;
    protected double longitude = 0;
    protected String province;
    protected String city;
    protected String county;
    protected String road;
    protected String LOCTYPE;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getLOCTYPE() {
        return LOCTYPE;
    }

    public void setLOCTYPE(String lOCTYPE) {
        LOCTYPE = lOCTYPE;
    }

}
