package com.ruffneck.player.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by 佛剑分说 on 2015/11/19.
 */
public class MySeekBar extends SeekBar {

    public MySeekBar(Context context) {
        super(context);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        System.out.println("MySeekBar.onFocusChanged");
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

    }
}
