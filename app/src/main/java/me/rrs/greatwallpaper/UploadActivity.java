package me.rrs.greatwallpaper;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
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

public class UploadActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private TextView selectedFileTextView;
    private Uri selectedFileUri;
    private Dialog progressDialog;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private final List<String> categoryList = new ArrayList<>();

    private final ActivityResultLauncher<String> fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    selectedFileTextView.setText(getString(R.string.selected_file, getFileName(selectedFileUri)));
                }
            }
    );

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
        progressDialog = createProgressDialog();
    }

    private void setupListeners() {
        findViewById(R.id.selectFileButton).setOnClickListener(v -> openFileChooser());
        findViewById(R.id.uploadButton).setOnClickListener(v -> uploadFile());
    }

    private void fetchCategoryList() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference().listAll()
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
        String result;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(columnIndex);
                    return result != null ? result : getFallbackFileName(uri);
                }
            }
        }
        return getFallbackFileName(uri);
    }

    private String getFallbackFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int cut = path.lastIndexOf('/');
            return (cut != -1) ? path.substring(cut + 1) : path;
        }
        return "unknown_file";
    }

    private void openFileChooser() {
        fileChooserLauncher.launch("image/*");
    }

    private void uploadFile() {
        if (selectedFileUri == null) {
            showToast(getString(R.string.no_file_selected));
            return;
        }

        progressDialog.show();

        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String destinationPath = selectedCategory + "/" + selectedFileUri.getLastPathSegment();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference fileRef = storage.getReference().child(destinationPath);

        fileRef.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    showToast(getString(R.string.upload_successful));
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showToast(getString(R.string.upload_failed, e.getMessage()));
                })
                .addOnProgressListener(taskSnapshot -> {
                    // Calculate the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    updateProgressDialog(progress);
                });
    }

    private Dialog createProgressDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        progressBar = dialog.findViewById(R.id.progressBar);
        progressTextView = dialog.findViewById(R.id.progressTextView);
        return dialog;
    }


    private void updateProgressDialog(double progress) {
        progressBar.setProgress((int) progress);
        progressTextView.setText(String.format("%.0f%%", progress));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
