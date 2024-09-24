package com.example.login_firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button btnLogout;
    Button btnupload;
    TextView textView;
    FirebaseUser user;
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;
    private static final int REQUEST_CAMERA_PERMISSION = 300;

    private Uri imageUri; // Store the image URI for upload
    private FirebaseStorage storage;
    StorageReference storageReference;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        btnupload = findViewById(R.id.upload);
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText(user.getEmail());
        }

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        Button buttonCamera = findViewById(R.id.button_camera);
        Button buttonGallery = findViewById(R.id.button_gallery);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        imageView = findViewById(R.id.image_view);

        buttonCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });

        buttonGallery.setOnClickListener(v -> openGallery());
        btnupload.setOnClickListener(v -> uploadImage());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(bitmap);
                imageUri = getImageUri(bitmap);

            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                imageView.setImageURI(selectedImageUri);

                // Store the URI for upload
                imageUri = selectedImageUri;
            }
        }
    }
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

            UploadTask uploadTask = imagesRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Handle successful upload
                Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Handle unsuccessful upload
                Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}