package com.chinanetcenter.wcs.android.api;

/**
 * @author :yanghuan
 * @version :1.6.2
 * @package : com.chinanetcenter.wcs.android.api
 * @class : TokenParams
 * @time : 2017/6/24 16:38
 * @description :获取token所需参数
 */
public class TokenParams {

    /**
     * 用户 id， portal 分配给接口使用者的账号id，主账号和子账号都具有不同的 ID。
     * 服务端需要对其授权范围进行校验，账号只能获取其权限内的信息。
     */
    public String userId;


    /**
     * 校验凭证
     */
    public String token;

    /**
     * 上传文件路径
     */
    public String filePath;

    /**
     * 上传文件名
     */
    public String fileName;

    /**
     * 视频域名(可选)
     */
    public String domain;

    /**
     * 一体化命令，经过 urlsafe base64 编码的JSON 字符串，
     * notifyUrl:一体化回调接口，
     * adId： 前置广告 ID，
     * backAdId:后置广告 id，
     * wmTemplateName:  水印模板名，
     * tcTemplateName： 转码组合名，
     * vframe:截图参数：offset 起始时间(单位秒)，w,h 是宽高（ 单位 px），n:截图数量，优先于 interval。
     * Interval：截图间隔时间，
     * detectNotifyRule：鉴定通知规则， all 全部通知 ， porn 通知涉黄的图片 ， sexy 通知性感图片， normal 通知正常图片 ，exception 通知鉴定异常的图片,
     * 不传就不鉴定， 可组合， 使用英文分号分隔 porn;sexy。
     * notifyUrl: 截图&鉴定回调地址。
     * 例如： {notifyUrl：http://someurl;adId:1,backAdId:2,wmTemplateName:3, tcTemplateName:4,
     * vframe:{offset:1,w:100,h:100,n:4,interval,4, sprite:4x5,detectNotifyRule:porn;sexy,notifyUrl:http://yoururl.com}}
     * (可选)
     */
    public String cmd;

    /**
     * 上传策略，是否覆盖。只能为 0 或 1。(可选)
     */
    public String overwrite;

    /**
     * 视频上传来源，要求只能是 web， sdk，或者web_js_sdk1.1.0。默认为 sdk。(可选)
     */
    public String videoSource;

}
