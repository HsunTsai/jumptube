package com.turtle.hsun.jumptube.API;

/**
 * Created by hsun on 2017/5/19.
 */

public class ResponseModel {
    private Integer ResponseCode;
    private String Messgae;

    public Integer getResponseCode() {
        return ResponseCode;
    }
    public void setResponseCode(Integer responseCode) {
        ResponseCode = responseCode;
    }

    public String getMessgae() {
        return Messgae;
    }
    public void setMessgae(String messgae) {
        Messgae = messgae;
    }
}
