package com.jxtii.falcon.webservice;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jxtii.falcon.bean.ParseResultBean;
import com.jxtii.falcon.bean.ResultBean;
import com.jxtii.falcon.util.CommUtil;
import com.jxtii.falcon.util.MD5;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huangyc on 2016/5/20.
 */
public class RestWebserviceClient {

    String TAG = RestWebserviceClient.class.getSimpleName();

    ResultBean getDoPostResponseDataByURL(String url, String ip,
                                                 String name, String password, String charset, String json,
                                                 Context ctx) {

        ResultBean resultBean = new ResultBean();
        StringBuffer response = new StringBuffer();

        if (!CommUtil.isNetworkAvailable(ctx)) {// 无网络时不发起网络任务（省电）。
            resultBean.setCode("1");
            resultBean.setDesc("无网络时不发起网络请求");
            return resultBean;
        }

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(url);
        client.getHostConfiguration().setHost(ip, 8080, "http");

        method.setRequestHeader("username", name);
        method.setRequestHeader(
                "password",
                MD5.compile(password
                        + new SimpleDateFormat("yyyyMMdd").format(new Date())));
        method.setRequestHeader("Content-Type",
                "application/x-www-form-urlencoded;charset=UTF-8");

        if (json != null) {
            RequestEntity requestEntity = new StringRequestEntity(json);
            method.setRequestEntity(requestEntity);
        }

        try {
            client.executeMethod(method);
            Log.i(TAG,
                    "REST method.getStatusCode()>>>" + method.getStatusCode());
            Header[] heads = method.getResponseHeaders();
            for (int i = 0; i < heads.length; i++) {
                // System.out.println(heads[i].getName());
                // System.out.println(heads[i].getValue());
            }
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(method.getResponseBodyAsStream(),
                                charset));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                byte[] responseBody = method.getResponseBodyAsString()
                        .getBytes(method.getResponseCharSet());
                String res = new String(responseBody, "utf-8");
                resultBean.setCode("1");
                resultBean.setDesc(res);
                return resultBean;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultBean.setCode("1");
            resultBean.setDesc("请求发生异常");
            return resultBean;
        } finally {
            method.releaseConnection();
        }
        resultBean.setCode("0");
        resultBean.setDesc(response.toString());
        return resultBean;
    }

    public ResultBean getGaoDeGeoCodeLocInfo(double mlat, double mlng,
                                             String coortype, Context ctx) {
        String json = "";

        if ("gcj".equalsIgnoreCase(coortype)) {
            json = "{\"longitude\":\"" + mlng + "\",\"latitude\":\"" + mlat
                    + "\",\"type\":\"gcj\"}";
        } else if ("wgs".equalsIgnoreCase(coortype)) {
            json = "{\"longitude\":\"" + mlng + "\",\"latitude\":\"" + mlat
                    + "\",\"type\":\"wgs\"}";
        }

        ResultBean resultBean = getDoPostResponseDataByURL(
                "/lbs/ws/rest/sample/parseLatiLongToAddress",
                CommUtil.REST_IP, CommUtil.REST_ACCOUNT,
                CommUtil.REST_PASSWORD, "utf-8", json, ctx);

        if (resultBean.getCode() == "0") {
            ParseResultBean pr = (ParseResultBean) JSON.parseObject(resultBean.getDesc(), ParseResultBean.class);
            String code = pr.getCode() == null ? "1" : pr.getCode();
            if ("0".equals(code)) {
                String addr = pr.getDesc() == null ? "地址解析为空" : pr.getDesc();
                resultBean.setDesc(addr);
            } else {
                String addr = pr.getDesc() == null ? "地址解析为空" : pr.getDesc();
                resultBean.setDesc(addr);
            }
        } else {
            resultBean.setCode("1");
            resultBean.setDesc("地址解析失败！");
        }
        return resultBean;
    }
}
