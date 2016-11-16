package fantasy.rqg.blemodule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * *Created by rqg on 5/26/16.
 */
public abstract class BaseBleController {
    private static final String TAG = "BaseBleController";

    public final static int SCAN_PERIOD = 10000;
    public final static int CONNECT_TIMEOUT = 20000;

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String BLE_STATE_CHANGE = "cn.ginshell.bong.ble_state_change";


    protected BluetoothGattCharacteristic mWrite;
    protected BluetoothGatt mBluetoothGatt;
    private String mAddress;
    private BluetoothAdapter mAdapter = null;
    private final List<BLEInitCallback> mInitCallbackList = new ArrayList<>();
    private Handler mHandler;
    private volatile STATE mState;
    private boolean mAutoConnect = false;


    private Context mAppContext;
    private final AtomicBoolean mFoundDevice = new AtomicBoolean(false);

    private BluetoothDevice mDevice;


    private final Semaphore mSemaphore = new Semaphore(1);
    private final AtomicBoolean mErrorCommitted = new AtomicBoolean(true);

    private static long mTimeStamp = 0;

    private BleRssiCallback mRssiCallback;

    private AtomicBoolean isConnecting = new AtomicBoolean(false);


    private LocalBroadcastManager mLocalBroadcastManager;

    public BaseBleController(Context appContext) {
        mAppContext = appContext;
        mHandler = new Handler(Looper.getMainLooper());
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(appContext);


        mErrorCommitted.set(true);
        getStateLock();
    }


    private Runnable mConnectTimeout = new Runnable() {
        @Override
        public void run() {
            disconnect();
            onConnectionError(ERROR_TIMEOUT);
        }
    };


    private Runnable mScanTimeOut = new Runnable() {
        @Override
        public void run() {
            mAdapter.stopLeScan(mScanCallback);
            connectAfterScanTimeout();
        }
    };

    private synchronized void connectToDevice() {
        Log.d(TAG, "connectToDevice() called with: " + "");
        mSemaphore.tryAcquire();

        if (isConnecting.getAndSet(true)) {
            Log.d(TAG, "connectToDevice: is connecting");
            return;
        }

        if (mState == STATE.CONNECTING || mState == STATE.CONNECTED) {
            Log.d(TAG, "already in Initialising");
            return;
        }
//        mState = STATE.CONNECTING;
        setBleState(STATE.CONNECTING);

        Log.d(TAG, "connectToDevice " + mState);
        mTimeStamp = System.currentTimeMillis();

        mHandler.postDelayed(mConnectTimeout, CONNECT_TIMEOUT);


        if (mBluetoothGatt != null)
            close();

        int mErrorCode = 0;


        Log.d(TAG, "connect to " + mAddress);


        if (TextUtils.isEmpty(mAddress)) {
            mErrorCode = ERROR_ADDRESS_IS_EMPTY;
            onConnectionError(mErrorCode);
            return;
        }


        BluetoothManager bm = (BluetoothManager) mAppContext.getSystemService(Context.BLUETOOTH_SERVICE);


        BluetoothDevice device = getDeviceConnected(bm);
        if (device != null) {
            initGatt(device);

            Log.i(TAG, "connectToDevice: device is connected before connected");
            return;
        } else {
            Log.i(TAG, "connectToDevice: device is not connected , start scan to discover device");
        }

        if (mAdapter == null) {
            mAdapter = bm.getAdapter();
        }

        if (mAdapter == null) {
            mErrorCode = ERROR_GET_BLUETOOTH_ADAPTER_FAILURE;
        } else if (!mAdapter.isEnabled()) {
            mErrorCode = ERROR_BLUETOOTH_NOT_OPEN;
        }
        if (mErrorCode != 0) {
            onConnectionError(mErrorCode);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFoundDevice.set(false);
                    mAdapter.startLeScan(mScanCallback);
                }
            });
            mHandler.postDelayed(mScanTimeOut, SCAN_PERIOD);
        }
    }

    /**
     * do not touch this instance after quit
     */
    public void quit() {

        disconnect();

        onConnectionError(ERROR_BLUETOOTH_CONNECTION_BREAK);

        close();

    }

    private BluetoothDevice getDeviceConnected(BluetoothManager bluetoothManager) {
        List<BluetoothDevice> connectedDevices;
        try {
            connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        } catch (Exception e) {
            Log.e(TAG, "getDeviceConnected: ", e);
            return null;
        }
        for (BluetoothDevice device : connectedDevices) {
            if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                Log.i(TAG, "ble " + device.getAddress());
            } else {
                Log.d(TAG, "blue " + device.getAddress());
            }

            if (TextUtils.equals(device.getAddress(), mAddress)) {
                return device;
            }

        }

        return null;
    }


    BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan device:" + device.getAddress());

            if (TextUtils.equals(device.getAddress(), mAddress) && !mFoundDevice.get()) {
                if (!mFoundDevice.getAndSet(true)) {
                    Log.d(TAG, "onLeScan Find my device Stamp:" + (System.currentTimeMillis() - mTimeStamp));
                    mHandler.removeCallbacks(mScanTimeOut);

                    mAdapter.stopLeScan(mScanCallback);

                    initGatt(device);
                }
            }
        }
    };


    public synchronized void connectTo(String address, BLEInitCallback mInitCallback) {
        mTimeStamp = System.currentTimeMillis();

        if (mInitCallback != null)
            mInitCallbackList.add(mInitCallback);

        if (TextUtils.equals(mAddress, address)) {
            if (mState == STATE.CONNECTED) {
                onInitCallbackSuccess();
            } else if (mState == STATE.CONNECTING) {

            } else {
                connectToDevice();
            }
        } else {
            if (mState == STATE.CONNECTED || mState == STATE.CONNECTING) {
                disconnect();
            }
            mAddress = address;
            connectToDevice();
        }


    }


    public synchronized boolean reconnect(BLEInitCallback initCallback) {
        Log.d(TAG, "reconnect() called with: " + "initCallback = [" + initCallback + "] state = [" + mState + "]");
        if (initCallback != null) {
            if (mState == STATE.CONNECTED) {
                initCallback.onSuccess();
            } else {
                Log.d(TAG, "reconnect add init callback");
                mInitCallbackList.add(initCallback);
            }
        }


        connectToDevice();
        return true;

    }

    private void initGatt(BluetoothDevice device) {

        mDevice = device;

        if (mDevice.getBondState() != BluetoothDevice.BOND_NONE) {
            removeBond(mDevice);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = mDevice.connectGatt(mAppContext, mAutoConnect, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                } else {
                    mBluetoothGatt = mDevice.connectGatt(mAppContext, mAutoConnect, mGattCallback);
                }
            }
        }, 1600);
    }

    private void connectAfterScanTimeout() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "connectAfterScanTimeout ");
                mDevice = mAdapter.getRemoteDevice(mAddress);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothGatt = mDevice.connectGatt(mAppContext, mAutoConnect, mGattCallback, BluetoothDevice.TRANSPORT_LE);
                } else {
                    mBluetoothGatt = mDevice.connectGatt(mAppContext, mAutoConnect, mGattCallback);
                }
            }
        }, 1000);
    }

    private void onInitCallbackError(final int error) {
        Log.d(TAG, "onInitCallbackError icls:" + mInitCallbackList.size());
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                synchronized (mInitCallbackList) {
                    List<BLEInitCallback> currBackList = new ArrayList<BLEInitCallback>();
                    currBackList.addAll(mInitCallbackList);
                    mInitCallbackList.clear();
                    for (BLEInitCallback callback : currBackList) {
                        callback.onFailure(error);
                    }
                    currBackList.clear();
                }
            }
        });
    }

    private void onConnectionError(final int error) {
        mHandler.removeCallbacks(mConnectTimeout);
//        mState = STATE.CONNECTION_BREAK;
        setBleState(STATE.CONNECTION_BREAK);

        if (!mErrorCommitted.getAndSet(true))
            getStateLock();


        notifyWorkerConnectionError();

        Log.e(TAG, "onConnectionError " + getErrorType(error));

        onInitCallbackError(error);

        isConnecting.set(false);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        BluetoothGatt gatt = mBluetoothGatt;

        Log.d(TAG, "close close device");
        if (gatt != null)
            gatt.close();

        mBluetoothGatt = null;
        mWrite = null;

    }


    private void getStateLock() {
        try {
            mSemaphore.acquire();
        } catch (InterruptedException e) {
            Log.w(TAG, "getStateLock: ", e);
        }
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     * <p/>
     * 必须在 onConnectionError 之前调用,不然会消除其中的投递
     */
    public void disconnect() {
        mHandler.removeCallbacksAndMessages(null);
        if (mAdapter != null) {
            mAdapter.stopLeScan(mScanCallback);
        }

//        mState = STATE.DISCONNECTING;
        setBleState(STATE.DISCONNECTING);

        isConnecting.set(false);


        BluetoothGatt gatt = mBluetoothGatt;
        if (mAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.e(TAG, "disconnect bluetooth gatt");
        gatt.disconnect();
    }


    protected abstract UUID getNotifyCharacteristicUUID();

    protected abstract UUID getWriteCharacteristicUUID();

    protected abstract UUID getServiceUUID();


    public final static int ERROR_FOUND_DFU_DEVICE = -6;
    public final static int ERROR_BLUETOOTH_INIT_FAILURE = -5;
    public final static int ERROR_BLUETOOTH_DEVICE_PROPERTY_MISS = -4;
    public final static int ERROR_ADDRESS_IS_EMPTY = -3;
    public final static int ERROR_BLUETOOTH_CONNECTION_BREAK = -2;
    public final static int ERROR_EXECUTE_FAILURE = -1;
    public final static int ERROR_GET_BLUETOOTH_ADAPTER_FAILURE = 1;
    public final static int ERROR_ADDRESS_ILLEGAL = 2;
    public final static int ERROR_GET_BLUETOOTH_GATT_FAILURE = 3;
    public final static int ERROR_TIMEOUT = 4;
    public final static int ERROR_COMMAND_INVALID = 5;
    public final static int ERROR_BLUETOOTH_NOT_OPEN = 6;
    public final static int ERROR_BLUETOOTH_SERVICE_NOT_FOUND = 7;
    public final static int ERROR_BLUETOOTH_SERVICE_UNKNOW = 8;


    public static String getErrorType(int type) {
        switch (type) {
            case ERROR_COMMAND_INVALID:
                return "ERROR_COMMAND_INVALID";
            case ERROR_BLUETOOTH_CONNECTION_BREAK:
                return "蓝牙连接已断开";
            case ERROR_EXECUTE_FAILURE:
                return "蓝牙操作执行失败";
            case ERROR_ADDRESS_IS_EMPTY:
                return "设备地址为空";
            case ERROR_GET_BLUETOOTH_ADAPTER_FAILURE:
                return "获取蓝牙适配器失败";
            case ERROR_ADDRESS_ILLEGAL:
                return "蓝牙设备地址不合法";
            case ERROR_GET_BLUETOOTH_GATT_FAILURE:
                return "获取蓝牙描述文件失败";
            case ERROR_TIMEOUT:
                return "蓝牙操作超时，请重试";
            case ERROR_BLUETOOTH_NOT_OPEN:
                return "手机蓝牙未打开";
            case ERROR_BLUETOOTH_SERVICE_NOT_FOUND:
                return "蓝牙服务未发现";
            case ERROR_BLUETOOTH_DEVICE_PROPERTY_MISS:
                return "蓝牙设备属性缺失";
            case ERROR_BLUETOOTH_SERVICE_UNKNOW:
                return "未知错误";
            case ERROR_BLUETOOTH_INIT_FAILURE:
                return "ERROR_BLUETOOTH_INIT_FAILURE";
            case ERROR_FOUND_DFU_DEVICE:
                return "发现DFU设备";
            default:
                return "UNKNOWN " + type;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange() called with: status = [" + status + "], newState = [" + newState + "]");
            if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {

                if (mState != STATE.CONNECTING) {
                    return;
                }

                Log.d(TAG, "connected timeStamp:" + (System.currentTimeMillis() - mTimeStamp));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Some proximity tags (e.g. nRF PROXIMITY) initialize bonding automatically when connected.
//                        if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "gatt.discoverServices()");
                        gatt.discoverServices();
//                        }
                    }
                }, 600);
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                Log.e(TAG, "onConnectionStateChange() called with: status = [" + status + "(" + GattError.parseConnectionError(status) + ")], newState = [" + newState + "]");

//                mState = STATE.CONNECTION_BREAK;
                setBleState(STATE.CONNECTION_BREAK);

                if (gatt == null) {
                    Log.e(TAG, "close gatt null");
                    return;
                }

//                Log.d(TAG, "close refresh device");
//                refreshDevice(gatt);

                onConnectionError(ERROR_BLUETOOTH_CONNECTION_BREAK);

            } else {
                Log.i(TAG, "onConnectionStateChange() called with: status = [" + status + "(" + GattError.parseConnectionError(status) + ")], newState = [" + newState + "]");
                //其他情况全部close
                onConnectionError(ERROR_BLUETOOTH_SERVICE_UNKNOW);
            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered() called with: status = [" + status + "]");
            BluetoothGattCharacteristic notify = null;

            if (status == BluetoothGatt.GATT_SUCCESS && mState == STATE.CONNECTING) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().compareTo(getServiceUUID()) == 0) {
                        notify = service.getCharacteristic(getNotifyCharacteristicUUID());
                        mWrite = service.getCharacteristic(getWriteCharacteristicUUID());
                        break;
                    }
                }

                if (notify == null || mWrite == null) {
                    //not found the service and characteristic
                    disconnect();
                    onConnectionError(ERROR_BLUETOOTH_DEVICE_PROPERTY_MISS);
                } else {
//                    if ((mWrite.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
//
//                        Log.e(TAG, "write characteristic don't has PROPERTY_WRITE_NO_RESPONSE");
//                        disconnect();
//                        onConnectionError(ERROR_BLUETOOTH_DEVICE_PROPERTY_MISS);
//
//                    } else {
                    if (!enableNotifications(notify)) {
                        onConnectionError(ERROR_BLUETOOTH_INIT_FAILURE);
                    }
//                    }
                }
            }

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead() called with:  status = [" + status + "]");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            handleCharacteristicChanged(gatt, characteristic);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            handleCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            Log.d(TAG, "onDescriptorRead() called with:  status = [" + status + "]");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.d(TAG, "onDescriptorWrite() called with:  status = [" + status + "]");
            if (status == BluetoothGatt.GATT_SUCCESS && mState == STATE.CONNECTING) {
                connectSuccess();
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onReliableWriteCompleted() called with:  status = [" + status + "]");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi() called with:  rssi = [" + rssi + "], status = [" + status + "]");
            BleRssiCallback cb = mRssiCallback;
            if (cb != null)
                cb.onRssi(rssi);
        }


        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "onMtuChanged() called with:  mtu = [" + mtu + "], status = [" + status + "]");
        }
    };


    protected abstract void notifyWorkerConnectionError();

    protected abstract void handleCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

    protected abstract void handleCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

    private void connectSuccess() {
        Log.d(TAG, "connect success Stamp:" + (System.currentTimeMillis() - mTimeStamp));

        mHandler.removeCallbacks(mConnectTimeout);

//        mState = STATE.CONNECTED;
        setBleState(STATE.CONNECTED);

        mSemaphore.release();
        mErrorCommitted.set(false);


        Log.d(TAG, "on connection established");

        onInitCallbackSuccess();

        isConnecting.set(false);
    }

    /**
     * Enables notifications on given characteristic
     *
     * @return true is the mRequest has been sent, false if one of the arguments was <code>null</code> or the characteristic does not have the CCCD.
     */
    protected final boolean enableNotifications(final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

// Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        Log.d(TAG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Log.v(TAG, "Enabling notifications for " + characteristic.getUuid());
            Log.d(TAG, "gatt.writeDescriptor(" + CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID + ", value=0x01-00)");
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private void onInitCallbackSuccess() {
        Log.d(TAG, "onInitCallbackSuccess list size:" + mInitCallbackList.size());
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                synchronized (mInitCallbackList) {
                    for (BLEInitCallback callback : mInitCallbackList) {
                        callback.onSuccess();
                    }
                    mInitCallbackList.clear();
                }
            }
        });
    }

    public static enum STATE {
        CONNECTED,
        CONNECTING,
        CONNECTION_BREAK,
        DISCONNECTING
    }


    public boolean readRssi(BleRssiCallback callback) {
        mRssiCallback = callback;
        BluetoothGatt gatt = mBluetoothGatt;

        return gatt != null && gatt.readRemoteRssi();

    }

    public boolean isConnected() {
        return mState == STATE.CONNECTED;
    }


    /**
     * 检查 当前蓝牙是否处于可用状态,如果不可用则阻塞调用者
     */
    public void checkStateLock() throws InterruptedException {
        Log.d(TAG, "checkStateLock() called with: " + "");

        if (!isConnected()) {
            Log.e(TAG, "checkStateLock: connection break");
            reconnect(null);
        }

        mSemaphore.acquire();
        mSemaphore.release();
    }

    public String getDeviceName() {
        if (mDevice != null) {
            return mDevice.getName();
        }
        return null;
    }

    /**
     * Removes the bond information for the given device.
     *
     * @param device the device to unbound
     * @return <code>true</code> if operation succeeded, <code>false</code> otherwise
     */
    private boolean removeBond(final BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_NONE)
            return true;

        boolean result = false;
        /*
         * There is a removeBond() method in BluetoothDevice class but for now it's hidden. We will call it using reflections.
		 */
        try {
            final Method removeBond = device.getClass().getMethod("removeBond");
            if (removeBond != null) {
                result = (Boolean) removeBond.invoke(device);
            }
            result = true;
        } catch (final Exception e) {
            Log.w(TAG, "An exception occurred while removing bond information", e);
        }
        return result;
    }


    private void setBleState(STATE state) {
        if (mState != state) {
            mState = state;

            Intent intent = new Intent(BLE_STATE_CHANGE);

            switch (state) {
                case CONNECTING:
                    intent.putExtra("state", STATE.CONNECTING.name());
                    break;
                case CONNECTED:
                    intent.putExtra("state", STATE.CONNECTED.name());
                    break;
                case CONNECTION_BREAK:
                    intent.putExtra("state", STATE.CONNECTION_BREAK.name());
                    break;
                case DISCONNECTING:
                    intent.putExtra("state", STATE.DISCONNECTING.name());
                    break;
            }


            mLocalBroadcastManager.sendBroadcast(intent);

        }


    }
}
