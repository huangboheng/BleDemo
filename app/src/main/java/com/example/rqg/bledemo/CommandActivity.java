package com.example.rqg.bledemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.rqg.common.view.SideView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.ginshell.sdk.BongSdk;
import cn.ginshell.sdk.db.DBCurve;
import cn.ginshell.sdk.db.DBHeart;
import cn.ginshell.sdk.model.BongBlock;
import cn.ginshell.sdk.pm.LogUtil;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.sdk.BongCommandHelper;
import fantasy.rqg.sdk.ResultCallback;
import fantasy.rqg.sdk.ResultCallbackImpl;
import fantasy.rqg.sdk.command.BatteryCallback;
import fantasy.rqg.sdk.util.AlarmSettings;


/**
 * Created by rqg on 17/11/2016.
 */

public class CommandActivity extends AppCompatActivity {
    private static final String TAG = "CommandActivity";


    private BleManager mBleManager;
    private BongCommandHelper mBongCommandHelper;


    CheckBox cbCall;
    CheckBox cbSMS;
    CheckBox cbQQ;
    CheckBox cbwechat;


    SideView mSideviewArrange;
    SideView mSideviewHand;
    SideView mSideviewSwitch;
    SideView mSideviewHighlight;


    Switch mStepSwitch;
    Switch mDistanceSwitch;
    Switch mCaloriaSwitch;
    Switch mHeartRateSwitch;
    Switch mWeatherSwitch;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        mBleManager = MemoryCache.sBleManager;
        mBongCommandHelper = new BongCommandHelper(mBleManager);

        initView();
    }


    private void initView() {
        cbCall = (CheckBox) findViewById(R.id.cb_call);
        cbSMS = (CheckBox) findViewById(R.id.cb_sms);
        cbQQ = (CheckBox) findViewById(R.id.cb_qq);
        cbwechat = (CheckBox) findViewById(R.id.cb_wechat);


        mSideviewArrange = (SideView) findViewById(R.id.sideview_arrange);
        mSideviewHand = (SideView) findViewById(R.id.sideview_hand);
        mSideviewSwitch = (SideView) findViewById(R.id.sideview_switch);
        mSideviewHighlight = (SideView) findViewById(R.id.sideview_highlight);


        cbCall.setOnCheckedChangeListener(mOnCheckedChange);
        cbSMS.setOnCheckedChangeListener(mOnCheckedChange);
        cbQQ.setOnCheckedChangeListener(mOnCheckedChange);
        cbwechat.setOnCheckedChangeListener(mOnCheckedChange);


        mSideviewArrange.setListener(mOnSideViewClick);
        mSideviewHand.setListener(mOnSideViewClick);
        mSideviewSwitch.setListener(mOnSideViewClick);
        mSideviewHighlight.setListener(mOnSideViewClick);


        mStepSwitch = (Switch) findViewById(R.id.step_switch);
        mDistanceSwitch = (Switch) findViewById(R.id.distance_switch);
        mCaloriaSwitch = (Switch) findViewById(R.id.cal_switch);
        mHeartRateSwitch = (Switch) findViewById(R.id.heart_switch);
        mWeatherSwitch = (Switch) findViewById(R.id.show_weather_switch);


        mStepSwitch.setOnCheckedChangeListener(mContentChange);
        mDistanceSwitch.setOnCheckedChangeListener(mContentChange);
        mCaloriaSwitch.setOnCheckedChangeListener(mContentChange);
        mHeartRateSwitch.setOnCheckedChangeListener(mContentChange);
        mWeatherSwitch.setOnCheckedChangeListener(mContentChange);


        //自动心率测量
        ((Switch) findViewById(R.id.sb_smart_heart)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBongCommandHelper.setAutoMeasureHeart(isChecked, new ResultCallbackImpl());
            }
        });


        ((Switch) findViewById(R.id.sb_sit_remind)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBongCommandHelper.setSitReminder(isChecked, new ResultCallbackImpl());

            }
        });


        ((Switch) findViewById(R.id.no_disturb_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBongCommandHelper.setNotDisturb(isChecked, 8, 0, 22, 30, new ResultCallbackImpl());
            }
        });


    }

    CompoundButton.OnCheckedChangeListener mOnCheckedChange = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mBongCommandHelper.setMessageNotifyEnable(cbCall.isChecked(), cbSMS.isChecked(), cbQQ.isChecked(), cbwechat.isChecked(), new ResultCallbackImpl());
        }
    };

    SideView.OnSideViewClick mOnSideViewClick = new SideView.OnSideViewClick() {
        @Override
        public void onSideClick(SideView sideView, boolean b) {
            mBongCommandHelper.setScreenStyle(!mSideviewArrange.isCheckLeft(), !mSideviewHand.isCheckLeft(), !mSideviewSwitch.isCheckLeft(), !mSideviewHighlight.isCheckLeft(), new ResultCallbackImpl());
        }
    };


    CompoundButton.OnCheckedChangeListener mContentChange = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mBongCommandHelper.setScreenContent(mStepSwitch.isChecked(), mDistanceSwitch.isChecked(), mCaloriaSwitch.isChecked(), mWeatherSwitch.isChecked(), mHeartRateSwitch.isChecked(), new ResultCallbackImpl());
        }
    };


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


    public void sendIncoming(View view) {
        mBongCommandHelper.sendAddIncomingCallNotify("Name", "1313131231231", new ResultCallbackImpl());
    }


    public void cancelIncoming(View view) {
        mBongCommandHelper.sendDelIncomingCallNotify("Name", "1313131231231", new ResultCallbackImpl());
    }

    public void sendMissIncoming(View view) {
        mBongCommandHelper.sendAddMissCallNotify("MissedCall", "123123132123", new ResultCallbackImpl());
    }

    public void sendQQMsg(View view) {
        mBongCommandHelper.sendAddAppMsg("QQ", "QQ Message From App Test", 31231211, 12312, new ResultCallbackImpl());
    }

    public void sendWechatMsg(View view) {
        mBongCommandHelper.sendAddAppMsg("微信"/*or WeChat*/, "WeChat Message From App Test", 31231211, 12312, new ResultCallbackImpl());
    }

    public void cancelWechatMsg(View view) {
        mBongCommandHelper.sendDelAppMsg(31231211, 12312, new ResultCallbackImpl());
    }

    public void sendSMS(View view) {
        mBongCommandHelper.sendAddSms("NDY", "SMS for test", 123, new ResultCallbackImpl());
    }


    public void syncTime(View view) {
        mBongCommandHelper.syncBongTime(new ResultCallbackImpl());
    }

    public void setAlarms(View view) {
        List<AlarmSettings> alarmSettingses = new ArrayList<>();

        AlarmSettings as = new AlarmSettings();


//     * AlarmSettings 属性如下
//     * private boolean isOn; 是否开启闹钟
//                * private int index; 闹钟id 1-5
//                * private int remindBefore; 浅睡眠提醒 (分钟)
//                * private Integer time;  闹钟时间 (分钟)
//                * private boolean day1On; 周一到周日 是否开 true - > 开 false -> 关
//                * private boolean day2On;
//                * private boolean day3On;
//                * private boolean day4On;
//                * private boolean day5On;
//                * private boolean day6On;
//                * private boolean day7On;
//                * private int lazyMode;//0：关闭 1:开启 懒人模式 (2s 设置无意义,但这个字段需要)

        as.setIndex(0);
        as.setOn(true);
        as.setRemindBefore(10);
        as.setTime((int) TimeUnit.HOURS.toMinutes(9)); // 早上九点

        as.setDay1On(true);
        as.setDay2On(true);
        as.setDay3On(true);
        as.setDay4On(true);
        as.setDay5On(true);
        as.setDay6On(true);
        as.setDay7On(true);
        as.setLazyMode(0);

        alarmSettingses.add(as);

        mBongCommandHelper.setAlarms(alarmSettingses, new ResultCallbackImpl());
    }

    public void restartBong(View view) {
        mBongCommandHelper.restartBong(new ResultCallbackImpl());
    }

    public void sendWeatherInfo(View view) {
        mBongCommandHelper.sendWeatherInfo(System.currentTimeMillis() / 1000, 1, -12, 13, new ResultCallbackImpl());
    }
}
