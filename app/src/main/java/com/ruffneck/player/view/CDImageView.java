package com.ruffneck.player.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class CDImageView extends CircleImageView {

    public CDImageView(Context context) {
        super(context);
    }

    public CDImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CDImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
