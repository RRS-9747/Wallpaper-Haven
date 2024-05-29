package me.rrs.greatwallpaper;

import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private TextView selectedFileTextView;
    private Uri selectedFileUri;
    private AlertDialog progressDialog;
    private final List<String> categoryList = new ArrayList<>();

    private final ActivityResultLauncher<String> fileChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    selectedFileTextView.setText(getString(R.string.selected_file, getFileName(selectedFileUri)));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);

        initializeViews();
        setupListeners();
        fetchCategoryList();
    }

    private void initializeViews() {
        categorySpinner = findViewById(R.id.categorySpinner);
        selectedFileTextView = findViewById(R.id.selectedFileTextView);
        progressDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.uploading)
                .setCancelable(false)
                .create();
    }

    private void setupListeners() {
        Button selectFileButton = findViewById(R.id.selectFileButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        selectFileButton.setOnClickListener(v -> openFileChooser());
        uploadButton.setOnClickListener(v -> uploadFile());
    }

    private void fetchCategoryList() {
        FirebaseStorage.getInstance().getReference().listAll()
                .addOnSuccessListener(listResult -> {
                    categoryList.clear();
                    for (StorageReference prefix : listResult.getPrefixes()) {
                        categoryList.add(prefix.getName());
                    }
                    populateCategorySpinner();
                })
                .addOnFailureListener(e -> showToast(getString(R.string.failed_to_load_categories)));
    }

    private void populateCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void openFileChooser() {
        fileChooserLauncher.launch("image/*");
    }

    private void uploadFile() {
        if (selectedFileUri != null) {
            progressDialog.show();

            String selectedCategory = categorySpinner.getSelectedItem().toString();
            String destinationPath = selectedCategory + "/" + selectedFileUri.getLastPathSegment();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child(destinationPath);

            fileRef.putFile(selectedFileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        showToast(getString(R.string.upload_successful));
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        showToast(getString(R.string.upload_failed, e.getMessage()));
                    });
        } else {
            showToast(getString(R.string.no_file_selected));
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

