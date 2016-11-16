package fantasy.rqg.blemodule.x;

import android.text.TextUtils;
import android.util.Log;


import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import fantasy.rqg.blemodule.BleConnectionException;
import fantasy.rqg.blemodule.BleExecuteException;
import fantasy.rqg.blemodule.BuildConfig;
import fantasy.rqg.blemodule.Debug;
import fantasy.rqg.blemodule.ParserUtils;
import fantasy.rqg.blemodule.x.request.XPerReadRequest;
import fantasy.rqg.blemodule.x.request.XReadRequest;
import fantasy.rqg.blemodule.x.request.XRequest;


/**
 * @author rqg
 * @date 1/18/16.
 */
public class XWorkerThread extends Thread {
    private static final String TAG = "XWorkerThread";

    private static final long TIMEOUT_MILLS = TimeUnit.SECONDS.toMillis(6);

    private final BlockingQueue<XRequest> mRequestQueue = new ArrayBlockingQueue<XRequest>(40);

    protected BlockingQueue<byte[]> mFrameBuffer = new ArrayBlockingQueue<>(20);

    private boolean isRun = true;

    private static final byte[] mSuccessBytes = new byte[]{0x73, 0x75, 0x63, 0x63, 0x65, 0x73, 0x73};
    private static final byte[] mEndBytes = new byte[]{0x65, 0x6E, 0x64};
    private static final byte[] mSyncBytes = new byte[]{0x20, 0x00, 0x00, 0x00, 0x13};

    private boolean isLastCommandSuccess = false;

    private boolean isWriting = false;

    private XBleController mController;

    private XRequest mRequest;


    private boolean onErrorNotified = false;


    public XWorkerThread(XBleController mController) {
        super("ble_worker");
        this.mController = mController;
    }

    @Override
    public void run() {
        while (isRun) {
            try {
                mRequest = null;


                mRequest = mRequestQueue.take();
                mController.checkStateLock();


                //check is run
                if (mRequest.isCanceled()) {
                    continue;
                }

                //write command
                mFrameBuffer.clear();
                byte[][] command = mRequest.getCommand();
                boolean failure = false;
                for (int i = 0; i < command.length && isRun; i++) {
                    if (!writeFrameAndConfirmSuccess(command[i])) {
                        failure = true;
                        break;
                    }
                }
                //check is run
                if (!isRun) {
                    return;
                }

                if (failure) {
                    XBleController controller = mController;

                    if (controller != null)
                        controller.disconnect();

                    throw new BleConnectionException("ble write failure assume ble connection break");
                }
                //check write command success and mRequest not canceled
                if (mRequest.isCanceled()) {
                    continue;
                } else {
                    mRequest.deliverCommandSuccess();
                }

                if (mRequest instanceof XReadRequest) {
                    receiveResponse((XReadRequest) mRequest);
                } else if (mRequest instanceof XPerReadRequest) {
                    receivePerResponse((XPerReadRequest) mRequest);
                }

                mRequest = null;

            } catch (Exception e) {
                Log.e(TAG, "xwork thread run ", e);

                if (mRequest != null)
                    mRequest.deliverError(e);

                if (!isRun) {
                    return;
                }

                if (onErrorNotified) {
                    BleConnectionException bce = new BleConnectionException("ble connection break");

                    while ((mRequest = mRequestQueue.poll()) != null) {
                        mRequest.deliverError(bce);
                    }

                    onErrorNotified = false;
                }

            }
        }

    }

    private void receiveResponse(XReadRequest request) throws InterruptedException, BleExecuteException {
        while (isRun && !request.isCanceled()) {
            byte[] frame = mFrameBuffer.poll(TIMEOUT_MILLS, TimeUnit.MILLISECONDS);

            if (frame == null) {
                throw new BleExecuteException("receive response timeout");
            }

            if (isSuccessFrame(frame) || isEndFrame(frame)) {
                request.deliverAllResponse();
                return;
            } else {
                request.deliverPerFrame(frame);
            }

        }
    }

    private void receivePerResponse(XPerReadRequest request) throws InterruptedException, BleExecuteException {

        if (isRun && !request.isCanceled()) {
            byte[] frame = mFrameBuffer.poll(TIMEOUT_MILLS, TimeUnit.MILLISECONDS);

            if (frame == null) {
                throw new BleExecuteException("receive response timeout");
            } else {
                request.deliverFrame(frame);

            }
        }
    }

    private boolean isSuccessFrame(byte[] frame) {
        if (frame == null)
            return false;

        if (frame.length != mSuccessBytes.length)
            return false;

        return Arrays.equals(frame, mSuccessBytes);
    }

    private boolean isEndFrame(byte[] frame) {
        if (frame == null)
            return false;

        if (frame.length != mEndBytes.length)
            return false;

        return Arrays.equals(frame, mEndBytes);
    }


    /**
     * 写入一帧数据，并等待写入成功确认
     *
     * @param frame 待写入数据
     * @return 写入成功返回true 否则返回false
     */
    private boolean writeFrameAndConfirmSuccess(byte[] frame) {
        isWriting = true;

        try {
            if (mController.writeFrame(frame)) {
                sleep(TIMEOUT_MILLS);
            }
        } catch (InterruptedException e) {
            isWriting = false;
            return isLastCommandSuccess;
        }

        isWriting = false;
        return false;
    }

    /**
     * 由蓝牙回调传入写入成功确认，并interrupt 此线程
     *
     * @param isSuccess 写入是否成功
     */
    public void deliverCommandSuccess(boolean isSuccess) {
        if (isWriting && isRun) {
            isLastCommandSuccess = isSuccess;
            interrupt();
        }
    }


    public void receiveFrame(byte[] frame) {
        Log.v(TAG, "receiveFrame " + ParserUtils.parse(frame));
        if (mFrameBuffer.remainingCapacity() == 0) {
            Log.e(TAG, "receiveFrame frame buffer full , clear buffer");
            if (Debug.DEBUG) {

                while (mFrameBuffer.size() != 0) {
                    byte[] f = mFrameBuffer.poll();

                    if (f == null)
                        break;

                    Log.e(TAG, "receiveFrame drop frame  raw = [" + ParserUtils.parse(frame) + "] bufferSize:" + mFrameBuffer.size());
                }
            }

            mFrameBuffer.clear();
        }

        mFrameBuffer.add(frame);
    }

    public boolean addRequest(XRequest request) {
        Log.d(TAG, "addRequest() called with: " + "request = [" + request + "], size = " + mRequestQueue.size());
        try {

            if (request.isErrorHasSync()) {
                // 检测之前有没有同步数据命令
                for (XRequest r : mRequestQueue) {
                    if (isSyncCommand(r.getCommand()[0])) {
                        request.deliverError(new XHasSportSyncException("has sport sync before this mRequest"));
                        return true;
                    }
                }
                //检测当前是否是同步命令数据
                XRequest r = mRequest;
                if (r != null && isSyncCommand(r.getCommand()[0])) {
                    request.deliverError(new XHasSportSyncException("has sport sync before this mRequest"));
                    return true;
                }
            }


            return mRequestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "addRequest ", e);
            return false;
        }
    }


    private boolean isSyncCommand(byte[] command) {
        if (command.length < mSyncBytes.length)
            return false;
        for (int i = 0; i < mSyncBytes.length; i++) {
            if (command[i] != mSyncBytes[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * cancel request
     *
     * @param tag according the tag to cancel the request
     */
    public void cancleRequest(String tag) {
        synchronized (mRequestQueue) {
            if (tag == null) {
                mRequestQueue.clear();
            } else {
                while (mRequestQueue.remove(tag)) ;
            }

            XRequest r = mRequest;
            if (r != null && TextUtils.equals(r.getTag(), tag)) {
                r.discardResult();
            }
        }
    }


    public void cancelAllRequest() {
        synchronized (mRequestQueue) {
            mRequestQueue.clear();
            XRequest r = mRequest;
            if (r != null) {
                r.discardResult();
            }

//            mRequestQueue.add(new XWriteRequest(
//                    BongCoder.encodeStopOutput(),
//                    new XResponse() {
//                        @Override
//                        public void onError(Exception e) {
//
//                        }
//
//                        @Override
//                        public void onCommandSuccess() {
//                            Log.i(TAG, "onCommandSuccess stop all output");
//                        }
//                    }
//            ));

            try {
                mController.writeFrame(encodeStopOutput());
            } catch (InterruptedException e) {
                Log.e(TAG, "cancelAllRequest stop output failure", e);
            }

            Log.i(TAG, "cancelAllRequest cancel all out put request");
        }
    }

    public static byte[] encodeStopOutput() {
        return BongUtil.hexStringToBytes("2100000020");
    }


    public void setOnErrorNotified() {
        this.onErrorNotified = true;
    }

    public void quit() {
        // TODO: 1/18/16 finish this method
        isRun = false;
        interrupt();
    }
}
