package fantasy.rqg.blemodule.scan;

import android.text.TextUtils;

/**
 * * Created by rqg on 08/11/2016.
 */

public class BleDevice {
    public String mac;
    public String name;
    public int rssi;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BleDevice) {
            BleDevice bd = (BleDevice) obj;
            boolean equals = TextUtils.equals(mac, bd.mac);

            if (equals && TextUtils.isEmpty(name) && !TextUtils.isEmpty(bd.name)) {
                name = bd.name;
            }
            return equals;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}
