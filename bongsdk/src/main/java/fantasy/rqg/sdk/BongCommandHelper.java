package fantasy.rqg.sdk;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import cn.ginshell.sdk.BongSdk;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XPerReadResponse;
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;
import fantasy.rqg.sdk.command.BatteryCallback;
import fantasy.rqg.sdk.util.Bong3HRNotifyHandler;
import fantasy.rqg.sdk.util.BongCoder;
import fantasy.rqg.sdk.util.INotifyHandler;

/**
 * Created by rqg on 17/11/2016.
 */

public class BongCommandHelper implements INotifyHandler {
    private BleManager mBleManager;
    private INotifyHandler mNotifyHandlerImpl;

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
}
