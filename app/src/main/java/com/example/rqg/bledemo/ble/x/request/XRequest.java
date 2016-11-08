package com.example.rqg.bledemo.ble.x.request;

import android.os.Handler;
import android.os.Looper;

import com.example.rqg.bledemo.ble.BaseRequest;

import java.util.ArrayList;
import java.util.List;


/**
 * @author rqg
 * @date 1/18/16.
 */
public class XRequest extends BaseRequest {

    private byte[][] mCommand;
    private boolean errorHasSync = true;

    protected XResponse mResponse;
    protected List<byte[]> mResultList = new ArrayList<>();

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public XRequest(byte[][] mCommand, XResponse mResponse) {
        this.mCommand = mCommand;
        this.mResponse = mResponse;
    }

    @Override
    public void discardResult() {
        mResponse = null;
    }

    @Override
    public boolean isCanceled() {
        return mResponse == null;
    }

    public byte[][] getCommand() {
        return mCommand;
    }

    @Override
    public void deliverError(final Exception e) {
        final XResponse r = mResponse;

        if (r == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                r.onError(e);
            }
        });
    }

    public void deliverCommandSuccess() {
        final XResponse r = mResponse;

        if (r == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                r.onCommandSuccess();

            }
        });
    }

    /**
     * 是否设置了前面有数据同步则报错退出
     *
     * @return
     */
    public boolean isErrorHasSync() {
        return errorHasSync;
    }

    /**
     * 是否设置了前面有数据同步则报错退出
     * default is true
     *
     * @return
     */
    public void setErrorHasSync(boolean errorHasSync) {
        this.errorHasSync = errorHasSync;
    }
}
