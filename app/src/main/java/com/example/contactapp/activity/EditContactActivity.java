package com.example.contactapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.contactapp.R;
import com.example.contactapp.database.ContactDatabaseHelper;
import com.example.contactapp.model.Contact;
import com.example.contactapp.utils.Utils;

import java.io.IOException;

public class EditContactActivity extends AppCompatActivity {

    private EditText etName, etPhoneNumber, etEmail, etAddress, etBirthday, etCategory;
    private ImageView ivProfile;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 2;
    private Uri imageUri;
    private ContactDatabaseHelper databaseHelper;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        initView();
        databaseHelper = new ContactDatabaseHelper(this);

        if (getIntent().hasExtra("contact")) {
            contact = getIntent().getParcelableExtra("contact");
            loadContactData();
        }
        ivProfile.setOnClickListener(v -> openImageChooser());
    }

    private void initView() {
        etName = findViewById(R.id.etName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etBirthday = findViewById(R.id.etBirthday);
        ivProfile = findViewById(R.id.ivProfile);
        etCategory = findViewById(R.id.etCategory);
    }
    private void loadContactData() {
        etName.setText(contact.getName());
        etPhoneNumber.setText(contact.getPhoneNumber());
        etEmail.setText(contact.getEmail());
        etAddress.setText(contact.getAddress());
        etBirthday.setText(contact.getBirthday());
        etCategory.setText(contact.getCategory());

        if (contact.getProfilePictureUri() != null && !contact.getProfilePictureUri().isEmpty()) {
            imageUri = Uri.parse(contact.getProfilePictureUri());
            Log.d("EditContactActivity", "Loading profile picture URI from contact: " + imageUri); // Thêm log
            try {
                ivProfile.setImageBitmap(Utils.uriToBitmap(this, imageUri));
            } catch (IOException e) {
                Log.e("EditContactActivity", "Error loading image", e); // Thêm log lỗi ở đây
                ivProfile.setImageResource(R.drawable.ic_person);
                e.printStackTrace();
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_person);
        }
    }

    private void openImageChooser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            } else {
                launchImagePickerIntent();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                launchImagePickerIntent();
            }
        }
    }

    private void launchImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePickerIntent();
            } else {
                Toast.makeText(this, "Permission denied to access your external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            Log.d("EditContactActivity", "Selected image URI from activity result: " + selectedImageUri);
            try {
                String filename = "profile_" + System.currentTimeMillis() + ".jpg";
                imageUri = Utils.copyUriToCache(this, selectedImageUri, filename);
                Log.d("EditContactActivity", "Copied image URI after copyUriToCache: " + imageUri);
                ivProfile.setImageBitmap(Utils.uriToBitmap(this, imageUri));
            } catch (IOException e) {
                Log.e("EditContactActivity", "Error loading image", e);
                Toast.makeText(this, "Lỗi copy ảnh", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void saveContact(View view) {
        String name = etName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String profilePictureUri;
        if (imageUri != null){
            profilePictureUri = imageUri.toString();
            Log.d("EditContactActivity", "Saving profile picture URI: " + profilePictureUri);
        } else {
            profilePictureUri = contact.getProfilePictureUri();
            Log.d("EditContactActivity", "Saving profile picture URI: " + profilePictureUri);
        }
        if (name.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Tên và số điện thoại không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setAddress(address);
        contact.setBirthday(birthday);
        contact.setProfilePictureUri(profilePictureUri);
        contact.setCategory(category);
        int rows = databaseHelper.updateContact(contact);

        if (rows > 0) {
            Toast.makeText(this, "Cập nhật liên hệ thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("contact_id", contact.getId());
            setResult(RESULT_OK,intent);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi cập nhật liên hệ", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelEdit(View view) {
        finish();
    }
}