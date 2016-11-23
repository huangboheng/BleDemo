package fantasy.rqg.blemodule;

/**
 * @author rqg
 * @date 11/10/15.
 */
public interface BleManager {
    /**
     * 链接指定设备
     *
     * @param address  ble mac
     * @param callback 初始化回调
     */
    void connect(String address, BLEInitCallback callback);


    /**
     * 关闭链接
     */
    void close();

    /**
     * 如果已链接直接调用{@link BLEInitCallback#onSuccess()}
     *
     * @param callback init callback
     */
    void reconnect(BLEInitCallback callback);


    /**
     * 断开链接
     */
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

    /**
     * 关闭并释放所有资源
     */
    void quit();

    /**
     * @return true 以链接，otherwise false
     */
    boolean isConnected();

    /**
     * 读取 rssi
     *
     * @param callback rssi callback
     * @return 请求是否成功
     */
    boolean readRssi(BleRssiCallback callback);


    /**
     * 获取 device 名称
     *
     * @return 名称
     */
    String readDeviceName();
}
