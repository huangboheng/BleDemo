package com.example.rqg.bledemo.ble.x;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.rqg.bledemo.ble.BaseBleController;
import com.example.rqg.bledemo.ble.ParserUtils;

import java.util.UUID;


/**
 * @author rqg
 * @date 1/18/16.
 */

public class XBleController extends BaseBleController {

    private static final String TAG = "XBleController";

    public final static UUID NOTIFY_CHARACTERISTIC = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca1e");
    public final static UUID WRITE_CHARACTERISTIC = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca1e");
    private final static UUID BONG_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca1e");

    private XWorkerThread mWorkerThread;
    private LocalBroadcastManager mLBM;

    public XBleController(Context appContext) {
        super(appContext);
        mLBM = LocalBroadcastManager.getInstance(appContext);

    }


    @Override
    protected UUID getNotifyCharacteristicUUID() {
        return NOTIFY_CHARACTERISTIC;
    }

    @Override
    protected UUID getWriteCharacteristicUUID() {
        return WRITE_CHARACTERISTIC;
    }

    @Override
    protected UUID getServiceUUID() {
        return BONG_SERVICE;
    }

    @Override
    protected void notifyWorkerConnectionError() {
        Log.i(TAG, "notifyWorkerConnectionError: ");
        if (mWorkerThread != null) {
            mWorkerThread.setOnErrorNotified();
            mWorkerThread.interrupt();
        }
    }


    /**
     * do not touch this instance after quit
     */
    @Override
    public void quit() {
        super.quit();

        XWorkerThread xt = mWorkerThread;
        if (xt != null)
            xt.quit();

        mWorkerThread = null;
    }

    @Override
    protected void handleCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic.getUuid().compareTo(WRITE_CHARACTERISTIC) == 0) {
            XWorkerThread xt = mWorkerThread;

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (xt != null)
                    mWorkerThread.deliverCommandSuccess(true);
            } else {

                if (xt != null)
                    mWorkerThread.deliverCommandSuccess(false);

                if (status == 0x85) {
                    disconnect();
                }
            }
        } else {
            Log.e(TAG, "onCharacteristicWrite unhandled uuid:" + characteristic.getUuid().toString());
        }
    }

    @Override
    protected void handleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();

        if (value != null) {
            // bong x 系列 特殊广播， 无需关注
//            if (value.length >= 2) {
//
//                if (value[0] == 0x02 && value[1] == 0x02 && value.length == 4) {
//                    Intent intent = new Intent(Constant.Intent.BONG_UPDATE_BATTERY);
//                    intent.putExtra(Constant.Key.BONG_2S_BATTERY_REMAIN, (int) value[3]);
//                    intent.putExtra(Constant.Key.BONG_2S_BATTERY_CHARGE, value[2] == 0x01);
//
//                    mLBM.sendBroadcast(intent);
//                } else if (value[0] == 0x02 && value[1] == 0x03) {
//                    Intent intent = new Intent(Constant.Intent.BODY_TEST);
//                    intent.putExtra(Constant.Key.BODY_TEST_DATA, Arrays.copyOfRange(value, 2, value.length));
//
//                    mLBM.sendBroadcast(intent);
//                } else if (value[0] == 0x02 && value[1] == 0x04 && value.length == 3) {
//                    Intent intent = new Intent(Constant.Intent.BODY_TEST);
//                    intent.putExtra(Constant.Key.BODY_TEST_HEART, (int) value[2]);
//
//                    mLBM.sendBroadcast(intent);
//                } else {
//                    XWorkerThread xt = mWorkerThread;
//                    if (xt != null)
//                        xt.receiveFrame(value);
//                }
//
//            } else {

            XWorkerThread xt = mWorkerThread;
            if (xt != null)
                xt.receiveFrame(value);
//            }
        }
    }


    /**
     * set to enable and set null to disable
     *
     * @param thread worker thread
     */
    public void attachWorkerThread(XWorkerThread thread) {
        mWorkerThread = thread;
    }

    public boolean writeFrame(byte[] frame) throws InterruptedException {

        Log.v(TAG, "writeFrame frame =[" + ParserUtils.parse(frame) + "]");

        BluetoothGattCharacteristic write = mWrite;
        BluetoothGatt gatt = mBluetoothGatt;

        if (write == null || gatt == null)
            return false;

        write.setValue(frame);

        int retry = 0;
        while (retry < 3 && !gatt.writeCharacteristic(write)) {
            //写入可能由于设备忙造成写入失败，等待5毫秒
            Log.e(TAG, "writePackage ble busy");

            Thread.sleep(10);

            retry++;
        }

        if (retry == 3) {
            Log.e(TAG, "write failure");
            return false;
        }

        return true;
    }


}
