package com.sprd.classichome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.simple.launcher2.R;

import java.util.ArrayList;

/**
 * Created by SPRD on 2017/9/30.
 */

public class BaseListAdapter extends BaseAdapter{

    Context mContext;
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<AppItemInfo> mApps = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private LayoutInflater mInflater;

    BaseListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void setApps(ArrayList<AppItemInfo> apps) {
        mApps.clear();
        mApps.addAll(apps);
    }

    @Override
    public int getCount() {
        return mApps.size();
    }

    @Override
    public Object getItem(int position) {
        return mApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.base_list_item, null);

        }

        AppItemInfo info = mApps.get(position);
        ImageView appIcon = convertView.findViewById(R.id.list_item_icon);
        appIcon.setImageDrawable(info.icon);

        TextView appName = convertView.findViewById(R.id.list_item_title);
        appName.setText(info.title);
        convertView.setTag(info);
        return convertView;
    }

    public void notifyAppsUpdated(ArrayList<AppItemInfo> apps) {
        setApps(apps);
        notifyDataSetChanged();
    }
}
