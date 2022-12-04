package com.sprd.simple.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


/**
 * Created by SPDR on 2017/03/13.
 */

public class SimpleViewPager extends ViewPager{
    private float xPosition;
    private static final int SLIDING_DISTANCE = 5;

    public SimpleViewPager(Context context){
        super (context);
    }

    public SimpleViewPager(Context context, AttributeSet attrs){
        super (context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev){
        final int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                xPosition = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                return Math.abs(ev.getX () - xPosition) > SLIDING_DISTANCE;
            default:
                break;
        }
        return super.onInterceptTouchEvent (ev);
    }
}
