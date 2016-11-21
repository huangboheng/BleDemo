package com.example.rqg.bledemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ginshell.sdk.BongSdk;
import cn.ginshell.sdk.model.BongBlock;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.sdk.BongCommandHelper;
import fantasy.rqg.sdk.ResultCallback;
import fantasy.rqg.sdk.command.BatteryCallback;

/**
 * Created by rqg on 17/11/2016.
 */

public class CommandActivity extends AppCompatActivity {
    private static final String TAG = "CommandActivity";


    private BleManager mBleManager;
    private BongCommandHelper mBongCommandHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        mBleManager = MemoryCache.sBleManager;
        mBongCommandHelper = new BongCommandHelper(mBleManager);
    }

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

                Log.d(TAG, "finished: " + bongBlockByTime.size());

                for (BongBlock bb : bongBlockByTime) {
                    Log.d(TAG, bb.toString());
                }

            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "onError: ", t);
            }
        });
    }


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

    
}
