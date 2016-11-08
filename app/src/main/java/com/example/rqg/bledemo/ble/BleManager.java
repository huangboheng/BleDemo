package com.example.rqg.bledemo.ble;

/**
 * @author rqg
 * @date 11/10/15.
 */
public interface BleManager {
    void connect(String address, BLEInitCallback callback);

    void close();

    void reconnect(BLEInitCallback callback);

    void disconnect();

    /**
     * 添加 请求 并执行
     *
     * @param request 请求
     * @param tag     标签，用于cancel
     */
    void addRequest(BaseRequest request, String tag);


    void addRequest(BaseRequest request);

    /**
     * 取消请求
     *
     * @param tag 在addRequest 中添加的tag ，null 清空所有请求
     */
    void cancel(String tag);

    void cancelAll();

    void quit();

    boolean isConnected();

    boolean readRssi(BleRssiCallback callback);

    String readDeviceName();
}
