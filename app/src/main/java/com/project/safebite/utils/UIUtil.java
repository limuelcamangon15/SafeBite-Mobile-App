package com.project.safebite.utils;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.project.safebite.R;

public class UIUtil {

    public UIUtil(){

    }

    public static void showLoading(Context context, MaterialButton button, ProgressBar progressBar) {
        button.setEnabled(false);
        button.setText("");
        progressBar.setVisibility(View.VISIBLE);
        button.setEnabled(false);
        button.setBackgroundTintList(
                ContextCompat.getColorStateList(context, R.color.green_disabled)
        );
    }

    public static void hideLoading(Context context, MaterialButton button, ProgressBar progressBar, String buttonLabel) {
        button.setEnabled(true);
        button.setText(buttonLabel);
        progressBar.setVisibility(View.GONE);
        button.setEnabled(true);
        button.setBackgroundTintList(
                ContextCompat.getColorStateList(context, R.color.green)
        );
    }

    public static void showSnackbar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }
}
