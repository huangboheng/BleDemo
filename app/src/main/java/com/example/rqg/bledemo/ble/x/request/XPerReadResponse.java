package com.example.rqg.bledemo.ble.x.request;

/**
 * @author rqg
 * @date 1/20/16.
 */
public interface XPerReadResponse extends XResponse {
    void onReceive(byte[] rsp);

}
