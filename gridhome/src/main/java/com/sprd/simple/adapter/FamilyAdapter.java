package com.sprd.simple.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.common.util.HomeConstants;
import com.sprd.simple.launcher.Launcher;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.List;

import static android.widget.GridView.AUTO_FIT;

/**
 * Created by SPRD on 2016/7/19.
 */
public class FamilyAdapter extends BaseGridAdapter {
    public static final String TAG = "FamilyAdapter";
    private Animation mIconAnimation;

    public FamilyAdapter(Context context, List<AppInfo> menuList, GridView gridView) {
        super(menuList, context, gridView);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int count = mGridView.getCount();
        int numColumns = mGridView.getNumColumns();
        if (numColumns == AUTO_FIT) {
            numColumns = 1;
        }
        ItemHolder itemHolder;
        float marginSize = mContext.getResources().getDimension(R.dimen.layout_margin_size);
        float horizontalSpacing = mContext.getResources().getDimension(R.dimen.horizontalSpacing_grid_view);
        int gridHeight = mGridView.getHeight();
        int row = count / numColumns;
        int itemHeight = (int) ((gridHeight - horizontalSpacing * (row - 1)) / (row));
        if (convertView == null) {
            itemHolder = new ItemHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.family_item, null);
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, itemHeight
            ));

            itemHolder.itemName = (TextView) convertView.findViewById(R.id.family_item_name);
            itemHolder.itemImage = (ImageView) convertView.findViewById(R.id.family_item_image);
            itemHolder.itemBackground = (ImageView) convertView.findViewById(R.id.family_item_bg);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
            ViewGroup.LayoutParams param = convertView.getLayoutParams();
            param.width = ViewGroup.LayoutParams.MATCH_PARENT;
            param.height = itemHeight;
            convertView.setLayoutParams(param);
        }
        SharedPreferences sp = mContext.getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        if (sp.contains(position + "")) {
            itemHolder.itemImage.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            itemHolder.itemName.setLayoutParams(layoutParams);
            itemHolder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.family_item_textsize));
        } else {
            itemHolder.itemImage.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            itemHolder.itemName.setLayoutParams(layoutParams);
            itemHolder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.family_item_textsize_empty));
        }

        itemHolder.itemBackground.setBackground(getGradientDrawable(position));
        itemHolder.itemName.setText(mArrayList.get(position).getAppLabel());
        Log.d(TAG, "Name = " + mArrayList.get(position).getAppLabel() + " position = " + position);

        // add the animation for icon
        if ((mPosition == position) && (Launcher.animAble)) {
            mIconAnimation = AnimationUtils.loadAnimation(mContext, R.anim.icon_anim);
            mIconAnimation.setRepeatMode(Animation.REVERSE);
            convertView.setAnimation(mIconAnimation);
            mIconAnimation.startNow();
            Log.d(TAG, "startAnimation");
        } else {
            convertView.clearAnimation();
        }

        return convertView;

    }
}
