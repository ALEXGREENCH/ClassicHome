package com.sprd.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.GridView;

import com.sprd.PlatformHelper;

/**
 * Created by SPRD on 12/18/17.
 */
public class LoopGridView extends GridView {

    @SuppressWarnings("FieldMayBeFinal")
    private boolean isLoop = true;

    public LoopGridView(Context context) {
        super( context );
    }

    public LoopGridView(Context context, AttributeSet attrs) {
        super( context, attrs );
    }

    public LoopGridView(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                result = PlatformHelper.isLayoutRtl(this) ? focusToNextItem() : focusToPreItem();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                result = PlatformHelper.isLayoutRtl(this) ? focusToPreItem() : focusToNextItem();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                result = isNeedFocusToEnd();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                result = isNeedFocusToStart();
                break;
        }

        return result || super.onKeyDown( keyCode, event );
    }

    private boolean focusToNextItem() {
        boolean result = false;
        boolean isLast = isAnyLineLast();
        int nextPos = getSelectedItemPosition() + 1;
        if (nextPos < getCount() && isLast) {
            setSelection( nextPos );
            result = true;
        }
        if (isLoop && nextPos == getCount()) {
            setSelection( 0 );
            result = true;
        }
        if(result){
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(View.FOCUS_RIGHT));
        }
        return result;
    }

    private boolean focusToPreItem() {
        boolean result = false;
        boolean isFirst = isAnyLineFirst();
        int nextPos = getSelectedItemPosition() - 1;
        if (nextPos >= 0 && isFirst) {
            setSelection( nextPos );
            result = true;
        }
        if (isLoop && nextPos == -1) {
            setSelection( getCount() - 1 );
            result = true;
        }
        if(result){
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(View.FOCUS_LEFT));
        }
        return result;
    }

    private boolean isNeedFocusToStart() {
        boolean result = false;
        if (isLoop && isLastLine()) {
            setSelection( 0 );
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(View.FOCUS_DOWN));
            result = true;
        }
        return result;
    }

    private boolean isNeedFocusToEnd() {
        boolean result = false;
        if (isLoop && isFirstLine()) {
            setSelection( getCount() - 1 );
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(View.FOCUS_UP));
            result = true;
        }
        return result;
    }

    /**
     * Judge whether the item is the last one of a line
     */
    private boolean isAnyLineLast() {
        int curPos = getSelectedItemPosition();
        int columnNum = getNumColumns();
        return columnNum != 0 && (curPos + 1) % columnNum == 0;

    }

    /**
     * Judge whether the item is the first one of a line
     */
    private boolean isAnyLineFirst() {
        int curPos = getSelectedItemPosition();
        int columnNum = getNumColumns();
        return columnNum != 0 && curPos % columnNum == 0;
    }

    /**
     * Judge whether the item is the first one of a line
     */
    private boolean isFirstLine() {
        return getSelectedItemPosition() < getNumColumns();

    }

    /**
     * Judge whether the item is the first one of a line
     */
    private boolean isLastLine() {
        int count = getCount();
        int select = getSelectedItemPosition();
        int remainder = count % getNumColumns();
        if (remainder == 0) {
            remainder = getNumColumns();
        }
        return (count - select) <= remainder;

    }
}
