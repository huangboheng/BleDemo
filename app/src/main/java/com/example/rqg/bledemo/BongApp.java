package com.example.rqg.bledemo;

import android.app.Application;

import cn.ginshell.sdk.BongSdk;
import cn.ginshell.sdk.model.Gender;

/**
 * Created by rqg on 17/11/2016.
 */

public class BongApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BongSdk.enableDebug(true);
        BongSdk.setUser(175, 25f, Gender.MALE);

        BongSdk.initSdk(this);
    }
}
