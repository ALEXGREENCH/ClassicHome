package com.sprd.wallpaperpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sprd.common.util.LogUtils;

public class SetWallpaperAdapter extends BaseAdapter {
    private static final boolean DEBUG = LogUtils.DEBUG_ALL;
    private static final String TAG = "SetWallpaperAdapter";
    private Context mContext = null;

    public SetWallpaperAdapter(Context context) {
        if (DEBUG) LogUtils.i(TAG, "SetWallpaperAdapter");
        mContext = context;
    }

    @Override
    public int getCount() {
        if (DEBUG) LogUtils.i(TAG, "getCount");
        return WallpaperUtil.getWallpaperResCount(mContext);
    }

    @Override
    public Object getItem(int item) {
        if (DEBUG) LogUtils.i(TAG, "getItem = " + item);
        return WallpaperUtil.getWallpaperRes(mContext, item);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) LogUtils.i(TAG, "getItemId , position = " + position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (DEBUG) LogUtils.i(TAG, "getView , position = " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wallpaper_item, null);
            ImageView imageView = convertView.findViewById(R.id.wallpaper_item);
            imageView.setImageResource(WallpaperUtil.getWallpaperRes(mContext, position));
            convertView.setTag(imageView);
        } else {
            ImageView imageView = (ImageView)convertView.getTag();
            imageView.setImageResource(WallpaperUtil.getWallpaperRes(mContext, position));
        }
        return convertView;
    }
}
