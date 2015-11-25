package com.ruffneck.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruffneck.player.R;

public class MusicDetailView extends LinearLayout {

    public static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private TextView title;
    private TextView content;
    private String titleValue;
    private String contentValue;

    public MusicDetailView(Context context) {
        super(context);

        initView();
    }

    public MusicDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttrs(attrs);

        initView();
    }

    public MusicDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(attrs);

        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        titleValue = attrs.getAttributeValue(NAMESPACE, "titleName");
        contentValue = attrs.getAttributeValue(NAMESPACE, "content");
    }


    private void initView() {

        View.inflate(getContext(), R.layout.item_popup_detail,this);

        title = (TextView) findViewById(R.id.tv_detail_title);
        content = (TextView) findViewById(R.id.tv_detail_content);

        title.setText(titleValue);
        content.setText(contentValue);
    }

    public void setContent(String string){
        content.setText(string);
    }

}
