package com.example.emsismartpresence;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY_MS = 2000; // 2 seconds
    private ProgressBar progressBar;
    private ImageView splashLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        // Initialize views after setContentView
        progressBar = findViewById(R.id.splashProgressBar);
        splashLogo = findViewById(R.id.splashLogo);

        // Start the animation
        startLogoAnimation();

        // Use a Handler to delay the transition to the next activity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToSignIn();
            }
        }, SPLASH_DELAY_MS);
    }

    private void startLogoAnimation() {
        // Scale up animation
        splashLogo.setScaleX(0.5f);
        splashLogo.setScaleY(0.5f);
        splashLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Fade in animation for progress bar
        progressBar.setAlpha(0f);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(300)
                .start();
    }

    private void navigateToSignIn() {
        // Fade out animation before transition
        splashLogo.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // Start SignIn activity with a fade transition
                        Intent intent = new Intent(SplashScreenActivity.this, Signin.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}
