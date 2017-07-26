package com.chinanetcenter.wcs.android;

public class Config {

    public static final String PUT_URL = "your upload domain";
    public static final String GET_URL = "http://wcsapi.biz.matocloud.com";
    public static final String MGR_URL = "http://mgr.wcsapi.biz.matocloud.com";


    public static String VERSION = "1.6.2";
    public static boolean DEBUGGING = false;

    //不设置默认url，必须由用户填充
    public static String baseUrl = PUT_URL;

    private Config() {
    }

}
