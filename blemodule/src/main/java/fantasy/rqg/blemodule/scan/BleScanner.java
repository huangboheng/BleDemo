package fantasy.rqg.blemodule.scan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import java.security.InvalidParameterException;

import fantasy.rqg.blemodule.BlePermission;

/**
 * Created by rqg on 16/11/2016.
 */

public class BleScanner {
    private static final String TAG = "BleScanner";

    private ScanCallback mScanCallback;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private BleScanCallback mCallback;


    /**
     * 线程不安全
     *
     * @param context  context
     * @param callback 搜索callback
     */
    public void startLeScan(@NonNull Context context, @NonNull BleScanCallback callback) {


        if (!BlePermission.checkBlePermission(context)) {
            callback.onError(new RuntimeException("Ble Permission not granted check permissions:\n" +
                    "\t\t\t" + Manifest.permission.BLUETOOTH + "\n" +
                    "\t\t\t" + Manifest.permission.BLUETOOTH_ADMIN + "\n" +
                    "\t\t\t" + Manifest.permission.ACCESS_FINE_LOCATION + "\n"
            ));

            return;
        }


        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!defaultAdapter.isEnabled()) {
            callback.onError(new RuntimeException("bluetooth not open"));
            return;
        }


        mCallback = callback;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = defaultAdapter.getBluetoothLeScanner();

            mScanCallback = new ScanCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BleDevice d = new BleDevice();
                    BluetoothDevice device = result.getDevice();
                    d.mac = device.getAddress();

                    d.name = device.getName();
                    d.rssi = result.getRssi();

                    if (TextUtils.isEmpty(d.name)) {
                        ScanRecord scanRecord = result.getScanRecord();
                        if (scanRecord != null) {
                            d.name = BleUtil.decodeName(scanRecord.getBytes());
                        }
                    }

                    mCallback.onScanResult(d);
                }
            };

            bluetoothLeScanner.startScan(mScanCallback);
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {


                    BleDevice d = new BleDevice();
                    d.mac = device.getAddress();

                    d.name = device.getName();

                    if (TextUtils.isEmpty(d.name) && scanRecord != null) {
                        d.name = BleUtil.decodeName(scanRecord);
                    }

                    d.rssi = rssi;

                    mCallback.onScanResult(d);

                }
            };

            defaultAdapter.startLeScan(mLeScanCallback);
        }
    }


    public void stopLeScan() {


        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = defaultAdapter.getBluetoothLeScanner();

            bluetoothLeScanner.stopScan(mScanCallback);

        } else {
            defaultAdapter.stopLeScan(mLeScanCallback);
        }


    }
}
