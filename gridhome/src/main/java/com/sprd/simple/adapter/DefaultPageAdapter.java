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

import com.sprd.common.util.UnreadCountStyleUtil;
import com.sprd.simple.launcher.Launcher;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.List;

/**
 * Created by SPRD on 2016/7/19.
 */
public class DefaultPageAdapter extends BaseGridAdapter {
    private static final String TAG = "DefaultPageAdapter";
    private static final int MSG_POSITION = 3;
    private Animation mIconAnimation;

    public DefaultPageAdapter(List<AppInfo> arrayList, Context context, GridView gridView) {
        super(arrayList, context, gridView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemHeight = getItemHeight();
        ItemHolder itemHolder = null;
        if (convertView == null) {
            itemHolder = new ItemHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.default_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, itemHeight
            ));
            itemHolder.itemName = convertView.findViewById(R.id.icon_item_name);
            itemHolder.itemImage = convertView.findViewById(R.id.icon_item_image);
            itemHolder.itemBackground = convertView.findViewById(R.id.icon_item_bg);
            itemHolder.itemValue = convertView.findViewById(R.id.unread_info);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
            ViewGroup.LayoutParams param = convertView.getLayoutParams();
            param.width = ViewGroup.LayoutParams.MATCH_PARENT;
            param.height = itemHeight;
            convertView.setLayoutParams(param);
        }


        // set unread msg count
        if (position == MSG_POSITION) {
            int mmsCount = getUnCount();
            Log.d(TAG, "mmsCount = " + mmsCount + "; position: " + position);
            if (mmsCount > 0) {
                UnreadCountStyleUtil.setReadCountStyle(itemHolder.itemValue, mmsCount);
                Log.d(TAG, "unreadInfo_mms = " + itemHolder.itemValue.getText());
            } else {
                itemHolder.itemValue.setVisibility(View.INVISIBLE);
            }
        } else {
            itemHolder.itemValue.setVisibility(View.INVISIBLE);
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
        itemHolder.itemName.setText(mArrayList.get(position).getAppLabel());
        itemHolder.itemImage.setBackgroundResource(mArrayList.get(position).getAppIcon());
        itemHolder.itemBackground.setBackground(getGradientDrawable(position));
        return convertView;
    }

    @Override
    public int getItemHeight() {
        return calculateItemHeigh(mGridView.getCount());
    }
}
