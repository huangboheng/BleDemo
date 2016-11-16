package com.example.rqg.bledemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.example.rqg.bledemo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fantasy.rqg.blemodule.BaseBleController;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.BongUtil;
import fantasy.rqg.blemodule.x.XBleManager;
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XPerReadResponse;
import fantasy.rqg.blemodule.x.request.XReadRequest;
import fantasy.rqg.blemodule.x.request.XReadResponse;
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private ActivityMainBinding mBinding;

    private BleManager mBleManger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setActivity(this);


        mBinding.selectBle.setEnabled(checkAndRequestPermissions());

        LocalBroadcastManager.getInstance(this).registerReceiver(mBleStateReceiver, new IntentFilter(BaseBleController.BLE_STATE_CHANGE));
    }


    private BroadcastReceiver mBleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            mBinding.bleStateInfo.setText("State: " + state);

            if (TextUtils.equals(state, "CONNECTED")) {
                mBinding.vibrate.setEnabled(true);
                mBinding.readBattery.setEnabled(true);
                mBinding.readSport.setEnabled(true);
            } else {
                mBinding.vibrate.setEnabled(false);
                mBinding.readBattery.setEnabled(false);
                mBinding.readSport.setEnabled(false);
            }
        }
    };


    public void onClickSelect() {

        if (mBleManger != null) {
            mBleManger.disconnect();
            mBleManger.quit();
            mBleManger = null;
        }

        Intent intent = new Intent(this, SelectActivity.class);

        startActivityForResult(intent, 10);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBleManger != null) {
            mBleManger.disconnect();
            mBleManger.quit();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBleStateReceiver);

    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int bluetooth = checkSelfPermission(Manifest.permission.BLUETOOTH);
            int bluetoothAdmin = checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN);
            int location = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            List<String> listPermissionsNeeded = new ArrayList<>();
            if (bluetooth != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
            }
            if (bluetoothAdmin != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
            }

            if (location != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }


            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
                return false;
            }

            return true;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allOk = true;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) {
                allOk = false;
                break;
            }
        }

        mBinding.selectBle.setEnabled(allOk);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK) {
            String ble_mac = data.getStringExtra("ble_mac");
            mBinding.bleMac.setText("Mac: " + ble_mac);
            mBleManger = new XBleManager(getApplication());
            mBleManger.connect(ble_mac, null);
        }
    }

    public void onClickVibrate() {
        //only write once


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

    public static String encodeVibrateString(int times) {
        if (times < 0) {
            times = 1;
        }
        if (times > 20) {
            times = 20;
        }
        String l = "2600000020";
        if (times <= 15) {
            l += "0";
        }
        l += Integer.toHexString(times);
        return l;
    }


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


    private int count = 0;

    public void onClickReadSport() {

        //write once and read multi times until receive "end" or "success"

        long endtime = System.currentTimeMillis();

        long beginTime = endtime - TimeUnit.HOURS.toMillis(3);

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


    private void setSportCount(int c) {
        mBinding.sportDataCount.setText("Count: " + c);

    }

    public static String getStrTimeForHex(long time) {
        TimeZone CHINA_TIMEZONE = TimeZone.getTimeZone("GMT+08:00");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.setTimeZone(CHINA_TIMEZONE);
        int year = calendar.get(Calendar.YEAR) % 100;//只取2位年份
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return (year > 0xf ? "" : "0") + Integer.toHexString(year) + //只取2位年份
                (month > 0xf ? "" : "0") + Integer.toHexString(month) +
                (day > 0xf ? "" : "0") + Integer.toHexString(day) +
                (hour > 0xf ? "" : "0") + Integer.toHexString(hour) +
                (minute > 0xf ? "" : "0") + Integer.toHexString(minute);
    }

}
