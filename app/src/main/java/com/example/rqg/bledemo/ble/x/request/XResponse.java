package com.example.rqg.bledemo.ble.x.request;

/**
 * @author rqg
 * @date 1/18/16.
 */
public interface XResponse {

    void onError(Exception e);

    void onCommandSuccess();
}
