package com.sprd.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by SPRD on 16-8-24.
 */
public class TextToSpeechUtils {
    private static final String TAG = "TextToSpeechUtils";
    private static final String VOICE_FOR_MENU = "voice_for_menu";

    @SuppressLint("StaticFieldLeak")
    private static TextToSpeechUtils INSTANCE;
    @SuppressWarnings("FieldMayBeFinal")
    private TextToSpeech mTextToSpeech;
    @SuppressWarnings("FieldMayBeFinal")
    private Context mContext;

    public static TextToSpeechUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TextToSpeechUtils(context);
        }
        return INSTANCE;
    }

    private TextToSpeechUtils(Context context){
        mContext = context;
        mTextToSpeech = new TextToSpeech(context.getApplicationContext(), mTtsInitListener);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final TextToSpeech.OnInitListener mTtsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                LogUtils.d(TAG, "onInit() success");
                Locale locale = mContext.getResources().getConfiguration().locale;
                if(mTextToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE){
                    mTextToSpeech.setLanguage(locale);
                }else {
                    mTextToSpeech.setLanguage(Locale.US);
                }
            }
        }
    };

    public static void speak(String str, Context context) {
        try {
            TextToSpeechUtils textToSpeechUtils = TextToSpeechUtils.getInstance(context);
            boolean isVoiceEnable = (Settings.System.getInt(context.getContentResolver(), VOICE_FOR_MENU) == 1);
            LogUtils.d(TAG, "voice_for_menu = " + isVoiceEnable);
            if (isVoiceEnable) {
                textToSpeechUtils.mTextToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

}
