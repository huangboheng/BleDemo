package com.example.rqg.bledemo;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.rqg.bledemo.databinding.ActivitySelectBinding;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import fantasy.rqg.blemodule.scan.BleDevice;
import fantasy.rqg.blemodule.scan.BleScanCallback;
import fantasy.rqg.blemodule.scan.BleScanner;

/**
 * * Created by rqg on 08/11/2016.
 */

public class SelectActivity extends AppCompatActivity {
    private static final String TAG = "SelectActivity";

    private ActivitySelectBinding mBinding;

    private Set<BleDevice> mBleDeviceHashSet = Collections.synchronizedSet(new HashSet<BleDevice>(10));


    private SelectAdapter mSelectAdapter;


    private BleScanner mBleScanner = new BleScanner();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select);
        initView();

        mBleScanner.startLeScan(this, new BleScanCallback() {
            @Override
            public void onScanResult(BleDevice device) {
                mBleDeviceHashSet.add(device);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: ", e);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleDevice[] bleDevices = new BleDevice[mBleDeviceHashSet.size()];
                bleDevices = mBleDeviceHashSet.toArray(bleDevices);
                Arrays.sort(bleDevices, new Comparator<BleDevice>() {
                    @Override
                    public int compare(BleDevice o1, BleDevice o2) {
                        return -o1.rssi + o2.rssi;
                    }
                });

                mSelectAdapter.setBleDevices(bleDevices);


                mHandler.postDelayed(this, 1000);
            }
        }, 500);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mBleScanner.stopLeScan();
    }

    private void initView() {
        RecyclerView recyclerView = mBinding.recyclerView;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSelectAdapter = new SelectAdapter(new SelectAdapter.OnBleClickListener() {
            @Override
            public void onBleClick(BleDevice device) {
                Intent intent = new Intent();

                intent.putExtra("ble_mac", device.mac);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        recyclerView.setAdapter(mSelectAdapter);
    }
}
