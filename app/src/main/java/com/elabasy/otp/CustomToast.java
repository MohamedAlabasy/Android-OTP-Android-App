package com.elabasy.otp;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {
    private final Activity activity;
    private TextView textView;
    private View view;
    private LayoutInflater inflater;
    private Toast toast;

    public CustomToast(Activity activity) {
        this.activity = activity;
    }

    public void toastError(String text) {
        inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.toast_error, activity.findViewById(R.id.toast_layout_root_error), false);
        toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_LONG);
        textView = view.findViewById(R.id.toast_tv_text_error);
        textView.setText(text);
        toast.setView(view);
        toast.show();
    }

    public void toastSuccess(String text) {
        inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.toast_success, activity.findViewById(R.id.toast_layout_root_success), false);
        toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_LONG);
        textView = view.findViewById(R.id.toast_tv_text_success);
        textView.setText(text);
        toast.setView(view);
        toast.show();
    }

}
