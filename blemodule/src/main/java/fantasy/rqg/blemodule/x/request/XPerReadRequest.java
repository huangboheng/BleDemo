package fantasy.rqg.blemodule.x.request;

/**
 * @author rqg
 * @date 1/20/16.
 * <p/>
 * <p/>
 * 接受数据只有一条，没有结束符
 */
public class XPerReadRequest extends XRequest {
    public XPerReadRequest(byte[][] mCommand, XPerReadResponse mResponse) {
        super(mCommand, mResponse);
    }

    public XPerReadRequest(byte[] mCommand, XPerReadResponse mResponse) {
        this(new byte[][]{mCommand}, mResponse);
    }

    public void deliverFrame(final byte[] frame) {
        final XPerReadResponse r = (XPerReadResponse) mResponse;

        if (r == null)
            return;

        mResultList.add(frame);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                r.onReceive(frame);

            }
        });
    }
}
