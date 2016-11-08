package com.example.rqg.bledemo.ble.x.request;

/**
 * @author rqg
 * @date 1/18/16.
 * <p/>
 * 需要手表响应的命令
 * 接收多条数据，有结束符
 */
public class XReadRequest extends XRequest {

    public XReadRequest(byte[][] mCommand, XReadResponse mResponse) {
        super(mCommand, mResponse);
    }

    public XReadRequest(byte[] mCommand, XReadResponse mResponse) {
        this(new byte[][]{mCommand}, mResponse);
    }

    public void deliverPerFrame(final byte[] frame) {
        final XReadResponse r = (XReadResponse) mResponse;

        if (r == null)
            return;

        mResultList.add(frame);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                r.onReceivePerFrame(frame);

            }
        });
    }

    public void deliverAllResponse() {
        final XReadResponse r = (XReadResponse) mResponse;

        mResponse = null;
        
        if (r == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                r.onReceive(mResultList);
            }
        });
    }
}
