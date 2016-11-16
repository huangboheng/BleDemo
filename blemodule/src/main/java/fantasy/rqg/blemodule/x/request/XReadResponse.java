package fantasy.rqg.blemodule.x.request;

import java.util.List;

/**
 * @author rqg
 * @date 1/18/16.
 */
public interface XReadResponse extends XResponse {
    void onReceive(List<byte[]> rsp);

    void onReceivePerFrame(byte[] perFrame);
}
