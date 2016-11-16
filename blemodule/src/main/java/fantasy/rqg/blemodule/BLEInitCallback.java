package fantasy.rqg.blemodule;

/**
 * @author rqg
 * @date 11/12/15.
 */
public interface BLEInitCallback {
    void onSuccess();

    boolean onFailure(int error);
}
