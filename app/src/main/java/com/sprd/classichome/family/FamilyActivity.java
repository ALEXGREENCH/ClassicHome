package com.sprd.classichome.family;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.LogUtils;
import com.sprd.common.util.Utilities;
import com.sprd.simple.launcher2.R;

/**
 * Created by SPRD on 2017/9/21.
 */

public class FamilyActivity extends Activity implements AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private final static String TAG = "FamilyActivity";

    private GridView mGridView;
    private FamilyContactsAdapter mAdapter;
    private FeatureBarHelper mFeatureBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family_main);
        mGridView = findViewById(R.id.family_grid_view);
        initGridView();
        setSoftKey();
    }

    private void initGridView() {
        if (mGridView != null) {
            mAdapter = new FamilyContactsAdapter(this, mGridView);
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemClickListener(this);
            mGridView.setOnItemLongClickListener(this);
            mGridView.setOnItemSelectedListener(this);
            mGridView.setSelection(0);
        }
    }

    private void setSoftKey() {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(this);
        }

        FeatureBarUtil.hideSoftKey(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!doActionCall(position)) {
            doActionPick(position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isFinishing() && !showDeleteDialog(position)) {
            doActionPick(position);
        }
        return true;
    }

    private boolean showDeleteDialog(int position) {
        boolean isShowDialog = false;
        final FamilyInfo info = mAdapter.getItem(position);
        if (info != null) {
            isShowDialog = true;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.remove_title);
            builder.setMessage(R.string.remove_info);
            builder.setPositiveButton(R.string.remove_confirm, (dialog, which) -> {
                mAdapter.updateAdapterItems(info, false);
                dialog.dismiss();
            });

            builder.setNegativeButton(R.string.remove_cancel, (dialog, which) -> dialog.dismiss());
            if (!isFinishing()) {
                builder.create().show();
            }
        }
        return isShowDialog;
    }

    private boolean doActionCall(final int position) {
        boolean ret = false;
        if (mAdapter != null && position >= 0 && position < mAdapter.getCount()) {
            final FamilyInfo info = mAdapter.getItem(position);
            if (info != null) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + info.getFamilyNumber());
                intent.setData(data);
                ret = Utilities.startActivity(this, intent);
            }
        }
        return ret;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean doActionPick(final int position) {
        boolean ret = false;
        if (mAdapter != null && position >= 0 && position < mAdapter.getCount()) {
            final FamilyInfo info = mAdapter.getItem(position);
            if (info == null) {
                Intent contactsIntent = new Intent(Intent.ACTION_PICK);
                contactsIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(contactsIntent, position);
                ret = true;
            } else {
                LogUtils.w(TAG, "doActionPick fail, position[" + position + "] is not empty.");
            }
        }
        return ret;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        if (LogUtils.DEBUG_ALL) LogUtils.d(TAG, "onItemSelected, position:" + position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if (LogUtils.DEBUG_ALL) LogUtils.d(TAG, "onNothingSelected");
    }

    @Override
    protected void onActivityResult(int position, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                Uri uri = data.getData();
                mAdapter.addAdapterItem(position, uri);
                break;
            case Activity.RESULT_CANCELED:
                // Do nothing in the contact select list
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;

        if (event.getRepeatCount() == 0) {
            event.startTracking();
            result = true;
        }
        return result;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = false;
        if (event.isTracking() && !event.isCanceled()) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (keyCode) {
                case KeyEvent.KEYCODE_CALL:
                    result = doActionCall(mGridView.getSelectedItemPosition());
                    break;
                default:
                    result = super.onKeyUp(keyCode, event);
                    break;
            }
        }
        return result;
    }
}
