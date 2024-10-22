package me.rrs.greatwallpaper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadActivity extends AppCompatActivity implements WallpaperAdapter.OnItemClickListener {

    private Spinner categorySpinner;
    private final List<String> categoryList = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private final Set<String> wallpaperUrls = new HashSet<>(); // Use Set to avoid duplicates
    private WallpaperAdapter wallpaperAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);

        initViews();
        setupListeners();

        fetchCategoryList();
        fetchWallpapers("All");
    }

    private void initViews() {
        categorySpinner = findViewById(R.id.categorySpinner);
        RecyclerView wallpaperRecyclerView = findViewById(R.id.wallpaperRecyclerView);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        wallpaperAdapter = new WallpaperAdapter(new ArrayList<>(wallpaperUrls), this);
        wallpaperRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        wallpaperRecyclerView.setAdapter(wallpaperAdapter);

        categoryList.add("All");
        categoryAdapter.notifyDataSetChanged();
        categorySpinner.setSelection(0);
    }

    private void setupListeners() {
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = categoryList.get(position);
                fetchWallpapers(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        findViewById(R.id.showWallpapersButton).setOnClickListener(v -> {
            String selectedCategory = categorySpinner.getSelectedItem().toString();
            fetchWallpapers(selectedCategory);
        });
    }

    private void fetchCategoryList() {
        FirebaseStorage.getInstance().getReference().listAll()
                .addOnSuccessListener(listResult -> {
                    categoryList.clear();
                    categoryList.add("All");
                    for (StorageReference prefix : listResult.getPrefixes()) {
                        categoryList.add(prefix.getName());
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast("Failed to load categories from Firebase: " + e.getMessage()));
    }

    private void fetchWallpapers(String category) {
        if (category == null || category.isEmpty()) {
            showToast("Category cannot be null or empty.");
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        wallpaperUrls.clear();

        if (category.equals("All")) {
            fetchAllWallpapers(storageRef);
        } else {
            fetchCategoryWallpapers(storageRef.child(category));
        }
    }

    private void fetchAllWallpapers(StorageReference storageRef) {
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Task<ListResult>> tasks = new ArrayList<>();
                    for (StorageReference prefix : listResult.getPrefixes()) {
                        tasks.add(prefix.listAll());
                    }
                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                for (Object obj : results) {
                                    ListResult result = (ListResult) obj;
                                    for (StorageReference item : result.getItems()) {
                                        item.getDownloadUrl()
                                                .addOnSuccessListener(uri -> wallpaperUrls.add(uri.toString())) // Add to Set
                                                .addOnFailureListener(e -> showToast("Failed to load wallpaper: " + e.getMessage()));
                                    }
                                }
                                updateWallpapers(); // Update after all URLs are fetched
                            })
                            .addOnFailureListener(e -> showToast("Failed to load wallpapers: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showToast("Failed to load wallpapers: " + e.getMessage()));
    }

    private void fetchCategoryWallpapers(StorageReference categoryRef) {
        categoryRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<Task<Uri>> tasks = new ArrayList<>();
                    for (StorageReference item : listResult.getItems()) {
                        tasks.add(item.getDownloadUrl());
                    }
                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(downloadUrls -> {
                                for (Object obj : downloadUrls) {
                                    wallpaperUrls.add(obj.toString()); // Add to Set
                                }
                                updateWallpapers(); // Update after all URLs are fetched
                            })
                            .addOnFailureListener(e -> showToast("Failed to load wallpapers: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showToast("Failed to load wallpapers: " + e.getMessage()));
    }

    private void updateWallpapers() {
        wallpaperAdapter.updateWallpapers(new ArrayList<>(wallpaperUrls)); // Convert Set to List
    }

    @Override
    public void onItemClick(String imageUrl) {
        Intent intent = new Intent(this, FullImageActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
