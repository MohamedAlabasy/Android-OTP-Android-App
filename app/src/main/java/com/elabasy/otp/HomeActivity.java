package com.elabasy.otp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.os.Bundle;

import com.elabasy.otp.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        @SuppressLint("SetTextI18n")
        Thread thread = new Thread(() -> {
            for (int i = 4; i >= 0; i--) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int finalI = i;
                runOnUiThread(() -> {
                    binding.numberOfSecond.setText(finalI + "");
                });
            }
            finish();
        });
        thread.start();
    }
}