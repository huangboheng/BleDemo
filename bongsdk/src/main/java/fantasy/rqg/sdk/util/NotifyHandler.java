package fantasy.rqg.sdk.util;

import fantasy.rqg.blemodule.BleManager;

/**
 * *Created by rqg on 6/17/16.
 */
public abstract class NotifyHandler implements INotifyHandler {

    protected BleManager mBleManager;

    public NotifyHandler(BleManager bleManager) {
        mBleManager = bleManager;
    }
}
