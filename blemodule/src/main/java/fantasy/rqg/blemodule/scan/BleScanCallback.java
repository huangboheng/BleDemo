package fantasy.rqg.blemodule.scan;

/**
 * Created by rqg on 16/11/2016.
 */

public interface BleScanCallback {
    void onScanResult(BleDevice device);
    void onError(Exception e);
}
