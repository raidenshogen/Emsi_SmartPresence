package com.example.emsismartpresence;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DocumentsActivity extends AppCompatActivity {
    private Button selectFileButton;
    private Button uploadButton;
    private TextView selectedFileText;
    private ProgressBar uploadProgressBar;
    private Uri selectedFileUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    assert selectedFileUri != null;
                    String fileName = getFileNameFromUri(selectedFileUri);
                    selectedFileText.setText("Selected file: " + fileName);
                    uploadButton.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        // Initialize Firebase components
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        selectFileButton = findViewById(R.id.selectFileButton);
        uploadButton = findViewById(R.id.uploadButton);
        selectedFileText = findViewById(R.id.selectedFileText);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        // Initially disable upload button
        uploadButton.setEnabled(false);

        selectFileButton.setOnClickListener(v -> openFilePicker());
        uploadButton.setOnClickListener(v -> uploadFile());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"));
    }

    @SuppressLint("SetTextI18n")
    private void uploadFile() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to upload documents", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        uploadProgressBar.setVisibility(View.VISIBLE);
        uploadButton.setEnabled(false);

        // Create a unique file name
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = getFileNameFromUri(selectedFileUri);
        String uniqueFileName = auth.getCurrentUser().getUid() + "/" + timestamp + "_" + fileName;

        // Create a reference to the file location
        StorageReference fileRef = storageRef.child("documents/" + uniqueFileName);

        // Upload file
        UploadTask uploadTask = fileRef.putFile(selectedFileUri);

        // Monitor upload progress
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            uploadProgressBar.setProgress((int) progress);
        }).addOnSuccessListener(taskSnapshot -> {
            // Handle successful upload
            uploadProgressBar.setVisibility(View.GONE);
            selectedFileText.setText("No file selected");
            uploadButton.setEnabled(false);
            Toast.makeText(DocumentsActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle failed upload
            uploadProgressBar.setVisibility(View.GONE);
            uploadButton.setEnabled(true);
            Toast.makeText(DocumentsActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}