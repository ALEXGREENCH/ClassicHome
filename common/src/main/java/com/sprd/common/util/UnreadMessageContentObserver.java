package com.sprd.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/**
 * Created by SPRD on 7/28/2016.
 */
public class UnreadMessageContentObserver extends ContentObserver {
    private final static String TAG = "UnreadMessageContentObserver";
    @SuppressWarnings("FieldMayBeFinal")
    private Context mContext;
    @SuppressWarnings("FieldMayBeFinal")
    private Handler mHandler;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public UnreadMessageContentObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        try {
            asyncGetUnreadInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void asyncGetUnreadInfo() {
        //noinspection deprecation
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return UnreadInfoUtil.getUnreadMessageCount(mContext);
            }

            @SuppressLint("LongLogTag")
            @Override
            protected void onPostExecute(Integer unReadMessageCount) {
                Log.i(TAG, "MissMessage#onChange#Handler unReadMessageCount = " + unReadMessageCount);
                mHandler.obtainMessage(UnreadInfoUtil.MMSSMS_UNREAD_MESSAGE, unReadMessageCount).sendToTarget();
            }
        }.execute();
    }
}
