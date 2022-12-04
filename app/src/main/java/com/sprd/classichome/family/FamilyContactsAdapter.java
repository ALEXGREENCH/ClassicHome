package com.sprd.classichome.family;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.common.util.HomeConstants;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher2.R;

import org.json.JSONArray;

/**
 * Created by SPRD on 2017/9/21.
 */

public class FamilyContactsAdapter extends BaseAdapter {
    private static final String TAG = "FamilyContactsAdapter";
    private final Context mContext;
    private final SparseArray<FamilyInfo> mAdapterItems = new SparseArray<>();

    private final SharedPreferences mSharedPrefs;

    private final GridView mGridView;

    FamilyContactsAdapter(Context context, GridView gridView) {
        mContext = context;
        mGridView = gridView;

        mSharedPrefs = mContext.getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        loadData();
    }

    private void loadData() {
        int maxItems = mContext.getResources().getInteger(R.integer.family_max_items);
        for (int i = 0; i < maxItems; i++) {
            if (mSharedPrefs.contains(Integer.toString(i))) {
                FamilyInfo info = getFamilyInfoFromSp(i);
                if (info != null) {
                    mAdapterItems.put(info.getFamilyId(), info);
                }
            }
        }
    }

    private FamilyInfo getFamilyInfoFromSp(int position) {
        FamilyInfo info = null;
        String[] strArray = new String[2];
        try {
            JSONArray jsonArray = new JSONArray(mSharedPrefs.getString(Integer.toString(position), ""));
            for (int i = 0; i < jsonArray.length(); i++) {
                strArray[i] = jsonArray.getString(i);
            }
            info = new FamilyInfo(position, strArray[0], strArray[1]);
        } catch (Exception e) {
            LogUtils.w(TAG, "getFamilyInfoFromSp exception " + position, e);
        }
        return info;
    }

    @Override
    public int getCount() {
        return mContext.getResources().getInteger(R.integer.family_max_items);
    }

    @Override
    public FamilyInfo getItem(int position) {
        return mAdapterItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int width = getItemWidth();
        int height = getItemHeight();
        ItemHolder itemHolder;
        if (convertView == null) {
            itemHolder = new ItemHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.family_info_item, null);
            convertView.setLayoutParams(new GridView.LayoutParams(width, height));

            itemHolder.itemImage = convertView.findViewById(R.id.family_info_image);
            itemHolder.itemEmptyName = convertView.findViewById(R.id.empty_family_info_name);
            itemHolder.itemName = convertView.findViewById(R.id.family_info_name);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
            ViewGroup.LayoutParams param = convertView.getLayoutParams();
            param.width = width;
            param.height = height;
            convertView.setLayoutParams(param);
        }

        FamilyInfo info = mAdapterItems.get(position);
        if (info != null) {
            itemHolder.itemImage.setVisibility(View.GONE);
            itemHolder.itemEmptyName.setVisibility(View.GONE);
            itemHolder.itemName.setVisibility(View.VISIBLE);

            itemHolder.itemName.setText(info.getFamilyName());
        } else {
            itemHolder.itemImage.setVisibility(View.VISIBLE);
            itemHolder.itemEmptyName.setVisibility(View.VISIBLE);
            itemHolder.itemName.setVisibility(View.GONE);

            String name = mContext.getString(R.string.family) + (position + 1);
            itemHolder.itemEmptyName.setText(name);
        }

        return convertView;
    }

    private int getItemWidth() {
        return mGridView.getMeasuredWidth() / mGridView.getNumColumns();
    }

    private int getItemHeight() {
        return getItemWidth();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    class ItemHolder {
        ImageView itemImage;
        TextView itemEmptyName;
        TextView itemName;
    }

    void updateAdapterItems(FamilyInfo info, boolean isNeedAdded) {
        if (info != null) {
            if (isNeedAdded) {
                saveAdapterItemInfo(info);
            } else {
                removeAdapterItemInfo(info);
            }
            notifyDataSetChanged();
        }
    }

    void addAdapterItem(int position, Uri uri) {
        ContentResolver contentResolver = mContext.getApplicationContext().getContentResolver();
        Cursor cursor = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                String phoneNum = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Data.DATA1));

                if (TextUtils.isEmpty(name)) {
                    name = phoneNum;
                }
                FamilyInfo info = new FamilyInfo(position, name, phoneNum);
                updateAdapterItems(info, true);
            }
        } catch (Exception ex) {
            LogUtils.w(TAG, "query contact data exception ", ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void saveAdapterItemInfo(FamilyInfo info) {
        mAdapterItems.put(info.getFamilyId(), info);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(info.getFamilyName());
        jsonArray.put(info.getFamilyNumber());

        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(Integer.toString(info.getFamilyId()), jsonArray.toString()).apply();
    }

    private void removeAdapterItemInfo(FamilyInfo info) {
        mAdapterItems.remove(info.getFamilyId());

        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.remove(Integer.toString(info.getFamilyId())).apply();
    }
}
