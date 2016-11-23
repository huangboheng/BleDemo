package fantasy.rqg.sdk;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ginshell.sdk.BongSdk;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.BongUtil;
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XPerReadResponse;
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;
import fantasy.rqg.sdk.command.BatteryCallback;
import fantasy.rqg.sdk.util.AlarmSettings;
import fantasy.rqg.sdk.util.Bong3HRNotifyHandler;
import fantasy.rqg.sdk.util.BongCoder;
import fantasy.rqg.sdk.util.ContentConfigModel;
import fantasy.rqg.sdk.util.INotifyHandler;

/**
 * Created by rqg on 17/11/2016.
 * <p>
 * <p>
 * 手环命令通信辅助类
 */

public class BongCommandHelper implements INotifyHandler {
    private BleManager mBleManager;
    private INotifyHandler mNotifyHandlerImpl;

    /**
     * @param bleManager {@link BleManager} mBleManger = new XBleManager(getApplication());
     *                   mBleManger.connect(ble_mac, null);
     *                   经过以上初始化才能传入构造函数
     */
    public BongCommandHelper(@NonNull BleManager bleManager) {
        mBleManager = bleManager;
    }


    /**
     * 从上次同步时间到当前开始同步数据
     *
     * @param callback
     */
    public void syncDataFromBong(@NonNull ResultCallback callback) {
        long end = System.currentTimeMillis() / 1000;
        long start = BongSdk.getSyncStartTime(end);

        if (end - start < 60) {
            //没有招到上次结束点
            start = end - TimeUnit.DAYS.toSeconds(3);
        }

        new XSyncHelper(mBleManager, callback)
                .syncSportData(start * 1000, end * 1000);

    }


    // vibrate bong
    public void vibrateBong(final ResultCallback callback) {

        byte[] command = BongCoder.encodeVibrate();

        mBleManager.addRequest(new XWriteRequest(
                command,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }


    public void readBattery(final BatteryCallback callback) {
        mBleManager.addRequest(new XPerReadRequest(
                BongCoder.encodeReadBattery()
                , new XPerReadResponse() {
            @Override
            public void onReceive(byte[] rsp) {
                int bu;
                if (rsp != null && rsp.length > 10) {
                    bu = (rsp[10] & 0x000000ff);

                } else {
                    bu = -1;
                }

                callback.onReadBatter(bu);

                callback.finished();
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }

            @Override
            public void onCommandSuccess() {

            }
        }));

    }


    private INotifyHandler getNotifyHandler() {
        if (mNotifyHandlerImpl == null) {
            synchronized (this) {
                if (mNotifyHandlerImpl == null)
                    mNotifyHandlerImpl = new Bong3HRNotifyHandler(mBleManager);
            }
        }

        return mNotifyHandlerImpl;

    }


    @Override
    public void sendAddIncomingCallNotify(String name, String number, ResultCallback callback) {
        getNotifyHandler().sendAddIncomingCallNotify(name, number, callback);
    }

    @Override
    public void sendAddMissCallNotify(String name, String number, ResultCallback callback) {
        getNotifyHandler().sendAddMissCallNotify(name, number, callback);
    }

    @Override
    public void sendDelIncomingCallNotify(String name, String number, ResultCallback callback) {
        getNotifyHandler().sendDelIncomingCallNotify(name, number, callback);
    }

    @Override
    public void sendAddAppMsg(String appName, String msg, int msgId, int appId, ResultCallback callback) {
        getNotifyHandler().sendAddAppMsg(appName, msg, msgId, appId, callback);
    }

    @Override
    public void sendDelAppMsg(int msgId, int appId, ResultCallback callback) {
        getNotifyHandler().sendDelAppMsg(msgId, appId, callback);
    }

    @Override
    public void sendAddSms(String name, String msg, int msgId, ResultCallback callback) {
        getNotifyHandler().sendAddSms(name, msg, msgId, callback);
    }

    /**
     * 设置消息提醒开关
     *
     * @param call     enable true , otherwise false
     * @param sms      enable true , otherwise false
     * @param qq       enable true , otherwise false
     * @param wechat   enable true , otherwise false
     * @param callback enable true , otherwise false
     */
    public void setMessageNotifyEnable(boolean call, boolean sms, boolean qq, boolean wechat, final ResultCallback callback) {
        byte[] cmd = BongCoder.encodeAppMsgSwitch(call, wechat, qq, sms);

        mBleManager.addRequest(new XPerReadRequest(
                cmd,
                new XPerReadResponse() {
                    @Override
                    public void onReceive(byte[] rsp) {
                        callback.finished();
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {

                    }
                }
        ));
    }

    /**
     * 同步当前时间和时区到 bong 手环
     */
    public void syncBongTime(final ResultCallback callback) {
        byte[] cmd = BongCoder.encodeTimeSync();

        mBleManager.addRequest(new XWriteRequest(
                cmd,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }


    /**
     * 设置屏幕样式， 详情见 使用处
     *
     * @param vertical
     * @param auto
     * @param right
     * @param highlight
     * @param callback
     */
    public void setScreenStyle(boolean vertical, boolean auto, boolean right, boolean highlight, final ResultCallback callback) {
        byte[] command = BongCoder.encodeScreenStyle(vertical, auto, right, highlight);

        mBleManager.addRequest(new XPerReadRequest(
                command,
                new XPerReadResponse() {
                    @Override
                    public void onReceive(byte[] rsp) {
                        callback.finished();
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {

                    }
                }
        ));

    }


    /**
     * 自动心率测量
     *
     * @param open
     * @param callback
     */
    public void setAutoMeasureHeart(boolean open, final ResultCallback callback) {
        byte[] cmd = BongCoder.encodeSmartHeart(open);
        mBleManager.addRequest(new XWriteRequest(
                cmd,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }


    /**
     * 就坐提醒开关
     *
     * @param enable
     * @param callback
     */
    public void setSitReminder(boolean enable, final ResultCallback callback) {
        String code = BongCoder.encode2sSitReminder(enable);

        byte[] cmd = BongUtil.hexStringToBytes(code);


        mBleManager.addRequest(new XWriteRequest(
                cmd,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }


    /**
     * 设置勿扰模式, mode == 0 其他参数无效
     *
     * @param mode     勿扰开关 true:开，false:关
     * @param sH       开始 小时
     * @param sM       开始 分钟
     * @param eH       结束 小时
     * @param eM       结束 分钟
     * @param callback callback
     */
    public void setNotDisturb(boolean mode, int sH, int sM, int eH, int eM, final ResultCallback callback) {
        String code = BongCoder.encodeNoDisturb(mode ? 1 : 0, sH, sM, eH, eM);


        mBleManager.addRequest(new XWriteRequest(
                BongUtil.hexStringToBytes(code),
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }

    /**
     * 重启手环
     *
     * @param callback callback
     */
    public void restartBong(final ResultCallback callback) {
        byte[] cmd = BongUtil.hexStringToBytes(BongCoder.encodeRestartBong());

        mBleManager.addRequest(new XWriteRequest(
                cmd,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }

    /**
     * 设置闹钟
     *
     * @param alarmSettingsList 最多5个
     * @param callback          callback
     */
    public void setAlarms(List<AlarmSettings> alarmSettingsList, final ResultCallback callback) {
        String code = BongCoder.encodeAlaramListString(alarmSettingsList);

        byte[] cmd = BongUtil.hexStringToBytes(code);

        mBleManager.addRequest(new XWriteRequest(
                cmd,
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }


    public void setScreenContent(boolean step, boolean distance, boolean cal, boolean weather, boolean heart, final ResultCallback callback) {
        mBleManager.addRequest(new XPerReadRequest(
                BongCoder.encodeBong3Screen(step, distance, cal, weather, heart),
                new XPerReadResponse() {
                    @Override
                    public void onReceive(byte[] rsp) {
                        callback.finished();
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {

                    }
                }
        ));

    }


    /**
     * 设置天气信息
     * <p>
     * <p>
     * id   |   天气
     * -----|-----------
     * 1	|	晴
     * 2	|	晴
     * 3	|	晴
     * 4	|	晴
     * 5	|	晴
     * 6	|	大部晴朗
     * 7	|	大部晴朗
     * 8	|	多云
     * 9	|	多云
     * 10	|	多云
     * 11	|	多云
     * 12	|	少云
     * 13	|	阴
     * 14	|	阴
     * 15	|	阵雨
     * 16	|	阵雨
     * 17	|	阵雨
     * 18	|	阵雨
     * 19	|	阵雨
     * 20	|	局部阵雨
     * 21	|	局部阵雨
     * 22	|	小阵雨
     * 23	|	强阵雨
     * 24	|	阵雪
     * 25	|	小阵雪
     * 26	|	雾
     * 27	|	雾
     * 28	|	冻雾
     * 29	|	沙尘暴
     * 30	|	浮尘
     * 31	|	尘卷风
     * 32	|	扬沙
     * 33	|	强沙尘暴
     * 34	|	霾
     * 35	|	霾
     * 36	|	阴
     * 37	|	雷阵雨
     * 38	|	雷阵雨
     * 39	|	雷阵雨
     * 40	|	雷阵雨
     * 41	|	雷阵雨
     * 42	|	雷电
     * 43	|	雷暴
     * 44	|	雷阵雨伴有冰雹
     * 45	|	雷阵雨伴有冰雹
     * 46	|	冰雹
     * 47	|	冰针
     * 48	|	冰粒
     * 49	|	雨夹雪
     * 50	|	雨夹雪
     * 51	|	小雨
     * 52	|	小雨
     * 53	|	中雨
     * 54	|	大雨
     * 55	|	暴雨
     * 56	|	大暴雨
     * 57	|	特大暴雨
     * 58	|	小雪
     * 59	|	小雪
     * 60	|	中雪
     * 61	|	中雪
     * 62	|	大雪
     * 63	|	暴雪
     * 64	|	冻雨
     * 65	|	冻雨
     * 66	|	小雨
     * 67	|	中雨
     * 68	|	大雨
     * 69	|	大暴雨
     * 70	|	大暴雨
     * 71	|	小雪
     * 72	|	小雪
     * 73	|	小雪
     * 74	|	大雪
     * 75	|	大雪
     * 76	|	大雪
     * 77	|	雪
     * 78	|	雨
     * 79	|	霾
     * 80	|	多云
     * 82	|	多云
     * 81	|	多云
     * 83	|	雾
     * 84	|	雾
     * 85	|	阴
     * 86	|	阵雨
     * 87	|	雷阵雨
     * 88	|	雷阵雨
     * 89	|	雷阵雨
     * 90	|	雷阵雨
     * 91	|	小到中雨
     * 92	|	中到大雨
     * 93	|	大到暴雨
     * 94	|	小到中雪
     *
     * @param second      时间戳 单位 秒
     * @param weatherCode 天气代码
     * @param temp        温度 摄氏度
     * @param pm25        pm 2.5
     * @param callback    call back
     */
    public void sendWeatherInfo(long second, int weatherCode, int temp, int pm25, final ResultCallback callback) {
        mBleManager.addRequest(new XWriteRequest(
                BongCoder.encodeWeather(second, weatherCode, temp, pm25),
                new XResponse() {
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onCommandSuccess() {
                        callback.finished();
                    }
                }
        ));
    }
}
