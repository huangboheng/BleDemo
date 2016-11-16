package fantasy.rqg.blemodule.x.request;

/**
 * @author rqg
 * @date 1/18/16.
 */
public interface XResponse {

    void onError(Exception e);

    void onCommandSuccess();
}
