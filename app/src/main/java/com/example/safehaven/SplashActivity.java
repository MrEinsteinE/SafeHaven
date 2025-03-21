package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an ImageView to show the logo
        ImageView logoView = new ImageView(this);
        logoView.setImageResource(R.drawable.safe_haven_main_logo_1);
        logoView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        setContentView(logoView);

        // Delay for 2 seconds and then start MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
