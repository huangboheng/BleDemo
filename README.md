```minSdkVersion 18```

Android sdk 18 才引入的 低功耗蓝牙 

```xml
<uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true"/>
```

在 AndroidManifest中加入以上 代码，确保设备有 低功耗蓝牙

#工程目录说明

 - app demo (依赖 bongsdk)

 
 主要包含对于  BleManger，BleScanner, Bongsdk ,BongCommandHelper的使用。
 - bongsdk 指令封装 (依赖 blemodule 和 bongSdk 数据解析)（源码开放）

 二次包装 Bongsdk CommandAPi ， AppMsgCoder  ，BongCoder 类，方便使用。
 同过 maven 方式引入 BongSdk 解析包.
 - blemodule 蓝牙操作封装（源码开放）
	
	以 Request 形式包装 蓝牙操作。

 - BongSdk 数据解析包（闭源）

 通过gradle compile 方式引入。 地址在私有 Maven库 上：url 'http://112.124.27.178:9093/repository/maven-public/'
 
 gradle 脚本如下:
 
 ```groovy
 	repositories {
        jcenter()

        maven {
            url 'http://112.124.27.178:9093/repository/maven-public/'
        }
    }
    
	dependencies {
	    compile 'cn.bong.android:bongsdk:0.1.4'
	}
 
 ```
目前最新版本 0.1.4,请勿使用 BongSdk.getVersion() 已废弃。 
由于代码混淆，调试的请提供 sdk 版本。
程序在混淆代码的时候注意排除所有依赖 BongSdk 中提供的包，在progurad 文件中指定只混淆自己的源代码，不混淆所有的第三方库。
 
 
----------


# app 
功能点代码示例

### 初始化 BongSdk
com.example.rqg.bledemo.BongApp

```java

public class BongApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BongSdk.enableDebug(true);
        BongSdk.setUser(175, 25f, Gender.MALE);

        BongSdk.initSdk(this);
    }
}
```



###BleScanner 蓝牙扫描
com.example.rqg.bledemo.SelectActivity

 - 开启扫描接收扫描到的设备。注意监测蓝牙是否开启，否侧会产生错误。
 

```java
        mBleScanner.startLeScan(this, new BleScanCallback() {
            @Override
            public void onScanResult(BleDevice device) {
	            //接收扫描到的蓝牙设备
                mBleDeviceHashSet.add(device);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });
```

 - 停止扫描，退出页面，或者停止业务的时候一定要停止扫描。
 

```java
        mBleScanner.stopLeScan();

```


----------


### 蓝牙控制类 BleManager（Bong3Hr）
com.example.rqg.bledemo.MainActivity

 - 权限，Sdk 23 之后要注意权限申请
 

```xml
	<uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

 - BleManger 初始化，连接

```java
           mBleManger = new XBleManager(getApplication());
           mBleManger.connect(ble_mac, null);
```

 - BleManger 断开并退出，断开后可以重新连接其他设备
 

```java
        if (mBleManger != null) {
            mBleManger.disconnect();
            mBleManger.quit();
            mBleManger = null;
        }
```

###三种数据通信形式 

	com.example.rqg.bledemo.MainActivity

 - 写入一次不接收

	

```java

    /**
     * 示例
     * <p>
     * 写入一次不接收数据（会接收蓝牙协议确认信号）
     * <p>
     * 注意此处用的是 {@link XWriteRequest} And {@link XResponse}
     */
    public void onClickVibrate() {


        String format = encodeVibrateString(5);

        byte[] bytes = BongUtil.hexStringToBytes(format);
        mBleManger.addRequest(new XWriteRequest(bytes, new XResponse() {
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }

            @Override
            public void onCommandSuccess() {

            }
        }));

    }

```

- 写入一次接收一次

```java

    /**
     * 示例
     * <p>
     * 写入一次接收一次
     * <p>
     * 注意此处用的是 {@link XPerReadRequest} And {@link XPerReadResponse}
     */
    public void onClickReadBattery() {
        //write once and read once

        byte[] bytes = BongUtil.hexStringToBytes("2600000010");


        mBleManger.addRequest(new XPerReadRequest(bytes,
                new XPerReadResponse() {
                    @Override
                    public void onReceive(byte[] rsp) {
                        if (rsp != null && rsp.length > 10) {
                            int bu = (rsp[10] & 0x000000ff);
                            mBinding.batterValue.setText("Battery: " + bu);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onCommandSuccess() {

                    }
                }));
    }

```
 - 写入一次接收多次

```java
    /**
     * 示例
     * <p>
     * 写入一次接收多次
     * <p>
     * 注意此处用的是 {@link XReadRequest} And {@link XReadResponse}
     */
    public void onClickReadSport() {

        //write once and read multi times until receive "end" or "success"

        long endtime = System.currentTimeMillis();

        long beginTime = endtime - TimeUnit.HOURS.toMillis(3);

        //命令拼接
        String s = "2000000013" + getStrTimeForHex(beginTime) + getStrTimeForHex(endtime);

        byte[] bytes = BongUtil.hexStringToBytes(s);

        count = 0;


        mBleManger.addRequest(new XReadRequest(bytes,
                new XReadResponse() {
                    @Override
                    public void onReceive(List<byte[]> rsp) {
                        //all data
                    }

                    @Override
                    public void onReceivePerFrame(byte[] perFrame) {
                        setSportCount(count++);
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void onCommandSuccess() {

                    }
                }));

    }

```

 - 写入多次

```java
//write once ,read once
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XPerReadResponse;

//write once ,read multi
import fantasy.rqg.blemodule.x.request.XReadRequest;
import fantasy.rqg.blemodule.x.request.XReadResponse;

//write only
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;

```

**以上每组 Request 和Response 组合使用完成一种通讯方式，其实每个request 都有两种 构建函数。 可以只有一个蓝牙Frame （一个byte[]对应一次蓝牙发送），也可以包含多条指令。由于 bong 手环绝大部分命令都是一个 Frame就完成了，所以我区分对待了一下。**

**换而言之，XperReadRequest， XReadRequest，XWriteRequest 都是可以发送多条指令的。**

**使用场景： 消息提醒，由于消息提醒包含消息内容，不可能一次发送完成。详情见 `Bong3HRNotifyHandler` And `AppMsgCoder`**



----------


###BongCommandHelper bong 命令发送辅助类

com.example.rqg.bledemo.CommandActivity

 - 初始化
 

```java
        mBongCommandHelper = new BongCommandHelper(mBleManager);
```
mBlemanager 不能为空

 - 同步数据
 

```java
 /**
     * 同步数据 包括从bong 手环获取数据的部分，耗时长
     *
     * @param view
     */
    public void syncAndLogBlock(View view) {

        mBongCommandHelper.syncDataFromBong(new ResultCallback() {
            @Override
            public void finished() {
                Log.d(TAG, "syncAndLogBlock finished() called");
                //成功,从sdk 中获取数据

                long end = System.currentTimeMillis() / 1000;

                long start = end - TimeUnit.HOURS.toSeconds(3);
                //获取三个小时内的block 数据

                List<BongBlock> bongBlockByTime = BongSdk.getBongBlockByTime(start, end);

                List<DBCurve> curveByTime = BongSdk.getCurveByTime(start, end);

                List<DBHeart> heartByTime = BongSdk.getHeartByTime(start, end);

                Log.d(TAG, "finished: " + bongBlockByTime.size());

                for (BongBlock bb : bongBlockByTime) {
                    Log.d(TAG, bb.toString());
                }

                Log.i(TAG, "curve");
                for (DBCurve dc : curveByTime) {
                    Log.d(TAG, LogUtil.formatCurve(dc));
                }

                Log.i(TAG, "heart rate");
                for (DBHeart dh : heartByTime) {
                    Log.d(TAG, "" + new Date(dh.getTimestamp() * 1000) + "  " + dh.getHeart() + "  " + dh.getManual());

                }


            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError: ", t);
            }
        });
    }

```

 - 读取电量
 

```java
    public void readBattery(View view) {
        mBongCommandHelper.readBattery(new BatteryCallback() {
            @Override
            public void onReadBatter(int remain) {
                Log.d(TAG, "onReadBatter() called with: remain = [" + remain + "]");
            }

            @Override
            public void finished() {

            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError: ", t);
            }
        });
    }

```

 - 其他发送指令
 

```java
    public void vibrate(View view) {
        mBongCommandHelper.vibrateBong(new ResultCallback() {
            @Override
            public void finished() {
                Log.d(TAG, "finished() called");
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError: ", t);
            }
        });
    }

```

