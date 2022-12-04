package com.sprd.simple.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.simple.launcher.Launcher;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.util.MemoryInfoUtil;

import java.util.List;

/**
 * Created by SPRD on 2016/7/19.
 */
public class FourthPageAdapter extends BaseGridAdapter {

    private static final String TAG = "FourthPageAdapter";
    private Animation mIconAnimation;
    private TextView mSpeedTextView;

    public FourthPageAdapter(List<AppInfo> arrayList, Context context, GridView gridView) {
        super(arrayList, context, gridView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemHeight = getItemHeight();
        ItemHolder itemHolder = null;
        if (convertView == null) {
            itemHolder = new ItemHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.app_item_appliction, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, itemHeight
            ));
            itemHolder.itemName = (TextView) convertView.findViewById(R.id.app_item_name);
            itemHolder.itemImage = (ImageView) convertView.findViewById(R.id.app_item_icon);
            itemHolder.itemBackground = (ImageView) convertView.findViewById(R.id.app_item_bg);
            itemHolder.itemValue = (TextView) convertView.findViewById(R.id.memory_info);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
            ViewGroup.LayoutParams param = convertView.getLayoutParams();
            param.width = ViewGroup.LayoutParams.MATCH_PARENT;
            param.height = itemHeight;
            convertView.setLayoutParams(param);
        }
        itemHolder.itemName.setText(mArrayList.get(position).getAppLabel());
        itemHolder.itemImage.setBackgroundResource(mArrayList.get(position).getAppIcon());
        itemHolder.itemBackground.setBackground(getGradientDrawable(position));
        // Show available memory
        if (itemHolder.itemName.getText().equals(mContext.getResources().getString(R.string.speed_name))) {
            itemHolder.itemValue.setText(MemoryInfoUtil.getUsedPercentValue(mContext));
            itemHolder.itemValue.setVisibility(View.VISIBLE);
            mSpeedTextView = itemHolder.itemValue;
        }

        // add the animation for icon
        if (mPosition == position) {
            if (Launcher.animAble) {
                mIconAnimation = AnimationUtils.loadAnimation(mContext, R.anim.icon_anim);
                mIconAnimation.setRepeatMode(Animation.REVERSE);
                convertView.setAnimation(mIconAnimation);
                mIconAnimation.startNow();
                Log.d(TAG, "startAnimation");
            }

        } else {
            convertView.clearAnimation();
        }

        return convertView;
    }

    public void updateSpeedView(){
        if(mSpeedTextView != null) {
            mSpeedTextView.setText(MemoryInfoUtil.getUsedPercentValue(mContext));
        }
    }
}
