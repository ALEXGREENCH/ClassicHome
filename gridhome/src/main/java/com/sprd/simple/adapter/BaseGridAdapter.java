package com.sprd.simple.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.common.util.HomeConstants;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by SPDR on 2016/10/25.
 */

public abstract class BaseGridAdapter extends BaseAdapter {
    private static final String TAG = "BaseGridAdapter";

    protected List<AppInfo> mArrayList;
    protected Context mContext;
    protected GridView mGridView;
    protected int mPosition = 0;
    private int unCount;

    public BaseGridAdapter(List<AppInfo> arrayList, Context context, GridView gridView) {
        mArrayList = arrayList;
        mContext = context;
        mGridView = gridView;
        saveDesktopAppInfo(context, arrayList);
    }
    /**
     * write desktop apk info to sharedPreferences
     *
     * @param context
     * @param appInfos
     */
    private void saveDesktopAppInfo(Context context, List<AppInfo> appInfos) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HomeConstants.SP_NAME,
                Context.MODE_PRIVATE);
        Set<String> packageNameSet = sharedPreferences.getStringSet(HomeConstants.HOME_APPS_NAMES, null);
        if (packageNameSet == null) {
            packageNameSet = new HashSet<String>();
        }
        for (AppInfo info : appInfos) {
            String packageName = info != null ? info.getAppPkg() : "";
            if (!TextUtils.isEmpty(packageName)) {
                packageNameSet.add(packageName);
            }
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(HomeConstants.HOME_APPS_NAMES, packageNameSet);
        editor.commit();
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public int getItemHeight() {
        int maxItems = mContext.getResources().getInteger(R.integer.grid_view_max_items);
        return calculateItemHeigh(maxItems);
    }

    public int calculateItemHeigh(int count){
        float horizontalSpacing = mContext.getResources().getDimension(R.dimen.horizontalSpacing_grid_view);
        int gridHeight = mGridView.getHeight();

        int numColumns = mGridView.getNumColumns();
        int itemHeight = 0;
        if(numColumns > 0) {
            int row = (count + (numColumns - 1)) / numColumns;
            itemHeight = (int) ((gridHeight - horizontalSpacing * (row - 1)) / (row));
            return itemHeight;
        }else{
            LogUtils.e(TAG, "numColumns is invalid value");
            return itemHeight;
        }
    }

    public void setSelectPosition(int position) {
        mPosition = position;
    }

    public int getSelectPosition() {
        return mPosition;
    }

    public int getUnCount() {
        return unCount;
    }

    public void setUnCount(int unCount) {
        this.unCount = unCount;
    }

    public GradientDrawable getGradientDrawable(int position){
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.shape_corners_radius));
        if (mArrayList != null && position > -1 && position < mArrayList.size()) {
            gradientDrawable.setColor(mContext.getResources().getColor(mArrayList.get(position).getBackground()));
        }
        return gradientDrawable;
    }

    class ItemHolder {
        TextView itemName;
        ImageView itemImage;
        ImageView itemBackground;
        TextView itemValue;
    }
}
