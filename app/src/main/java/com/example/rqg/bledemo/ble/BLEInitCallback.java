package com.example.rqg.bledemo.ble;

/**
 * @author rqg
 * @date 11/12/15.
 */
public interface BLEInitCallback {
    void onSuccess();

    boolean onFailure(int error);
}
