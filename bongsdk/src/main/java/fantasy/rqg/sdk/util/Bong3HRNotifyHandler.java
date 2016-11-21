package fantasy.rqg.sdk.util;


import cn.ginshell.sdk.AppMsgCoder;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.request.XResponse;
import fantasy.rqg.blemodule.x.request.XWriteRequest;
import fantasy.rqg.sdk.ResultCallback;

/**
 * *Created by rqg on 6/17/16.
 */
public class Bong3HRNotifyHandler extends NotifyHandler {
    private int mCallAppId;

    public Bong3HRNotifyHandler(BleManager bleManager) {
        super(bleManager);

        STM32CRC crc = new STM32CRC();
        crc.reset();
        crc.update("phone_call".getBytes());

        mCallAppId = crc.getValue();

    }

    @Override
    public void sendAddIncomingCallNotify(String name, String number, final ResultCallback callback) {
        mBleManager.addRequest(
                new XWriteRequest(
                        AppMsgCoder.encodeNotification(AppMsgCoder.NOTIFY_TYPE_CALL, mCallAppId, AppMsgCoder.NOTIFY_CFG_ADD, name, ""),
                        new XResponse() {
                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }

                            @Override
                            public void onCommandSuccess() {
                                callback.finished();
                            }
                        }
                )
        );
    }

    @Override
    public void sendAddMissCallNotify(String name, String number, ResultCallback callback) {
        //empty
    }

    @Override
    public void sendDelIncomingCallNotify(String name, String number, final ResultCallback callback) {
        mBleManager.addRequest(
                new XWriteRequest(
                        AppMsgCoder.encodeNotification(AppMsgCoder.NOTIFY_TYPE_CALL, mCallAppId, AppMsgCoder.NOTIFY_CFG_DEL, name, ""),
                        new XResponse() {
                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }

                            @Override
                            public void onCommandSuccess() {
                                callback.finished();
                            }
                        }
                )
        );
    }

    @Override
    public void sendAddAppMsg(String appName, String msg, int msgId, int appId, final ResultCallback callback) {
        mBleManager.addRequest(
                new XWriteRequest(
                        AppMsgCoder.encodeNotification(AppMsgCoder.NOTIFY_TYPE_APP, msgId, AppMsgCoder.NOTIFY_CFG_ADD, appName, msg),
                        new XResponse() {
                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }

                            @Override
                            public void onCommandSuccess() {
                                callback.finished();
                            }
                        }
                )
        );
    }

    @Override
    public void sendDelAppMsg(int msgId, int appId, final ResultCallback callback) {
        mBleManager.addRequest(
                new XWriteRequest(
                        AppMsgCoder.encodeNotification(AppMsgCoder.NOTIFY_TYPE_APP, msgId, AppMsgCoder.NOTIFY_CFG_DEL, "", ""),
                        new XResponse() {
                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }

                            @Override
                            public void onCommandSuccess() {
                                callback.finished();
                            }
                        }
                )
        );
    }

    @Override
    public void sendAddSms(String name, String msg, int msgId, final ResultCallback callback) {
        mBleManager.addRequest(
                new XWriteRequest(
                        AppMsgCoder.encodeNotification(AppMsgCoder.NOTIFY_TYPE_SMS, msgId, AppMsgCoder.NOTIFY_CFG_ADD, name, msg),
                        new XResponse() {
                            @Override
                            public void onError(Exception e) {
                                callback.onError(e);
                            }

                            @Override
                            public void onCommandSuccess() {
                                callback.finished();
                            }
                        }
                )
        );
    }
}
