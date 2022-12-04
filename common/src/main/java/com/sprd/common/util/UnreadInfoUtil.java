package com.sprd.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.util.Log;

/**
 * Created by SPRD on 2016/7/22.
 */
public class UnreadInfoUtil {
    private static final String TAG = "UnreadInfoUtil";
    public static final Uri MMSSMS_CONTENT_URI = Uri.parse("content://mms-sms");
    private static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    public static final Uri CALLS_CONTENT_URI = CallLog.Calls.CONTENT_URI;
    private static final String MISSED_CALLS_SELECTION =
            CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + " = 1";

    public static final int MMSSMS_UNREAD_MESSAGE = 1;
    public static final int MISS_CALL_MESSAGE = 2;

    public static int getUnreadMessageCount(Context context) {
        long startTime = System.currentTimeMillis();

        int unreadSms = 0;
        int unreadMms = 0;

        ContentResolver resolver = context.getContentResolver();

        // get Unread SMS count
        Cursor smsCursor = null;
        try {
            smsCursor = resolver.query(SMS_CONTENT_URI, new String[]{BaseColumns._ID},
                    "type =1 AND read = 0", null, null);
            if (smsCursor != null) {
                unreadSms = smsCursor.getCount();
                Log.i(TAG, "SMS count = " + unreadSms);
            }
        } catch (Exception e) {
            Log.d(TAG, "getUnreadSmsCount Exception: ", e);
        } finally {
            closeCursorSilently(smsCursor);
        }

        // get Unread MMS count
        Cursor mmsCursor = null;
        try {
            mmsCursor = resolver.query(MMS_CONTENT_URI, new String[]{BaseColumns._ID},
                    "msg_box = 1 AND read = 0 AND ( m_type =130 OR m_type = 132 ) AND thread_id > 0",
                    null, null);
            if (mmsCursor != null) {
                unreadMms = mmsCursor.getCount();
                Log.i(TAG, "MMS count = " + unreadMms);
            }
        } catch (Exception e) {
            Log.d(TAG, "getUnreadMmsCount Exception: ", e);
        } finally {
            closeCursorSilently(mmsCursor);
        }

        long endTime = System.currentTimeMillis();
        Log.i(TAG, "get unread message need " + (endTime - startTime)/1000);

        return unreadMms + unreadSms;
    }

    public static int getMissedCallCount(Context context) {
        long startTime = System.currentTimeMillis();

        int missedCalls = 0;

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = null;
        try {
            cursor = resolver.query(CALLS_CONTENT_URI, new String[]{BaseColumns._ID},
                    MISSED_CALLS_SELECTION, null, null);
            if (cursor != null) {
                missedCalls = cursor.getCount();
                Log.i(TAG, "Missed Call count = " + missedCalls);
            }
        } catch (Exception e) {
            Log.d(TAG, "getMissedCallCount Exception: ", e);
        } finally {
            closeCursorSilently(cursor);
        }

        long endTime = System.currentTimeMillis();
        Log.i(TAG, "get miss call need " + (endTime - startTime)/1000);

        return missedCalls;
    }

    private static void closeCursorSilently(Cursor cursor) {
        try {
            if (cursor != null) cursor.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }
}
