package fantasy.rqg.sdk;


import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;
import java.util.List;

import cn.ginshell.sdk.BongCommandApi;
import cn.ginshell.sdk.BongSdk;
import fantasy.rqg.blemodule.BleManager;
import fantasy.rqg.blemodule.x.request.XReadRequest;
import fantasy.rqg.blemodule.x.request.XReadResponse;
import fantasy.rqg.blemodule.x.request.XRequest;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * @author rqg
 *         <p>
 *         负责同步数据流程
 * @date 1/22/16.
 */
public class XSyncHelper {
    private static final String TAG = "XSyncHelper";


    private BleManager mBleManager;
    private ResultCallback mResultCallback;

    public XSyncHelper(@NonNull BleManager bleManager, @NonNull ResultCallback resultCallback) {
        mResultCallback = resultCallback;
        mBleManager = bleManager;
    }

    public void syncSportData(final long startTime, final long endTime) {
        Log.d(TAG, "syncSportData() called with: startTime = [" + new Date(startTime) + "], endTime = [" + new Date(endTime) + "]");

        byte[] commond = BongCommandApi.syncSportDataByTimeRead(startTime, endTime);

        XReadRequest request = new XReadRequest(
                commond
                , new XReadResponse() {
            @Override
            public void onReceive(List<byte[]> rsp) {
                Log.d(TAG, "onReceive() called with: " + "rsp = [" + rsp.size() + "]");


                if (rsp.size() == 0) {
                    mResultCallback.finished();
                } else {
                    get2sHeartRate(rsp, startTime, endTime);

                }

            }

            @Override
            public void onReceivePerFrame(byte[] perFrame) {

            }

            @Override
            public void onError(Exception e) {
                mResultCallback.onError(e);
            }

            @Override
            public void onCommandSuccess() {

            }
        });


        request.setErrorHasSync(false);

        mBleManager.addRequest(request);
    }


    private void get2sHeartRate(final List<byte[]> sportList, final long mStartTime, final long mEndTime) {

        byte[] command = BongCommandApi.syncHeartDataByTimeRead(mStartTime, mEndTime);

        XRequest request = new XReadRequest(
                command
                , new XReadResponse() {
            @Override
            public void onReceive(List<byte[]> rsp) {
                handle2sRsp(sportList, rsp, mStartTime, mEndTime);
            }

            @Override
            public void onReceivePerFrame(byte[] perFrame) {

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "get2sHeartRate ", e);
                handle2sRsp(sportList, null, mStartTime, mEndTime);
                mResultCallback.onError(e);
            }

            @Override
            public void onCommandSuccess() {

            }
        });

        request.setErrorHasSync(false);

        mBleManager.addRequest(request);
    }

    private void handle2sRsp(final List<byte[]> sportList, final List<byte[]> heartList, final long mStartTime, final long mEndTime) {
        Log.d(TAG, "handle2sRsp() called with: sportList = [" + sportList.size() + "], heartList = [" + heartList.size() + "], mStartTime = [" + mStartTime + "], mEndTime = [" + mEndTime + "]");

        Observable.just(null)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mResultCallback.onError(e);
                    }

                    @Override
                    public void onNext(Object o) {
                        //计算密集，不要再主线程工作
                        BongSdk.putRawData(sportList, heartList, mEndTime, mStartTime);

                        mResultCallback.finished();
                    }
                });
    }


}
