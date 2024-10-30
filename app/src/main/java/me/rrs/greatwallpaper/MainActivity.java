package me.rrs.greatwallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textview.MaterialTextView;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import me.rrs.greatwallpaper.admin.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private int clickCount = 0;
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadAds();
        setupEasterEgg();
    }

    private void setupEasterEgg() {
        MaterialTextView titleTextView = findViewById(R.id.titleTextView);

        titleTextView.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();

            // Check if the clicks are within the specified delay
            if (currentTime - lastClickTime < CLICK_DELAY) {
                clickCount++;
            } else {
                clickCount = 1; // Reset count if too much time has passed
            }

            lastClickTime = currentTime;

            if (clickCount >= 5) {
                // Open LoginActivity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                clickCount = 0; // Reset click count after opening
            }
        });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void initializeViews() {
        Button getWallpaperButton = findViewById(R.id.getWallpaperButton);
        Button uploadWallpaperButton = findViewById(R.id.uploadWallpaperButton);

        getWallpaperButton.setOnClickListener(v -> startActivity(new Intent(this, DownloadActivity.class)));
        uploadWallpaperButton.setOnClickListener(v -> {
            StartAppAd.showAd(this);
            startActivity(new Intent(this, UploadActivity.class));
        });

        // Set up the easter egg click listener
        setupEasterEgg();
    }


    private void loadAds() {
        Mrec startioMrec = findViewById(R.id.startioMrec);
        startioMrec.loadAd();

        Banner startioBanner = findViewById(R.id.startioBanner);
        startioBanner.loadAd();
    }
}
