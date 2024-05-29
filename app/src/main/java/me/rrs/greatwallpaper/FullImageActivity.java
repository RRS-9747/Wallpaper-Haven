package me.rrs.greatwallpaper;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import java.io.OutputStream;

public class FullImageActivity extends AppCompatActivity {

    private String imageUrl;
    private StartAppAd startAppAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        ImageView fullImageView = findViewById(R.id.fullImageView);
        imageUrl = getIntent().getStringExtra("image_url");
        startAppAd = new StartAppAd(this);
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .override(1080, 1920)
                    .into(fullImageView);
        }
    }

    private void setupListeners() {
        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> showAdBeforeDownload());
    }

    private void showAdBeforeDownload() {
        startAppAd.loadAd(StartAppAd.AdMode.VIDEO, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override
                    public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                    }

                    @Override
                    public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                    }

                    @Override
                    public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                    }

                    @Override
                    public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                    }
                });
            }

            @Override
            public void onFailedToReceiveAd(com.startapp.sdk.adsbase.Ad ad) {
                downloadImage();
            }
        });
        downloadImage();
    }

    private void downloadImage() {
        Picasso.get().load(imageUrl).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                saveImageToGallery(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                showToast("Failed to download image");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // No specific actions needed here
            }
        });
    }

    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = "wallpaper_" + System.currentTimeMillis() + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream outputStream = imageUri != null ? getContentResolver().openOutputStream(imageUri) : null) {
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                showToast("Image saved to gallery");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to save image");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
