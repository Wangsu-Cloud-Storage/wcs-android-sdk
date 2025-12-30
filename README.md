# 云存储 Android SDK

## 语言 / Language
- [简体中文](README.md)
- [English](README.en.md)

## wcs-android-sdk

ANDROID SDK基于wcs云存储API规范构建，适用于ANDROID。使用此SDK构建您的移动APP，能让您非常便捷地将数据安全地存储到wcs云平台上。

## 工程介绍
工程源码：[wcs-android-sdk](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/wcs-android-sdk)
sample目录：[app](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/app/src/main)
jar包：[jar](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/app/libs)

### 下载链接
[wcs-android-sdk下载链接](https://wcsd.chinanetcenter.com/sdk/cnc-android-sdk-wcs.zip)

### 移动端场景演示

1) 移动端向企业自建WEB服务端请求上传凭证 
2) 企业自建WEB服务端将构建好的上传凭证返回移动端 
3) 移动端调用wcs云存储平台提供的接口上传文件 
4) wcs云存储在检验凭证合法性后，执行移动端请求的接口逻辑，最终返回给移动端处理结果 

![img](https://wcs.chinanetcenter.com/indexNew/image/wcs/wcs-android-sdk1.png)

### 使用指南

#### 准备开发环境

一、移动端开发环境准备 
- 在官网点击查看下载sdk包
- 解压后在libs目录下得到jar包，目前包括wcs-android-sdk-x.x.x.jar、okhttp-3.x.x.jar、okio-1.x.x.jar3个jar
- 将以上3个jar包导入工程的libs目录

Eclipse:
![复制到项目里libs目录](https://wcs.chinanetcenter.com/indexNew/image/wcs/wcs-android-sdk2.png)

1)如果使用的ADT插件是16及以上，则会自动把jar放到Android Dependencies中，并自动完成后续的jar包的导入；如果不是，请继续浏览第3步；

2)右键选择工程，选择Properties； 

3)点击Java Build Path->Libraries；

4)点击Add Jars，选择工程libs目录下的wcs-android-sdk-x.x.x.jar、okhttp-3.x.x.jar、okio-1.x.x.jar； 

![移动开发环境准备2](https://wcs.chinanetcenter.com/indexNew/image/wcs/wcs-android-sdk3.png)

5)点击OK。 

Android Studio:

1)选中3个jar包

2)右键选择Add As Library

![移动开发环境准备3](http://doc-pics.w.wcsapi.biz.matocloud.com/sdk/%E7%A7%BB%E5%8A%A8%E7%AB%AF%E5%BC%80%E5%8F%91%E7%8E%AF%E5%A2%83%E9%85%8D%E7%BD%AE-android.png)

3)点击OK。

- 配置网络权限：AndroidManifest.xml添加入<uses-permission android:name="android.permission.INTERNET"/>。

二、服务端开发环境准备 

服务端开发环境请参考wcs-Java-SDK: https://github.com/Wangsu-Cloud-Storage/wcs-java-sdk

#### 初始化

初始化主要完成upload domain设置、Client参数配置（可选）-分片上传并发数、响应超时时间、连接超时时间、重试次数。
1. 通过配置文件统一设置上传，管理域名
```
com.chinanetcenter.wcs.android.Config.java

public static final String PUT_URL = "您的上传域名";
public static final String MGR_URL = "您的管理域名";
```

2. 在程序中指定
```java
    FileUploader.setUploadUrl("您的上传域名");
    
    ClientConfig config = new ClientConfig();
    
    // 设置分片上传并发数为10，不配置时默认值为5
    config.setMaxConcurrentRequest(10);
    
    // 设置链接超时时间，单位毫秒，默认15秒
    config.setConnectionTimeout(15000)
    
    // 设置传输超时时间，单位毫秒，默认30秒
    config.setSocketTimeout(30000)
    
    // 设置请求失败重试次数，默认1次
    config.setMaxErrorRetry(3)
    
    FileUploader.setClientConfig(config);
```

3. 通过表单参数，指定自定义参数，如文件名，mimeType，deadline等【注：此为非必填参数】
```
import com.chinanetcenter.wcs.android.api.ParamsConf;

conf = new ParamsConf();

// 原始文件名称
conf.fileName = '<原始文件>';

// 通过表单参数设置文件保存到云存储的名称，非必填，上传token中指定的文件优先
conf.keyName = <fileKey>;

// 通过表单参数设置文件的mimeType，非必填，默认由系统检测文件的mimeType
conf.mimeType = '<mimeType>';
// 通过表单参数设置文件的保存天数，如30-文件保存30天后自动删除，非必填，默认永久保存
conf.deadline = '<deadline>';
FileUploader.setParams(conf);
```

#### 自定义块、片的大小

块的大小，默认为4M，必须是4M的倍数，最大不能超过100M。
片的大小，默认为4M，必须是64K的倍数，最大不能超过块的大小。

```java
    FileUploader.setBlockConfigs(4, 1024 * 4); //设置块大小为4M，片大小为4M
```

#### 文件上传

<1>表单上传时可开启returnurl进行页面跳转，其他情况下建议不设置returnurl。

<2>若文件大小超过2M，建议使用分片上传 

<3>云存储提供的上传域名为普通域名，若对上传速度较为敏感，有要求的客户建议采用上传加速服务。


1.普通上传（POST方式） 
用户在上传文件后，上传返回结果由云存储平台统一控制，规范统一化。

&emsp;&emsp;如果用户指定上传策略数据的returnUrl，wcs云存储将反馈一个指向returnUrl的HTTP 303，驱动客户端执行跳转；

&emsp;&emsp;如果用户没指定上传策略数据的returnUrl，wcs云存储根据returnbody的设定向客户端发送反馈信息。

**范例：**

移动端代码：

```java
/**
 * 上传接口范例
 */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-上传凭证
         * srcFile-本地待上传的文件
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** 上传成功回调 **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** 上传失败回调 **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** 上传进度回调 **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });
    }
```

服务端生成普通上传凭证： [参考上传凭证说明](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

2.回调上传（POST方式） 

用户上传文件后，对返回给客户端的信息进行自定义格式。 
使用该上传模式需要启用上传策略数据的callbackUrl参数,而callbackBody参数可选（建议使用该参数）。 
*注意：returnUrl和callbackUrl不能同时指定。*

&emsp;&emsp;如果指定了callbackBody参数，云存储将接收此参数，并向callbackUrl指定的地址发起一个HTTP请求回调业务服务器，同时向业务服务器发送数据。发送的数据内容由callbackBody指定。业务服务器完成回调的处理后，可以在HTTP Response中放入数据，wcs云存储会响应客户端，并将业务服务器反馈的数据发送给客户端。
如果不指定callbackBody参数，云存储将返回空串给客户端。

**范例：**

移动端代码：

```java
 /**
     * 上传接口范例
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-上传凭证
         * srcFile-本地待上传的文件
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** 上传成功回调 **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** 上传失败回调 **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** 上传进度回调 **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }
```

服务端生成回调上传凭证： [参考上传凭证说明](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

3.通知上传 (POST方式) 

用户在上传文件的同时，提交文件处理指令，请求wcs云存储平台对上传的文件进行处理。由于处理操作较耗时，为了不影响客户端的体验，wcs云存储平台采用异步处理策略，处理完成后将结果自动通知客户服务端。 
使用该上传模式需要启用上传策略数据的persistentOps参数和persistentNotifyUrl参数。

**范例：**

移动端代码：

```java
/**
     * 上传接口范例
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-上传凭证
         * srcFile-本地待上传的文件
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** 上传成功回调 **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** 上传失败回调 **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** 上传进度回调 **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }
```

服务端生成通知上传凭证： [参考上传凭证说明](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

4.分片上传（POST方式） 

移动端上传大文件需要耗费较长时间，一旦在传输过程中出现异常，文件内容需全部重传，影响用户体验，为避免这个问题，引进分片上传机制。 

分片上传机制是将一个大文件切分成多个自定义大小的块，然后将这些块并行上传，如果某个块上传失败，客户端只需要重新上传这个块即可。 

*注意：每个块的最大大小不能超过100M；最小不能小于4M，否则将会采用默认4M去切分。*

**范例**

移动端代码：

```java
private static final long DEFAULT_BLOCK_SIZE = 1 * 1024 * 1024;
/**
* context-应用当前的上下文
* uploadToken-上传Token
* ipaFile-需要上传的文件
* DEFAULT_BLOCK_SIZE-块大小
*/
FileUploader.sliceUpload(context, uploadToken, ipaFile, DEFAULT_BLOCK_SIZE, new SliceUploaderListener() {
         
          /** 上传成功回调 **/
          @Override
          public void onSliceUploadSucceed(JSONObject jsonObject) {
            Log.d("CNCLog", "slice upload succeeded.");
          }

          /** 上传失败回调 **/
          @Override
          public void onSliceUploadFailured(OperationMessage operationMessage) {
            Log.d("CNCLog", "slice upload failured.");
          }

          /** 上传进度回调 **/
          @Override
          public void onProgress(long uploaded, long total) {
            Log.d("CNCLog", String.format(Locale.CHINA, "uploaded : %s, total : %s", uploaded, total));
          }
});
```

服务端生成分片上传凭证： [参考上传凭证说明](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

#### 文件完整性校验
如果需要校验上传成功的文件是否完整，可在客户端计算文件hash，并和上传成功后云存储返回的hash进行对比，如果hash一致，则表明文件是完整的。

*注意：文件hash值的计算比较消耗资源，请谨慎使用*

**范例**
```
import com.chinanetcenter.wcs.android.utils.WetagUtil;

WetagUtil.getEtagHash(file);
```
