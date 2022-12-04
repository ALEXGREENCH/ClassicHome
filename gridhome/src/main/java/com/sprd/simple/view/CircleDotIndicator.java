package com.sprd.simple.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sprd.simple.launcher.gridhome.R;

/**
 * Created by yangyong on 16-11-25.
 */

public class CircleDotIndicator extends LinearLayout {

    private int mCircleDotCount;
    private int mImgResourceId;
    private int mHeight;
    private int mWidth;
    private Context mContext;

    public int getCircleDotCount() {
        return mCircleDotCount;
    }

    public void setCircleDotCount(int circleDotCount) {
        this.mCircleDotCount = circleDotCount;
        addChildView(circleDotCount);
    }

    public CircleDotIndicator(Context context) {
        super(context);
        initView(context, null);
    }

    public CircleDotIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs == null) {
            return;
        }
        setBaseAttributes(context, attrs);
        addChildView(mCircleDotCount);

    }

    private void addChildView(int count) {
        removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(mContext);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(mWidth, mHeight);
            imageView.setLayoutParams(params);
            imageView.setImageResource(mImgResourceId);
            addView(imageView);
        }
    }

    protected void setBaseAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleDotIndicator);
        mWidth = typedArray.getLayoutDimension(R.styleable.CircleDotIndicator_image_width, "image_width");
        mHeight = typedArray.getLayoutDimension(R.styleable.CircleDotIndicator_image_height, "image_height");
        int defValue = 0;
        mImgResourceId = typedArray.getResourceId(R.styleable.CircleDotIndicator_image_src, defValue);
        mCircleDotCount = typedArray.getInt(R.styleable.CircleDotIndicator_circle_dot_count, defValue);
        typedArray.recycle();
    }

    public void setChildEnable(int index) {
        for (int i = 0; i < mCircleDotCount; i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == index) {
                child.setEnabled(true);
            } else {
                child.setEnabled(false);
            }
        }
    }
}
