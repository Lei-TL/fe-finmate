package com.finmate.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.finmate.R;
import com.finmate.ui.auth.LoginActivity;
import com.finmate.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SplashViewModel viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        viewModel.isLoggedIn.observe(this, isLoggedIn -> {
            if (isLoggedIn) {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        });
    }
}
