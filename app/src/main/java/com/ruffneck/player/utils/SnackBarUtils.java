package com.ruffneck.player.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by 佛剑分说 on 2015/11/20.
 */
public class SnackBarUtils {

    public static void showExceptionSnackBar(View view, Exception e, int duration) {

        final Snackbar snackbar = Snackbar.make(view, e.getMessage(), duration);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public static void showStringSnackBar(View view, String string, int duration) {

        final Snackbar snackbar = Snackbar.make(view, string, duration);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
