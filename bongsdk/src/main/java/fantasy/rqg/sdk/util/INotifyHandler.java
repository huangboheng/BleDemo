package fantasy.rqg.sdk.util;

import fantasy.rqg.sdk.ResultCallback;

/**
 * Created by rqg on 21/11/2016.
 */

public interface INotifyHandler {
    /**
     * 来电提醒
     *
     * @param name     来电人名
     * @param number   phone number
     * @param callback callback
     */
    void sendAddIncomingCallNotify(String name, String number, ResultCallback callback);

    /**
     * 来电提醒未接收
     *
     * @param name     名字
     * @param number   电话号码
     * @param callback callback
     */
    void sendAddMissCallNotify(String name, String number, ResultCallback callback);

    /**
     * 取消来电提醒
     *
     * @param name     名字
     * @param number   电话号码
     * @param callback callback
     */
    void sendDelIncomingCallNotify(String name, String number, ResultCallback callback);


    /**
     * 发送 app 消息提醒
     *
     * @param appName  app名字
     * @param msg      消息内容
     * @param msgId    不可重复，不然代表同一条消息
     * @param appId    appId, 建议用 包名 md5 计算出 32 位值
     * @param callback callback
     */
    void sendAddAppMsg(String appName, String msg, int msgId, int appId, ResultCallback callback);

    /**
     * 取消 app 消息提醒 （固件未实现）
     *
     * @param msgId    不可重复，不然代表同一条消息
     * @param appId    appId, 建议用 包名 md5 计算出32 位值
     * @param callback callback
     */
    void sendDelAppMsg(int msgId, int appId, ResultCallback callback);

    /**
     * 发送  sms 消息
     *
     * @param name     发消息 人名
     * @param msg      消息内容
     * @param msgId    消息id
     * @param callback callback
     */
    void sendAddSms(String name, String msg, int msgId, ResultCallback callback);
}
