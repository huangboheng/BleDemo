package fantasy.rqg.blemodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rqg on 16/11/2016.
 */

public class BlePermission {
    /***
     * check
     * @return return true if permission granted, otherwise return false
     */
    public static boolean checkBlePermission(Context context) {
        int bluetooth = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
        int bluetoothAdmin = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN);
        int location = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (bluetooth != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }
        if (bluetoothAdmin != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (location != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            return false;
        }

        return true;

    }
}
