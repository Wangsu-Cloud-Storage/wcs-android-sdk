# Cloud Storage Android SDK
## 语言 / Language
- [简体中文](README.md)
- [English](README.en.md)

## wcs-android-sdk
### Prerequisites
- Object Storage is activated.
- The AccessKey and SecretKey are created


### Download SDK
[Android SDK](https://wcsd.chinanetcenter.com/sdk/cnc-android-sdk-wcs.zip)

## How to use it
### Prepare the development environment
#### 1. Development environment preparation in mobile end
- Download SDK package
- Decompress it, and you will get jar packages under libs directory. Currently we have 3 jars: wcs-android-sdk-x.x.x.jar, okhttp-3.x.x.jar and okio-1.x.x.jar
- Import the 3 jar packages to libs directory in project.


#### For Eclipse
![image](https://user-images.githubusercontent.com/98135632/151097790-940f688d-1258-48fe-b8b4-a791578c4434.png)

1)If the ADT plugin is above 16, it will automatically put jat to Android Dependencies, and it will also finish the following importing jar packages; if the ADT plugin isn't above 16, please jump to 3).

2)Right click on project, go to Properties;

3)Click Java Build Path->Libraries

4)Click Add Jars, choose wcs-android-sdk-x.x.x.jar, okhttp-3.x.x.jar and okio-1.x.x.jar under directory libs.
![image](https://user-images.githubusercontent.com/98135632/151097833-9326522b-695b-4dae-84ee-6ec091e71917.png)


5)Click OK

#### For Android Studio

1)Select the 3 jar packages

2)Right click select ***Add As Library***

3)Click OK

- Configure network permission, AndroidManifest.xml.


### 2. Development environment preparation in server end

Please refer to wcs-Java-SDK: https://github.com/CDNetworks-Object-Storage/wcs-java-sdk

#### Initialization

Initialization is mainly to finish upload domain config, client para config (optional), multipart upload concurrency, response timeout, connect timeout, retry times, etc.

##### 2.1 Set upload/management domain by config file
```
com.chinanetcenter.wcs.android.Config.java

public static final String PUT_URL = "Your upload domain";
public static final String MGR_URL = "You management domain";
```

##### 2.2 Specify in the program
```
    FileUploader.setUploadUrl("Your upload domain");
    
    ClientConfig config = new ClientConfig();
    
    // Set the concurrency of multipart upload ad 10, the default value is 5
    config.setMaxConcurrentRequest(10);
    FileUploader.setClientConfig(config);
```

##### 2.3 Set parameters of datasheet

```
import com.chinanetcenter.wcs.android.api.ParamsConf;

conf = new ParamsConf();

// Original file name
conf.fileName = '<Original file>';

// Set the file name in WCS
conf.keyName = <fileKey>;

// Set the mimeType of file
conf.mimeType = '<mimeType>';

// Set DDL of file
conf.deadline = '<deadline>';
FileUploader.setParams(conf);
```
####  Customize the size of block and part
The size of block is 4M as default, it must be a multiple of 4M, and the max value can't be exceed 100M.
The size of part is 256KB as default, it must be a multiple of 64K, and the max value can't be exceed the size of block.
```
  FileUploader.setBlockConfigs(8, 512); //Set block size as 8M, and part size as 512KB.
```

#### Upload

<1> When uploading the datasheet, you can enable ***returnurl*** for page jumping, otherwise it is recommended not to set ***returnurl***.

<2> If the file size exceeds 2M, multipart upload is recommended.

<3> Cloud Storage provides a default upload domain for upload, if the upload speed is more sensitive, customers with such requirements is suggested to use CDNetworks CDN acceleration service.


##### 1.Normal upload（POST） 

After the user uploads the file, the returned result is controlled and standardized by cloud storage.

- If the user specifies the ***returnUrl*** for uploading policy data, cloud storage will feedback an ***HTTP 303*** to ***returnUrl***, driving the client end to perform the jump;
- If the user does not specify a ***returnUrl*** for uploading policy data, cloud storage sends feedback to the client end based on the settings of The ***ReturnBody***.

###### Example

Code in mobile end：

```
/**
 * Example of upload interface
 */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-local
         * srcFile-The file which requires to be uploaded from local
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });
    }

```


##### 2.Call back upload（POST）
After the user uploads the file, user can customize the format of the information returned to the client. Using this upload mode requires enabling the ***callbackUrl*** parameter of the upload policy data, and the ***callbackBody*** parameter is optional (it is recommended). Note: ***returnUrl*** and ***callbackUrl*** cannot be specified together.

- If a ***callbackBody*** parameter is specified, cloud storage will receive it and initiates an HTTP request to callback to server at the address specified in the ***callbackUrl***, sending data to server. The content of the data sent is specified by the ***callbackBody***. After the server completes the callback processing, it can put the data in the HTTP Response, and cloud storage will respond to the client and send the data fed back by server to the client. 
- If the ***callbackBody*** parameter is not specified, cloud storage returns an empty string to the client.

###### Example

Code in mobile end:
```
 /**
     * example of upload interface
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-token
         * srcFile-The file which requires to be uploaded from local
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }

```

##### 3.Upload with notification (POST)

At the same time the user uploates the file, it will submit the file processing instruction, requesting cloud storage to process the uploaded file. Due to the time-consuming processing operation, in order not to affect the experience of the client, cloud storage adopts the asynchronous processing strategy, and automatically informs the client service side of the result after the processing is completed. Using this upload pattern requires the ***persistentOps*** parameter and the ***persistentNotifyUrl*** parameter to be enabled for the upload policy data.

###### Example

Code in mobile end
```
/**
     * example of upload interface
     */
private void uploadFile(File srcFile) {
/**
         * UPLOADER_TOKEN-token
         * srcFile-The file which requires to be uploaded from local
         */
        FileUploader.upload(UPLOADER_TOKEN, srcFile, new FileUploaderListener() {

            @Override
            public void onSuccess(int status, JSONObject responseJson) {
                Log.d(TAG, "responseJson : " + responseJson.toString());
            }

            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.e(TAG, "errorMessage : " + operationMessage.toString());
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                Log.d(TAG, String.format("Progress %d from %d (%s)", bytesWritten, totalSize, (totalSize > 0) ? ((float) bytesWritten / totalSize) * 100 : -1));
            }
        });

    }

```



##### 4.Multipart upload（POST）


It takes a long time to upload large files on the mobile end. Once abnormalities occur in the transmission process, all file contents need to be retransmitted, which will affect the user experience. To avoid this problem, multipart upload mechanism is introduced.

Multipart upload mechanism is to slice a large file into many custom sized blocks, and then upload these blocks in parallel. If a block upload fails, the client just needs to re-upload the block.

Note: The maximum size of each block should not exceed 100M; It must not be less than 4M, otherwise the default value will be set as 4M.

###### Example

Code in mobile end
```
private static final long DEFAULT_BLOCK_SIZE = 1 * 1024 * 1024;
/**
* context
* uploadToken-Token
* ipaFile-the file requires to be uploaded
* DEFAULT_BLOCK_SIZE-block size
*/
FileUploader.sliceUpload(context, uploadToken, ipaFile, DEFAULT_BLOCK_SIZE, new SliceUploaderListener() {
          @Override
          public void onSliceUploadSucceed(JSONObject jsonObject) {
Log.d("CNCLog", "slice upload succeeded.");
}

@Override
public void onSliceUploadFailured(OperationMessage operationMessage) {
Log.d("CNCLog", "slice upload failured.");
}

@Override
public void onProgress(long uploaded, long total) {
Log.d("CNCLog", String.format(Locale.CHINA, "uploaded : %s, total : %s", uploaded, total));
}
});

```


####  Integrity checker 

If the integrity of the successfully uploaded file needs to be verified, the file hash can be calculated on the client side and compared with the hash returned by cloud storage after the successful upload. If the hash is consistent, it indicates that the file is complete.

Note: Calculations of file hash values consume resources, so use it with caution


###### Example
```
import com.chinanetcenter.wcs.android.utils.WetagUtil;

WetagUtil.getEtagHash(file);

```
```
