package fantasy.rqg.blemodule.x;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import fantasy.rqg.blemodule.BLEInitCallback;
import fantasy.rqg.blemodule.BaseRequest;
import fantasy.rqg.blemodule.BleExecuteException;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.BleRssiCallback;
import fantasy.rqg.blemodule.x.request.XRequest;


/**
 * @author rqg
 * @date 1/19/16.
 */
public class XBleManager implements BleManager {

    private static final String TAG = "XBleManager";

    private XBleController mController;
    private XWorkerThread mWorkerThread;


    public XBleManager(Application app) {
        XBleController bleController = new XBleController(app);
        XWorkerThread workerThread = new XWorkerThread(bleController);
        bleController.attachWorkerThread(workerThread);
        workerThread.start();

        mController = bleController;
        mWorkerThread = workerThread;
    }

    @Override
    public void connect(String address, BLEInitCallback callback) {
        mController.connectTo(address, callback);
    }

    @Override
    public void close() {
        mController.disconnect();
    }

    @Override
    public void reconnect(BLEInitCallback callback) {
        mController.reconnect(callback);
    }

    @Override
    public void disconnect() {
        mController.disconnect();
    }


    private void addRequestWhileConnected(XRequest request) {
        if (!mWorkerThread.addRequest(request)) {
            request.deliverError(new BleExecuteException("add mRequest failure"));
        }
    }

    @Override
    public void addRequest(BaseRequest request, String tag) {

        try {
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mAdapter == null || !mAdapter.isEnabled()) {
//                ToastUtil.showToast(R.string.ble_enable);
                request.deliverError(new BleExecuteException("not open bluetooth"));
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "addRequest: ", e);
        }

        request.setTag(tag);

        if (request instanceof XRequest) {
            addRequestWhileConnected((XRequest) request);

        } else {
            request.deliverError(new BleExecuteException("mRequest type error"));
        }


    }


    @Override
    public void addRequest(BaseRequest request) {
        addRequest(request, null);
    }

    @Override
    public void cancel(String tag) {
        // TODO: 1/26/16 not finished
    }

    @Override
    public void cancelAll() {
        mWorkerThread.cancelAllRequest();
    }

    @Override
    public void quit() {
        if (mController != null)
            mController.quit();
//
//        mController = null;
//        mWorkerThread = null;
    }

    @Override
    public boolean isConnected() {
        return mController.isConnected();
    }


    @Override
    public boolean readRssi(BleRssiCallback callback) {
        if (isConnected()) {
            return mController.readRssi(callback);
        } else {
            return false;
        }
    }

    @Override
    public String readDeviceName() {
        if (isConnected()) {
            return mController.getDeviceName();
        }
        return null;
    }
}
