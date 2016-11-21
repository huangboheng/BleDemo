package fantasy.rqg.sdk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import cn.ginshell.sdk.BongSdk;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XPerReadResponse;
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;
import fantasy.rqg.sdk.command.BatteryCallback;
import fantasy.rqg.sdk.util.BongCoder;

/**
 * Created by rqg on 17/11/2016.
 */

public class BongCommandHelper {
    private BleManager mBleManager;

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



}
