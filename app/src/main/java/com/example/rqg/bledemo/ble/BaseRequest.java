package com.example.rqg.bledemo.ble;

import android.text.TextUtils;
import android.util.Log;

/**
 * @author rqg
 * @date 11/10/15.
 */
public abstract class BaseRequest {
    private static final String TAG = "BaseRequest";

    protected String mTag = null;


    public void setTag(String tag) {
        Log.d(TAG, "setTag() called with: " + "tag = [" + tag + "]");
        mTag = tag;
    }

    public abstract void discardResult();

    public abstract boolean isCanceled();

    public boolean sameTag(CharSequence tag) {
        return tag == null || TextUtils.equals(tag, mTag);
    }

    public String getTag() {
        return mTag;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CharSequence) {
            return sameTag((CharSequence) o);
        }
        return super.equals(o);
    }


    public abstract void deliverError(Exception e);
}
