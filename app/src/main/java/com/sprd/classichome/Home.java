package com.sprd.classichome;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.os.SystemProperties;
import android.provider.Settings;
//import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
//import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

////////import com.android.internal.telephony.IccCardConstants;
////////import com.android.internal.telephony.TelephonyIntents;
////////import com.android.internal.telephony.IccCardConstants.State;
import com.sprd.classichome.model.HomeMonitorCallbacks;
//import com.sprd.classichome.util.IconUtilities;
import com.sprd.classichome.util.UtilitiesExt;
import com.sprd.common.FeatureOption;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.common.util.Utilities;
import com.sprd.common.util.FlashlightController;
import com.sprd.simple.launcher2.R;

/**
 * Created by SPRD on 9/22/17.
 */
public class Home extends BaseHomeActivity {

    private static final String TAG = "Home";

    private HomeStatusView mHomeStatus;
    private ComponentName mLeftCn;
    private ComponentName mRightCn;

    private static final int MSG_CARRIER_INFO_UPDATE = 100;
    private static final int MSG_SIM_STATE_CHANGE = 101;
    ////////private IccCardConstants.State mSimState;
    private CharSequence mTelephonyPlmn;
    private CharSequence mTelephonySpn;
    private TextView mCarrierViews;
    @SuppressWarnings("FieldCanBeLocal")
    private CharSequence mCarrierTexts;
    private static CharSequence mSeparator;
    private IntentFilter mIntentFilter;

    @SuppressWarnings("FieldMayBeFinal")
    private HomeMonitorCallbacks mCallback = new HomeMonitorCallbacks() {
        @Override
        public void onDateChanged() {
            if (mHomeStatus != null) {
                mHomeStatus.updateLunarDateView();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        ((HomeApplication) getApplication()).setHomeCallback(mCallback);
        setupViews();
        enableWallpaperShowing(getResources().getBoolean(R.bool.idle_home_with_wallpaper));
        if(FeatureOption.SPRD_SHOW_CARRIER_SUPPORT){
            mSeparator = getResources().getString(R.string.desktop_text_message_separator);
            mTelephonyPlmn = getDefaultPlmn();
            ////////mSimState = IccCardConstants.State.UNKNOWN;
            mCarrierViews = findViewById(R.id.sim_label);
            mCarrierViews.setVisibility(View.VISIBLE);
            mCarrierViews.setGravity(Gravity.CENTER_HORIZONTAL);
            mCarrierViews.setSingleLine();
            mCarrierViews.setEllipsize(TruncateAt.MARQUEE);
            mCarrierViews.setFocusable(true);
            mIntentFilter = new IntentFilter();
            ////////mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
            ////////mIntentFilter.addAction(TelephonyIntents.SPN_STRINGS_UPDATED_ACTION);
        }

        FlashlightController.checkFlashlightStatus(this);
    }

    private void setupViews() {
        mHomeStatus = findViewById(R.id.default_clock_view);
        mLeftCn = UtilitiesExt.getLFComponentName(this);
        mRightCn = UtilitiesExt.getRTComponentName(this);
        setSoftKey();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FeatureOption.SPRD_SHOW_CARRIER_SUPPORT){
            Log.d(TAG, "regist carrier text broadcast");
            registerReceiver(mBroadcastReceiver, mIntentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(FeatureOption.SPRD_SHOW_CARRIER_SUPPORT){
            Log.d(TAG, "unregist carrier text broadcast");
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    protected void setSoftKey() {
        setupFeatureBar(this);
        FeatureBarUtil.setIcon(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.MDK, R.drawable.main_menu);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK, R.color.classichome_softbar_font_color);
        FeatureBarUtil.setText(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK, Utilities.loadAppLabel(this, mLeftCn));
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.RTK, R.color.classichome_softbar_font_color);
        FeatureBarUtil.setText(mFeatureBarHelper, FeatureBarUtil.SoftKey.RTK, Utilities.loadAppLabel(this, mRightCn));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((HomeApplication) getApplication()).removeHomeCallback(mCallback);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)) {
            return super.onKeyUp(keyCode, event);
        } else {
          Log.d(TAG, "isLauncherNeedUseKeycode");
        }

        boolean result = false;
        if (event.isTracking() && !event.isCanceled()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    UtilitiesExt.goMainMenu(this);
                    Log.d(TAG, "goMainMenu");
                    result = true;
                    break;
                case KeyEvent.KEYCODE_MENU:
                    Utilities.startActivity(this, mLeftCn);
                    result = true;
                    break;
                case KeyEvent.KEYCODE_BACK:
                    Utilities.startActivity(this, mRightCn);
                    result = true;
                    break;
                default:
                    break;
            }
        }

        if (!result) {
            result = super.onKeyUp(keyCode, event);
        }
        return result;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "action .." + action);
            ////////if ((action).equals(TelephonyIntents.SPN_STRINGS_UPDATED_ACTION)) {
            ////////    mTelephonyPlmn = getTelephonyPlmnFrom(intent);
            ////////    mTelephonySpn = getTelephonySpnFrom(intent);
            ////////    Log.d(TAG, "Plmn : " + mTelephonyPlmn + " Spn : " + mTelephonySpn);
            ////////    Message msg = mHandler.obtainMessage(MSG_CARRIER_INFO_UPDATE);
            ////////    mHandler.sendMessage(msg);
            ////////} else if ((action).equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            ////////    Log.d(TAG, "sim state : " +
            ////////          intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE));
            ////////    mHandler.sendMessage(mHandler.obtainMessage(
            ////////            MSG_SIM_STATE_CHANGE, SimArgs.fromIntent(intent)));
            ////////}
        }
    };

    /*
    @SuppressLint("HandlerLeak")
    @SuppressWarnings("deprecation")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CARRIER_INFO_UPDATE) {
                handleCarrierInfoUpdate();
            } else if (msg.what == MSG_SIM_STATE_CHANGE) {
                SimArgs simArgs = (SimArgs) msg.obj;
                final IccCardConstants.State state = simArgs.simState;
                if (state != mSimState) {
                    handleSimStateChange(simArgs);
                }
            }
        }
    };
     */

    private CharSequence getDefaultPlmn() {
        return getResources().getText(R.string.desktop_carrier_default);
    }

    /*
    private CharSequence getTelephonyPlmnFrom(Intent intent) {
        if (intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_PLMN, false)) {
            final String plmn = intent.getStringExtra(TelephonyIntents.EXTRA_PLMN);
            return (plmn != null) ? plmn : getDefaultPlmn();
        }
        return null;
    }
     */

    /*
    private CharSequence getTelephonySpnFrom(Intent intent) {
        if (intent.getBooleanExtra(TelephonyIntents.EXTRA_SHOW_SPN, false)) {
            final String spn = intent.getStringExtra(TelephonyIntents.EXTRA_SPN);
            //noinspection RedundantIfStatement
            if (spn != null) {
                return spn;
            }
        }
        return null;
    }
     */

    /*
    private StatusMode getStatusForIccState(IccCardConstants.State simState) {
        if (simState == null) {
            return StatusMode.Normal;
        }
        final boolean missingAndNotProvisioned = !isDeviceProvisionedInSettingsDb()
                && (simState == IccCardConstants.State.ABSENT ||
                        simState == IccCardConstants.State.PERM_DISABLED);
        simState = missingAndNotProvisioned ?
                IccCardConstants.State.NETWORK_LOCKED : simState;
        switch (simState) {
            case ABSENT:
                return StatusMode.SimMissing;
            case NETWORK_LOCKED:
                return StatusMode.NetworkLocked;
            case NOT_READY:
                return StatusMode.SimNotReady;
            case PIN_REQUIRED:
                return StatusMode.SimLocked;
            case PUK_REQUIRED:
                return StatusMode.SimPukLocked;
            case READY:
                return StatusMode.Normal;
            case PERM_DISABLED:
                return StatusMode.SimPermDisabled;
            case NETWORK_SUBSET_LOCKED:
                return StatusMode.NetworkSubsetLocked;
            case SERVICE_PROVIDER_LOCKED:
                return StatusMode.ServiceProviderLocked;
            case CORPORATE_LOCKED:
                return StatusMode.CorporateLocked;
            case UNKNOWN:
                //noinspection DuplicateBranchesInSwitch
                return StatusMode.SimMissing;
        }
        return StatusMode.SimMissing;
    }
    */
    /*
    private static class SimArgs {
        public final IccCardConstants.State simState;

        SimArgs(IccCardConstants.State state) {
            simState = state;
        }

        static SimArgs fromIntent(Intent intent) {
            IccCardConstants.State state;
            String stateExtra = intent.getStringExtra(
                    IccCardConstants.INTENT_KEY_ICC_STATE);
            if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
                final String absentReason = intent
                        .getStringExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON);
                if (IccCardConstants.INTENT_VALUE_ABSENT_ON_PERM_DISABLED.equals(
                        absentReason)) {
                    state = IccCardConstants.State.PERM_DISABLED;
                } else {
                    state = IccCardConstants.State.ABSENT;
                }
            } else if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
                state = IccCardConstants.State.READY;
            } else if (IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
                final String lockedReason = intent
                        .getStringExtra(IccCardConstants.INTENT_KEY_LOCKED_REASON);
                if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                    state = IccCardConstants.State.PIN_REQUIRED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_ON_PUK.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.PUK_REQUIRED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_NETWORK.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.NETWORK_LOCKED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_SIM.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.SIM_LOCKED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_NETWORK_SUBSET.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.NETWORK_SUBSET_LOCKED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_SERVICE_PROVIDER.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.SERVICE_PROVIDER_LOCKED;
                } else if (IccCardConstants.INTENT_VALUE_LOCKED_CORPORATE.equals(
                        lockedReason)) {
                    state = IccCardConstants.State.CORPORATE_LOCKED;
                } else {
                    state = IccCardConstants.State.UNKNOWN;
                }
            } else if (IccCardConstants.INTENT_VALUE_LOCKED_NETWORK.equals(
                    stateExtra)) {
                state = IccCardConstants.State.NETWORK_LOCKED;
            } else if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stateExtra)
                    || IccCardConstants.INTENT_VALUE_ICC_IMSI.equals(stateExtra)) {
                state = IccCardConstants.State.READY;
            } else {
                state = IccCardConstants.State.UNKNOWN;
            }
            return new SimArgs(state);
        }

        @SuppressWarnings("NullableProblems")
        public String toString() {
            return simState.toString();
        }
    }
    */
    /*
    private void handleSimStateChange(SimArgs simArgs) {
        final IccCardConstants.State state = simArgs.simState;
        if (state != IccCardConstants.State.UNKNOWN) {
            mSimState = state;
            updateCarrierText(mSimState, mTelephonyPlmn, mTelephonySpn);
        }
    }
    */
    /*
    private void handleCarrierInfoUpdate() {
        updateCarrierText(mSimState, mTelephonyPlmn, mTelephonySpn);
    }
     */
    /*
    private void updateCarrierText(IccCardConstants.State simState,
            CharSequence plmn, CharSequence spn) {
        mCarrierTexts = getCarrierTextForSimState(simState, plmn, spn);
        mCarrierViews.setText(mCarrierTexts);
    }
    */
    /*
    private CharSequence getCarrierTextForSimState(IccCardConstants.State simState,
            CharSequence plmn, CharSequence spn) {
        CharSequence carrierText;
        StatusMode status = getStatusForIccState(simState);
        switch (status) {
            case Normal:
                carrierText = makeCarierString(plmn, spn);
                break;
            case SimNotReady:
                //noinspection DuplicateBranchesInSwitch
                carrierText = makeCarierString(plmn, spn);
                break;
            case NetworkLocked:
                carrierText = makeCarierString(plmn,
                        getText(R.string.desktop_network_locked_message));
                break;
            case SimMissing:
                carrierText = makeCarierString(plmn,
                        getText(R.string.desktop_missing_sim_message_short));
                break;
            case SimPermDisabled:
                carrierText = getText(
                        R.string.desktop_permanent_disabled_sim_message_short);
                break;
            case SimMissingLocked:
                //noinspection DuplicateBranchesInSwitch
                carrierText = makeCarierString(plmn,
                        getText(R.string.desktop_missing_sim_message_short));
                break;
            case SimLocked:
                carrierText = plmn;
                break;
            case NetworkSubsetLocked:
                carrierText = makeCarierString(plmn,
                        getText(R.string.lockscreen_sim_nws_locked_mssage));
                break;
            case ServiceProviderLocked:
                carrierText = makeCarierString(plmn,
                        getText(R.string.lockscreen_sim_sp_locked_mssage));
                break;
            case CorporateLocked:
                carrierText = makeCarierString(plmn,
                        getText(R.string.lockscreen_sim_corporate_locked_mssage));
                break;
            case SimPinLocked:
                carrierText = makeCarierString(plmn,
                        getText(R.string.lockscreen_sim_pin_locked_message));
                break;
            case SimPukLocked:
                //noinspection DuplicateBranchesInSwitch
                carrierText = plmn;
                break;
            default:
                //noinspection ConstantConditions
                carrierText = null;
        }
        return carrierText;
    }
     */
    private static CharSequence makeCarierString(CharSequence plmn, CharSequence spn) {
        return concatenate(plmn, spn);
    }

    private static CharSequence concatenate(CharSequence plmn, CharSequence spn) {
        final boolean plmnValid = !TextUtils.isEmpty(plmn);
        final boolean spnValid = !TextUtils.isEmpty(spn);
        if (plmnValid && spnValid) {
            //noinspection StringBufferReplaceableByString
            return new StringBuilder().append(plmn).append(
                    mSeparator).append(spn).toString();
        } else if (plmnValid) {
            return plmn;
        } else if (spnValid) {
            return spn;
        } else {
            return "";
        }
    }

    private enum StatusMode {
        // Normal case (sim card present, it's not locked)
        Normal,
        // SIM card is 'network locked'.
        NetworkLocked,
        // SIM card is missing.
        SimMissing,
        // SIM card is missing, and device isn't provisioned; don't allow access
        SimMissingLocked,
        // SIM card is PUK locked because SIM entered wrong too many times
        SimPukLocked,
        // SIM card is currently locked
        SimLocked,
        // SIM card is permanently disabled due to PUK unlock failure
        SimPermDisabled,
        // SIM is not ready yet. May never be on devices w/o a SIM.
        SimNotReady,
        //network subset lock
        NetworkSubsetLocked,
        //Service Provider lock
        ServiceProviderLocked,
        //Corporate lock
        CorporateLocked,
        SimPinLocked
    }

    private boolean isDeviceProvisionedInSettingsDb() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) != 0;
    }
}
