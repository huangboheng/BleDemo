package fantasy.rqg.blemodule;

/**
 * @author rqg
 * @date 11/12/15.
 */
public interface BLEInitCallback {
    /**
     * 链接成功
     */
    void onSuccess();

    /**
     * 链接失败
     *
     * @param error error code , {@link GattError#parseConnectionError(int)} for read reason
     * @return not use
     */
    boolean onFailure(int error);
}
