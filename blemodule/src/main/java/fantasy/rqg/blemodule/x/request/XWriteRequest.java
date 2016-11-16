package fantasy.rqg.blemodule.x.request;

/**
 * @author rqg
 * @date 1/18/16.
 * <p>
 * <p>
 * 无需任何手表响应的命令
 */
public class XWriteRequest extends XRequest {

    public XWriteRequest(byte[][] mCommand, XResponse mResponse) {
        super(mCommand, mResponse);
    }

    public XWriteRequest(byte[] mCommand, XResponse mResponse) {
        this(new byte[][]{mCommand}, mResponse);
    }

}
