package com.ruffneck.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by 佛剑分说 on 2015/11/19.
 */
public class FocusTextView extends TextView {

    public FocusTextView(Context context) {
        super(context);
    }

    public FocusTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
