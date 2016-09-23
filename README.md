# ZxingLite
## 摘要
一款精减版的扫码控件  只有扫码,没有其他定制

## 截图
![1](https://github.com/yingLanNull/ZxingLite/blob/master/show/Screenshot_2016-09-23-11-58-23.png)
![2](https://github.com/yingLanNull/ZxingLite/blob/master/show/Screenshot_2016-09-23-11-58-17.png)
![3](https://github.com/yingLanNull/ZxingLite/blob/master/show/Screenshot_2016-09-23-11-58-08.png)

## Demo 下载APK体验
[Download Demo](https://github.com/yingLanNull/ZxingLite/blob/master/show/app-debug.apk)

## Usage 使用方法
### Step 1
#### Gradle 配置
```
dependencies {
    compile 'com.yingyan.zxinglite:zxing-lib:1.0.2'
}
```

### Step 2

#### In Java Code
```
	step1

	    Intent intent = new Intent();
        intent.setClass(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);

    step2
    
        @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (null != data && requestCode == REQUEST_CODE) {
                    Bundle bundle = data.getExtras();
                    String scanResult = bundle.getString(Intents.Scan.RESULT);
                }
            }

```

## LICENSE

    Apache License Version 2.0

