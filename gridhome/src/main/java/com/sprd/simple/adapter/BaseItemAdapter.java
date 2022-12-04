package com.sprd.simple.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.List;

/**
 * Created by SPRD on 2016/7/19.
 */
public class BaseItemAdapter extends BaseListViewAdapter<AppInfo> {

    private final static String TAG = "BaseItemAdapter";

    public BaseItemAdapter(Context context, List<AppInfo> icons) {
        super(context, icons);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IconHolder iconHolder = null;
        if (convertView == null) {
            iconHolder = new IconHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.tools_appinfo_item, null);
            iconHolder.iconName = (TextView) convertView.findViewById(R.id.list_item_name_icon);
            iconHolder.iconImage = (ImageView) convertView.findViewById(R.id.list_item_image_icon);
            convertView.setTag(iconHolder);
        } else {
            iconHolder = (IconHolder) convertView.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        iconHolder.iconName.setText(appInfo.getAppLabel());
        if(appInfo.getAppDrawableIcon() != null) {
            iconHolder.iconImage.setImageDrawable(appInfo.getAppDrawableIcon());
        }else{
            iconHolder.iconImage.setImageResource(mInfos.get(position).getAppIcon());
        }

        if (mPosition == position) {
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.selected_list_color));
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    class IconHolder {
        private TextView iconName;
        private ImageView iconImage;
    }

}
