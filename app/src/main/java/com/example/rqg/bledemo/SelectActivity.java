package com.example.rqg.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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

/**
 * * Created by rqg on 08/11/2016.
 */

public class SelectActivity extends AppCompatActivity {
    private static final String TAG = "SelectActivity";

    private ActivitySelectBinding mBinding;

    private Set<BleDevice> mBleDeviceHashSet = Collections.synchronizedSet(new HashSet<BleDevice>(10));


    private SelectAdapter mSelectAdapter;


    private ScanCallback mScanCallback;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select);
        initView();

        startLeScan();


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

    private void startLeScan() {

        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = defaultAdapter.getBluetoothLeScanner();

            mScanCallback = new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d(TAG, "onScanResult() called with: callbackType = [" + callbackType + "], result = [" + result + "]");
                    BleDevice d = new BleDevice();
                    BluetoothDevice device = result.getDevice();
                    d.mac = device.getAddress();

                    d.name = device.getName();
                    d.rssi = result.getRssi();

                    mBleDeviceHashSet.add(d);
                }
            };

            bluetoothLeScanner.startScan(mScanCallback);
        } else {

            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.d(TAG, "onLeScan() called with: device = [" + device + "], rssi = [" + rssi + "], scanRecord = [" + scanRecord + "]");


                    BleDevice d = new BleDevice();
                    d.mac = device.getAddress();

                    d.name = device.getName();

                    d.rssi = rssi;

                    mBleDeviceHashSet.add(d);

                }
            };

            defaultAdapter.startLeScan(mLeScanCallback);
        }
    }


    private void stopLeScan() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = defaultAdapter.getBluetoothLeScanner();

            bluetoothLeScanner.stopScan(mScanCallback);

        } else {
            defaultAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLeScan();
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
