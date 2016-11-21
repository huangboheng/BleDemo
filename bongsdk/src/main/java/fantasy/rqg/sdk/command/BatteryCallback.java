package fantasy.rqg.sdk.command;

import fantasy.rqg.sdk.ResultCallback;

/**
 * Created by rqg on 21/11/2016.
 */

public interface BatteryCallback extends ResultCallback {

    /**
     * @param remain max 100 , min 0; 此范围之外都是错误值
     *
     */
    void onReadBatter(int remain);
}
