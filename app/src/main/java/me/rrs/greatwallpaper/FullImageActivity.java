package me.rrs.greatwallpaper;

import android.app.WallpaperManager;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.transition.Transition;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import java.io.IOException;
import java.io.OutputStream;

public class FullImageActivity extends AppCompatActivity {

    private String imageUrl;
    private StartAppAd startAppAd;
    private ImageView fullImageView;
    private Button downloadButton;
    private Button setWallpaperButton; // Add reference for the new button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        initializeViews();
        loadImage();
        setupListeners();
    }

    private void initializeViews() {
        fullImageView = findViewById(R.id.fullImageView);
        downloadButton = findViewById(R.id.downloadButton);
        setWallpaperButton = findViewById(R.id.setWallpaperButton); // Initialize the new button
        startAppAd = new StartAppAd(this);
        imageUrl = getIntent().getStringExtra("image_url");
    }

    private void loadImage() {
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder) // Placeholder image
                    .override(1080, 1920)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Caches both original & resized images
                    .into(fullImageView);
        } else {
            showToast("Image URL is not available.");
        }
    }

    private void setupListeners() {
        downloadButton.setOnClickListener(v -> showAdBeforeDownload());
        setWallpaperButton.setOnClickListener(v -> setWallpaper(imageUrl)); // Set listener for the new button
    }

    private void showAdBeforeDownload() {
        startAppAd.loadAd(StartAppAd.AdMode.VIDEO, new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                startAppAd.showAd(new AdDisplayListener() {
                    @Override
                    public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                        downloadImage();
                    }

                    @Override
                    public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                        // Optional: Add logic if needed when ad is displayed
                    }

                    @Override
                    public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                        // Optional: Handle ad click
                    }

                    @Override
                    public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                        downloadImage(); // Fallback to download if ad not displayed
                    }
                });
            }

            @Override
            public void onFailedToReceiveAd(com.startapp.sdk.adsbase.Ad ad) {
                downloadImage(); // Directly download if ad loading fails
            }
        });
        // Start loading the ad
        startAppAd.loadAd(StartAppAd.AdMode.VIDEO);
    }

    private void downloadImage() {
        if (imageUrl == null) {
            showToast("Image URL is not available.");
            return;
        }

        Picasso.get().load(imageUrl).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                saveImageToGallery(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                showToast("Failed to download image: " + e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // No specific actions needed here
            }
        });
    }

    private void setWallpaper(String imageUrl) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                        try {
                            wallpaperManager.setBitmap(resource);
                            showToast("Wallpaper set successfully!");
                        } catch (IOException e) {
                            showToast("Error setting wallpaper: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle cleanup if needed
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
                showToast("Image saved to gallery");
            } else {
                showToast("Failed to save image: output stream is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Failed to save image: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}