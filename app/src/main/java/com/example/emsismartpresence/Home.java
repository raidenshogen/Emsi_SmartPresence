package com.example.emsismartpresence;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 100;
    private static final int REQUEST_READ_MEDIA_IMAGES = 101;

    // Views
    private LinearLayout markAttendanceCard, locationCard, documentsCard,
            scheduleCard, makeupClassCard, aiAssistantCard;
    private TextView professorName, greetingText;
    private CircleImageView profileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    // Image Picker Launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth and Storage
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        initializeViews();

        // Load user data and profile image
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadUserData();
            loadProfileImage(user.getUid());
        }

        // Initialize image picker launcher
        setupImagePickerLauncher();

        // Set click listeners
        setupClickListeners();
    }

    /**
     * Setup the image picker launcher to handle image selection.
     */
    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });
    }

    /**
     * Load the profile image from Firebase Storage.
     *
     * @param userId The unique identifier of the user.
     */
    private void loadProfileImage(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("FirebaseStorage", "User ID is null or empty");
            profileImage.setImageResource(R.drawable.account); // Default placeholder
            return;
        }

        // Try loading .jpg first, then .png
        StorageReference jpgRef = storageReference.child(userId + ".jpg");
        jpgRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.account)
                            .error(R.drawable.account)
                            .into(profileImage);
                })
                .addOnFailureListener(e -> {
                    StorageReference pngRef = storageReference.child(userId + ".png");
                    pngRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Glide.with(this)
                                        .load(uri)
                                        .placeholder(R.drawable.account)
                                        .error(R.drawable.account)
                                        .into(profileImage);
                            })
                            .addOnFailureListener(e2 -> {
                                // No image found, show placeholder
                                profileImage.setImageResource(R.drawable.account);
                            });
                });
    }

    /**
     * Upload the selected image to Firebase Storage with correct file extension.
     *
     * @param imageUri The URI of the selected image.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading toast
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        String userId = user.getUid();
        String fileExtension = getFileExtension(imageUri);

        if (fileExtension == null) {
            Toast.makeText(this, "Unsupported file format", Toast.LENGTH_LONG).show();
            return;
        }

        // Reference to store the image in Firebase Storage
        StorageReference fileRef = storageReference.child(userId + "." + fileExtension);

        // Upload the file
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("FirebaseStorage", "Upload successful");
                    // Get the download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("FirebaseStorage", "Got download URL: " + uri.toString());
                        // Update UI with the new image
                        Glide.with(this)
                                .load(uri)
                                .placeholder(R.drawable.account)
                                .error(R.drawable.account)
                                .into(profileImage);
                        Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.e("FirebaseStorage", "Error getting download URL", e);
                        Toast.makeText(this, "Error getting image URL: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Upload failed", e);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Show upload progress
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d("FirebaseStorage", "Upload is " + progress + "% done");
                });
    }

    /**
     * Determine the file extension based on the MIME type.
     *
     * @param imageUri The URI of the selected image.
     * @return The file extension (e.g., "jpg", "png") or null if unsupported.
     */
    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mime = contentResolver.getType(imageUri);
        if (mime != null) {
            return mimeTypeMap.getExtensionFromMimeType(mime);
        }
        return null;
    }

    /**
     * Check permissions and open the image picker.
     */
    private void checkPermissionAndOpenImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_READ_MEDIA_IMAGES);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }

    /**
     * Open the image picker to select an image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == REQUEST_READ_STORAGE || requestCode == REQUEST_READ_MEDIA_IMAGES)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission needed to select images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initialize all views in the layout.
     */
    private void initializeViews() {
        markAttendanceCard = findViewById(R.id.attendanceButton);
        locationCard = findViewById(R.id.locationButton);
        documentsCard = findViewById(R.id.documentsButton);
        scheduleCard = findViewById(R.id.scheduleButton);
        makeupClassCard = findViewById(R.id.makeupButton);
        aiAssistantCard = findViewById(R.id.aiAssistantButton);
        professorName = findViewById(R.id.professorName);
        greetingText = findViewById(R.id.greetingText);
        profileImage = findViewById(R.id.profileImage);

        profileImage.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
    }

    /**
     * Set click listeners for all cards.
     */
    private void setupClickListeners() {
        markAttendanceCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, AttendanceDashboard.class);
            startActivity(intent);
        });

        locationCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, MapsFragment.class);
            startActivity(intent);
        });

        documentsCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, DocumentsActivity.class);
            startActivity(intent);
        });

        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Schedule.class);
            startActivity(intent);
        });

        makeupClassCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, MakeupClass.class);
            startActivity(intent);
        });

        aiAssistantCard.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, AIAssistant.class);
            startActivity(intent);
        });
    }


    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Set greeting based on time of day
        greetingText.setText(getGreeting());

        // Load user's full name from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("user_name")) {
                        String username = documentSnapshot.getString("user_name");
                        if (username != null && !username.isEmpty()) {
                            professorName.setText("Hi"+" "+username.toUpperCase());
                            return;
                        }
                    }
                    String displayName = user.getEmail();
                    if (displayName != null && displayName.contains("@")) {
                        displayName = displayName.substring(0, displayName.indexOf("@"));
                    }
                    professorName.setText(displayName != null ? displayName : "User");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading user data", e);
                    String displayName = user.getEmail();
                    if (displayName != null && displayName.contains("@")) {
                        displayName = displayName.substring(0, displayName.indexOf("@"));
                    }
                    professorName.setText(displayName != null ? displayName : "User");
                });
    }

    /**
     * Get a greeting message based on the current time.
     *
     *
     */
    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good Afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }
}