# Cloud Storage Android SDK

## Language
- [简体中文](README.md)
- [English](README.en.md)

## wcs-android-sdk

The ANDROID SDK is built based on the wcs cloud storage API specification and is suitable for ANDROID. Using this SDK to build your mobile APP allows you to conveniently and securely store data on the wcs cloud platform.

## Project Introduction
Project source code: [wcs-android-sdk](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/wcs-android-sdk)
sample directory: [app](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/app/src/main)
jar package: [jar](https://github.com/Wangsu-Cloud-Storage/wcs-android-sdk/tree/master/app/libs)

### Download Link
[wcs-android-sdk download link](https://wcsd.chinanetcenter.com/sdk/cnc-android-sdk-wcs.zip)

### Mobile Terminal Scene Demonstration

1) The mobile terminal requests upload credentials from the enterprise's self-built WEB server
2) The enterprise's self-built WEB server returns the constructed upload credentials to the mobile terminal
3) The mobile terminal calls the interface provided by the wcs cloud storage platform to upload files
4) After verifying the validity of the credentials, wcs cloud storage executes the interface logic requested by the mobile terminal, and finally returns the processing result to the mobile terminal

![img](https://wcsd.chinanetcenter.com/sdk/wcs-android-sdk1.png)

### Usage Guide

#### Prepare Development Environment

1. Mobile terminal development environment preparation
- Click on the official website to view and download the sdk package
- After decompression, get the jar packages in the libs directory, currently including 3 jars: wcs-android-sdk-x.x.x.jar, okhttp-3.x.x.jar, okio-1.x.x.jar
- Import the above 3 jar packages into the libs directory of the project

Eclipse:
![Copy to project libs directory](https://wcsd.chinanetcenter.com/sdk/wcs-android-sdk2.png)

1) If the ADT plugin used is version 16 or above, it will automatically put the jar into Android Dependencies and automatically complete the subsequent import of the jar package; if not, please continue to browse step 3;

2) Right-click on the project and select Properties;

3) Click Java Build Path->Libraries;

4) Click Add Jars, select wcs-android-sdk-x.x.x.jar, okhttp-3.x.x.jar, okio-1.x.x.jar under the project libs directory;

![Mobile development environment preparation 2](https://wcsd.chinanetcenter.com/sdk/wcs-android-sdk3.png)

5) Click OK.

Android Studio:

1) Select the 3 jar packages

2) Right click and select Add As Library

![Mobile development environment preparation 3](https://wcsd.chinanetcenter.com/sdk/wcs-android-sdk4.png)

3) Click OK.

- Configure network permissions: Add <uses-permission android:name="android.permission.INTERNET"/> to AndroidManifest.xml.

2. Server-side development environment preparation

For server-side development environment, please refer to wcs-Java-SDK: https://github.com/Wangsu-Cloud-Storage/wcs-java-sdk

#### Initialization

Initialization mainly completes the upload domain setting and Client parameter configuration (optional) - shard upload concurrency, response timeout, connection timeout, retry times.

1. Set upload and management domains uniformly through configuration files
```
com.chinanetcenter.wcs.android.Config.java

public static final String PUT_URL = "Your upload domain";
public static final String MGR_URL = "Your management domain";
```

2. Specify in the program
```java
FileUploader.setUploadUrl("Your upload domain");

ClientConfig config = new ClientConfig();

// Set the shard upload concurrency to 10, default value is 5 if not configured
config.setMaxConcurrentRequest(10);

// Set connection timeout, in milliseconds, default 15 seconds
config.setConnectionTimeout(15000)

// Set transfer timeout, in milliseconds, default 30 seconds
config.setSocketTimeout(30000)

// Set request failure retry times, default 1 time
config.setMaxErrorRetry(3)

FileUploader.setClientConfig(config);
```

3. Specify custom parameters through form parameters, such as file name, mimeType, deadline, etc. [Note: This is a non-required parameter]
```
import com.chinanetcenter.wcs.android.api.ParamsConf;

conf = new ParamsConf();

// Original file name
conf.fileName = '<Original file>';

// Set the name of the file to be saved to cloud storage through form parameters, not required, the file specified in the upload token has priority
conf.keyName = <fileKey>;

// Set the mimeType of the file through form parameters, not required, the system will detect the mimeType of the file by default
conf.mimeType = '<mimeType>';
// Set the number of days the file is saved through form parameters, such as 30 - the file will be automatically deleted after 30 days of saving, not required, default is permanent saving
conf.deadline = '<deadline>';
FileUploader.setParams(conf);
```

#### Custom Block and Chunk Sizes

Block size, default is 4M, must be a multiple of 4M, maximum cannot exceed 100M.
Chunk size, default is 4M, must be a multiple of 64K, maximum cannot exceed the block size.

```java
FileUploader.setBlockConfigs(4, 1024 * 4); // Set block size to 4M, chunk size to 4M
```

#### File Upload

<1> When uploading forms, you can enable returnurl for page jump. It is recommended not to set returnurl in other cases.

<2> If the file size exceeds 2M, it is recommended to use shard upload

<3> The upload domain name provided by cloud storage is a normal domain name. If you are sensitive to upload speed and have requirements, it is recommended to use upload acceleration service.


1. Normal upload (POST method)
After the user uploads the file, the upload return result is uniformly controlled by the cloud storage platform, and the specification is unified.

&emsp;&emsp;If the user specifies the returnUrl of the upload policy data, wcs cloud storage will feedback an HTTP 303 pointing to returnUrl, driving the client to execute the jump;

&emsp;&emsp;If the user does not specify the returnUrl of the upload policy data, wcs cloud storage will send feedback information to the client according to the setting of returnbody.

**Example:**

Mobile terminal code:

```java
/**
 * Upload interface example
 */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN - upload credential
         * srcFile - local file to be uploaded
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** Upload success callback **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** Upload failure callback **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** Upload progress callback **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });
    }
```

Server generates normal upload credentials: [Refer to upload credential instructions](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

2. Callback upload (POST method)

After the user uploads the file, the information returned to the client is in a custom format. 
Using this upload mode requires enabling the callbackUrl parameter of the upload policy data, and the callbackBody parameter is optional (it is recommended to use this parameter). 
*Note: returnUrl and callbackUrl cannot be specified at the same time.*

&emsp;&emsp;If the callbackBody parameter is specified, cloud storage will receive this parameter and initiate an HTTP request to callback the business server to the address specified by callbackUrl, and send data to the business server at the same time. The data content sent is specified by callbackBody. After the business server completes the callback processing, it can put data in the HTTP Response, and wcs cloud storage will respond to the client and send the data fed back by the business server to the client.
If the callbackBody parameter is not specified, cloud storage will return an empty string to the client.

**Example:**

Mobile terminal code:

```java
 /**
     * Upload interface example
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN - upload credential
         * srcFile - local file to be uploaded
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** Upload success callback **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** Upload failure callback **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** Upload progress callback **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }
```

Server generates callback upload credentials: [Refer to upload credential instructions](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

3. Notification upload (POST method)

While uploading files, users submit file processing instructions and request the wcs cloud storage platform to process the uploaded files. Since the processing operation is time-consuming, in order not to affect the client experience, the wcs cloud storage platform adopts an asynchronous processing strategy, and will automatically notify the customer server of the results after processing is completed. 
Using this upload mode requires enabling the persistentOps parameter and persistentNotifyUrl parameter of the upload policy data.

**Example:**

Mobile terminal code:

```java
/**
     * Upload interface example
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN - upload credential
         * srcFile - local file to be uploaded
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            /** Upload success callback **/
            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            /** Upload failure callback **/
            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            /** Upload progress callback **/
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }
```

Server generates notification upload credentials: [Refer to upload credential instructions](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

4. Shard upload (POST method)

Uploading large files on mobile terminals takes a long time. Once an exception occurs during transmission, the entire file content needs to be retransmitted, which affects user experience. To avoid this problem, a shard upload mechanism is introduced.

The shard upload mechanism is to split a large file into multiple blocks of custom size, and then upload these blocks in parallel. If a block fails to upload, the client only needs to re-upload this block.

*Note: The maximum size of each block cannot exceed 100M; the minimum cannot be less than 4M, otherwise the default 4M will be used for splitting.*

**Example**

Mobile terminal code:

```java
private static final long DEFAULT_BLOCK_SIZE = 1 * 1024 * 1024;
/**
* context - current context of the application
* uploadToken - upload Token
* ipaFile - file to be uploaded
* DEFAULT_BLOCK_SIZE - block size
*/
FileUploader.sliceUpload(context, uploadToken, ipaFile, DEFAULT_BLOCK_SIZE, new SliceUploaderListener() {
         
          /** Upload success callback **/
          @Override
          public void onSliceUploadSucceed(JSONObject jsonObject) {
            Log.d("CNCLog", "slice upload succeeded.");
          }

          /** Upload failure callback **/
          @Override
          public void onSliceUploadFailured(OperationMessage operationMessage) {
            Log.d("CNCLog", "slice upload failured.");
          }

          /** Upload progress callback **/
          @Override
          public void onProgress(long uploaded, long total) {
            Log.d("CNCLog", String.format(Locale.CHINA, "uploaded : %s, total : %s", uploaded, total));
          }
});
```

Server generates shard upload credentials: [Refer to upload credential instructions](https://wcs.chinanetcenter.com/document/API/Token/UploadToken)

#### File Integrity Verification
If you need to verify whether the successfully uploaded file is complete, you can calculate the file hash on the client side and compare it with the hash returned by cloud storage after successful upload. If the hashes are consistent, it indicates that the file is complete.

*Note: The calculation of file hash value consumes more resources, please use it with caution*

**Example**
```
import com.chinanetcenter.wcs.android.utils.WetagUtil;

WetagUtil.getEtagHash(file);
```