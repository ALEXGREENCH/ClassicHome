package com.sprd.common.util;

import android.content.Context;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/**
 * Created by SPRD on 7/28/2016.
 */
public class MissCallContentObserver extends ContentObserver {
    private final static String TAG = "MissCallContentObserver";
    private Context mContext;
    private Handler mHandler;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MissCallContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            asyncGetUnreadInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void asyncGetUnreadInfo() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return UnreadInfoUtil.getMissedCallCount(mContext);
            }

            @Override
            protected void onPostExecute(Integer missCallCount) {
                Log.i(TAG, "MissCall#onChange#Handler missCallCount = " + missCallCount);
                mHandler.obtainMessage(UnreadInfoUtil.MISS_CALL_MESSAGE, missCallCount).sendToTarget();
            }
        }.execute();
    }
}
