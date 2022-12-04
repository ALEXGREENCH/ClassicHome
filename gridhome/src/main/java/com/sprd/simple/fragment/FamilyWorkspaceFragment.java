package com.sprd.simple.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.sprd.common.util.HomeConstants;
import com.sprd.simple.adapter.FamilyAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.util.PackageInfoUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by SPRD on 2016/7/19.
 */
public class FamilyWorkspaceFragment extends LauncherFragment<AppInfo> {
    private static final String TAG = "FamilyWorkspaceFragment";
    private String[] spData = new String[2];
    private static String[] mStrArray = new String[2];
    private final static int ITEM_COUNT = 6;

    @Override
    protected List<AppInfo> loadData() {
        List<AppInfo> datas = new ArrayList<AppInfo>();
        SharedPreferences sp = getActivity().getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        for (int i = 0; i < ITEM_COUNT; i++) {
            int picResId = R.array.family_pic_bg;
            String iconName = getResources().getString(R.string.family) + (i + 1);
            if (sp.contains(i + "")) {
                // Contact has added in the family list
                getData(getActivity(), i, spData.length);
                iconName = mStrArray[0];
            }
            AppInfo appInfo = PackageInfoUtil.loadAppInfo(getActivity(), picResId, i, iconName);
            datas.add(appInfo);
        }
        return datas;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = new FamilyAdapter(getActivity(), mDatas, mGridView);
        mGridView.setAdapter(mAdapter);

        return rootView;
    }

    private void updateGridView(int position, String iconName) {
        AppInfo appInfo = PackageInfoUtil.loadAppInfo(getActivity(), R.array.family_pic_bg, position, iconName);
        if (mDatas != null) {
            mDatas.remove(position);
            mDatas.add(position, appInfo);
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * get the data from SharedPreference
     *
     * @param context
     * @param position
     * @param arrayLength
     * @return
     */
    public String[] getData(Context context, int position, int arrayLength) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
            mStrArray = new String[arrayLength];
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(position + "", ""));
            for (int i = 0; i < jsonArray.length(); i++) {
                mStrArray[i] = jsonArray.getString(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStrArray;
    }

    /**
     * save the data to SharedPreference
     *
     * @param contexts
     * @param position
     * @param strings
     */
    public void saveData(Context contexts, int position, String[] strings) {
        SharedPreferences sp = contexts.getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();
        for (String str : strings) {
            jsonArray.put(str);
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(position + "", jsonArray.toString());
        editor.commit();
        editor.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences sp = getActivity().getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        if (sp.contains(position + "")) {
            getData(getActivity(), position, spData.length);
            Intent intent = new Intent(Intent.ACTION_CALL);
            Log.d(TAG, "strArray[1] = " + mStrArray[1]);
            Uri data = Uri.parse("tel:" + mStrArray[1]);
            intent.setData(data);
            startActivity(intent);
        } else {
            Intent contactsIntent = new Intent(Intent.ACTION_PICK);
            contactsIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(contactsIntent, position);
            Log.d(TAG, "startActivity " + position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        SharedPreferences sp = getActivity().getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
        //Family added contact or not
        if (sp.contains(position + "")) {
            getData(getActivity(), position, spData.length);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.remove_title);
            builder.setMessage(R.string.remove_info);
            builder.setPositiveButton(R.string.remove_confirm, (dialog, which) -> {
                // data change update view
                updateGridView(position, getResources().getString(R.string.family) + (position + 1));

                SharedPreferences sp1 = getActivity().getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp1.edit();
                editor.remove(position + "");
                editor.commit();

                dialog.dismiss();
            });

            builder.setNegativeButton(R.string.remove_cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else {
            Intent contactsIntent = new Intent(Intent.ACTION_PICK);
            contactsIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(contactsIntent, position);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "position = " + position);
        mAdapter.setSelectPosition(position);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int position, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                Log.d(TAG, "RESULT_OK = " + Activity.RESULT_OK + " position = " + position);
                Uri uri = data.getData();
                Log.d(TAG, "uri = " + uri);

                ContentResolver contentResolver = getActivity().getContentResolver();
                Cursor cursor = null;
                try {
                    cursor = contentResolver.query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        //get the app_phone number
                        String phoneNum = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
                        Log.d(TAG, "phoneNum = " + phoneNum);

                        //get the name
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                        Log.d(TAG, "name = " + name);
                        spData[0] = name;
                        spData[1] = phoneNum;

                        // data change update view
                        updateGridView(position, name);

                        //save the data
                        saveData(getActivity(), position, spData);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                        cursor = null;
                    }
                }
                break;
            case Activity.RESULT_CANCELED:
                // Do nothing in the contact select list
                Log.d(TAG, "RESULT_CANCELED");
                break;
            default:
                break;
        }
    }

    @Override
    protected void registerContentObservers() {
        // TODO: 16-11-28
    }

    @Override
    protected void unregisterContentObservers() {
        // TODO: 16-11-30
    }
}
