package com.chinanetcenter.wcs.android;

public class Config {

    //空间名称为wcs-sdk-test 用户名apiusertest
//    public static final String PUT_URL = "http://apitestuser.up0.v1.wcsapi.com";
//    public static final String PUT_URL = "http://up.wcsapi.biz.matocloud.com:8090";
    public static final String PUT_URL = "your upload domain";
    public static final String GET_URL = "http://wcsapi.biz.matocloud.com";
    public static final String MGR_URL = "http://mgr.wcsapi.biz.matocloud.com";

    // public static String baseUrl = "http://up.wcsapi.biz.matocloud.com:8090";
    // public static String GET_URL = "http://wcs1.biz.matocloud.com:99";
    // public static String MGR_URL = "http://mgr.wcsapi.biz.matocloud.com:99";

    public static String VERSION = "1.6.2";
    public static boolean DEBUGGING = false;

    //不设置默认url，必须由用户填充
    public static String baseUrl = PUT_URL;

    private Config() {
    }

}
