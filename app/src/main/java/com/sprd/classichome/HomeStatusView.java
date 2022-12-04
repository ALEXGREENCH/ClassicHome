package com.sprd.classichome;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.sprd.common.FeatureOption;
import com.sprd.common.util.LogUtils;
import com.sprd.common.util.LunarCalendar;
import com.sprd.common.util.LunarCalendarConvertUtil;
import com.sprd.simple.launcher2.R;

import java.util.Locale;

/**
 * Created by SPRD on 9/25/17.
 */
public class HomeStatusView extends LinearLayout {
    private static final String TAG = "HomeStatusView";

    private Context mContext;

    private TextClock mClockView;
    private TextClock mDateView;
    private TextView mLunarView;

    public HomeStatusView(Context context) {
        super(context);
    }

    public HomeStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext = getContext();
        mClockView = findViewById(R.id.gridhome_clock_view);
        mDateView = findViewById(R.id.gridhome_date_view);
        refreshTime();

        if (FeatureOption.LUNAR_SUPPORT) {
            if(LunarCalendarConvertUtil.isLunarSetting()) {
                mLunarView = findViewById(R.id.gridhome_lunar_date_view);
                mLunarView.setVisibility(View.VISIBLE);
                LunarCalendar.reloadLanguageResources(mContext);
                updateLunarDateView();
            } else {
                LunarCalendar.clearLanguageResourcesRefs();
            }
        }
    }

    public void refreshTime() {
        Patterns.update(mContext);
        mDateView.setFormat24Hour(Patterns.dateView);
        mDateView.setFormat12Hour(Patterns.dateView);

        mClockView.setFormat12Hour(updateAmPmTextSize(
                (int) getResources().getDimension(R.dimen.am_pm_widget_font_size), Patterns.clockView12));
        mClockView.setFormat24Hour(Patterns.clockView24);
    }

    public CharSequence updateAmPmTextSize(int amPmFontSize, String pattern) {
        // Remove the am/pm
        if (amPmFontSize <= 0) {
            pattern.replaceAll("a", "").trim();
        }
        // Replace spaces with "Hair Space"
        pattern = pattern.replaceAll(" ", "\u200A");
        // Build a spannable so that the am/pm will be formatted
        int amPmPos = pattern.indexOf('a');
        if (amPmPos == -1) {
            return pattern;
        }
        Spannable sp = new SpannableString(pattern);
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new AbsoluteSizeSpan(amPmFontSize), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        sp.setSpan(new TypefaceSpan("sans-serif-condensed"), amPmPos, amPmPos + 1,
                Spannable.SPAN_POINT_MARK);
        return sp;
    }

    public void updateLunarDateView() {
        Time time = new Time();
        time.set(System.currentTimeMillis());
        String lunarStr = LunarCalendarConvertUtil.bulidLunarYear(time, mContext)
                + LunarCalendarConvertUtil.buildLunarMonthDay(time, mContext);
        if (LogUtils.DEBUG) {
            LogUtils.d(TAG, "updateLunarDateView: lunarStr = " + lunarStr);
        }
        if(mLunarView != null) {
            mLunarView.setText(lunarStr);
        }
    }

    // DateFormat.getBestDateTimePattern is extremely expensive, and refresh is called often.
    // This is an optimization to ensure we only recompute the patterns when the inputs change.
    private static final class Patterns {
        static String dateView;
        static String clockView12;
        static String clockView24;
        static String cacheKey;

        static void update(Context context) {
            final Locale locale = Locale.getDefault();
            final Resources res = context.getResources();
            final String dateViewSkel = res.getString(R.string.abbrev_wday_month_day_no_year);
            final String clockView12Skel = res.getString(R.string.clock_12hr_format);
            final String clockView24Skel = res.getString(R.string.clock_24hr_format);
            final String key = locale + dateViewSkel + clockView12Skel + clockView24Skel;
            if (key.equals(cacheKey)) return;

            dateView = DateFormat.getBestDateTimePattern(locale, dateViewSkel);

            clockView12 = DateFormat.getBestDateTimePattern(Locale.ENGLISH, clockView12Skel);
            // CLDR insists on adding an AM/PM indicator even though it wasn't in the skeleton
            // format.  The following code removes the AM/PM indicator if we didn't want it.
            if (!clockView12Skel.contains("a")) {
                clockView12 = clockView12.replaceAll("a", "").trim();
            }

            clockView24 = DateFormat.getBestDateTimePattern(locale, clockView24Skel);

            cacheKey = key;
        }
    }
}
