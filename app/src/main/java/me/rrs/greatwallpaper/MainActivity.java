package me.rrs.greatwallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        loadAds();
    }

    private void initializeViews() {
        Button getWallpaperButton = findViewById(R.id.getWallpaperButton);
        Button uploadWallpaperButton = findViewById(R.id.uploadWallpaperButton);

        getWallpaperButton.setOnClickListener(v -> startActivity(new Intent(this, DownloadActivity.class)));
        uploadWallpaperButton.setOnClickListener(v -> {
            StartAppAd.showAd(this);
            startActivity(new Intent(this, UploadActivity.class));
        });
    }

    private void loadAds() {
        Mrec startioMrec = findViewById(R.id.startioMrec);
        startioMrec.loadAd();

        Banner startioBanner = findViewById(R.id.startioBanner);
        startioBanner.loadAd();
    }
}
