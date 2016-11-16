package fantasy.rqg.blemodule.scan;

import java.util.Arrays;

/**
 * @author rqg
 * @date 3/7/16.
 */
public class BleUtil {


    /**
     * get specified type content bytes
     *
     * @param bytes raw bytes
     * @param type  content type
     * @return specified type content bytes, nulll mean can not get specified type content
     */
    private static byte[] getTypeBytes(byte[] bytes, final byte type) {
        int len;

        byte[] contentBytes = null;

        if (bytes.length < 2)
            return null;

        int offset = 0;
        while (offset + 1 < bytes.length) {
            len = bytes[offset];
            if (bytes[offset + 1] == type) {

                if (offset + len < bytes.length) {
                    contentBytes = Arrays.copyOfRange(bytes, offset + 2, offset + len + 1);
                }

                break;
            } else {
                offset += len + 1;
            }
        }


        return contentBytes;

    }

    public static String decodeName(byte[] bytes) {
        byte[] cbs = getTypeBytes(bytes, (byte) 0x08);

        if (cbs == null) {
            cbs = getTypeBytes(bytes, (byte) 0x09);
        }
        return cbs == null ? null : new String(cbs);
    }

}
