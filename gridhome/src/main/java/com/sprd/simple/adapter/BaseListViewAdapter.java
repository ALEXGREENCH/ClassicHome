package com.sprd.simple.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPDR on 2016/11/11.
 */

public abstract class BaseListViewAdapter<T> extends BaseAdapter {
    protected List<T> mInfos = new ArrayList<T>();
    protected Context mContext;
    protected int mPosition;

    public BaseListViewAdapter(Context context, List<T> infos) {
        mInfos = infos;
        mContext = context;
    }

    public void setPosition(int pos) {
        mPosition = pos;
    }

    public int getPosition() {
        return mPosition;
    }

    @Override
    public int getCount() {
        return mInfos != null ? mInfos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (position > -1 && position < mInfos.size()) {
            return mInfos.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
