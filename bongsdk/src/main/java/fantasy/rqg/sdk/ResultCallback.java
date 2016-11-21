package fantasy.rqg.sdk;

/**
 * Created by rqg on 17/11/2016.
 * <p>
 * <p>
 * 不保证所有的回调会在主线程执行
 */

public interface ResultCallback {

    /**
     * all step finished
     */
    void finished();


    /**
     * something goes wrong
     *
     * @param t wrong reason
     */
    void onError(Throwable t);
}
