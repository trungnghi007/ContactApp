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

public class AddContactActivity extends AppCompatActivity {

    private EditText etName, etPhoneNumber, etEmail, etAddress, etBirthday, etCategory;
    private ImageView ivProfile;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSION = 2;
    private Uri imageUri;
    private ContactDatabaseHelper databaseHelper;
    private  String profilePictureUri ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        initView();
        databaseHelper = new ContactDatabaseHelper(this);

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

    // Mở trình chọn ảnh
    private void openImageChooser() {
        // Kiểm tra phiên bản hệ điều hành để xử lý cấp quyền phù hợp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Yêu cầu quyền đọc ảnh cho các thiết bị Android mới
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            } else {
                launchImagePickerIntent();
            }
        } else {
            // Yêu cầu quyền đọc bộ nhớ ngoài cho các thiết bị
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            } else {
                launchImagePickerIntent();
            }
        }
    }

    // Khởi chạy intent chọn ảnh từ thư viện
    private void launchImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Xử lý khi người dùng cấp hoặc từ chối quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Nếu quyền được cấp, tiếp tục chọn ảnh
                launchImagePickerIntent();
            } else {
                // Hiển thị thông báo nếu người dùng từ chối cấp quyền
                Toast.makeText(this, "Permission denied to access your external storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Phương thức xử lý khi chọn ảnh xong
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData(); // Lấy URI ảnh đã chọn
            Log.d("AddContactActivity", "Selected image URI from activity result: " + selectedImageUri);
            try {
                // Tạo bản sao ảnh vào bộ nhớ cache
                String filename =  "profile_"+System.currentTimeMillis()+".jpg";
                imageUri = Utils.copyUriToCache(this, selectedImageUri, filename);
                Log.d("AddContactActivity", "Copied image URI after copyUriToCache: " + imageUri);
                // Hiển thị ảnh trên ImageView
                ivProfile.setImageBitmap(Utils.uriToBitmap(this, imageUri));
            } catch (IOException e) {
                Log.e("AddContactActivity", "Error loading image", e);
                e.printStackTrace();
            }
        }
    }

    // Lưu thông tin liên hệ
    public void saveContact(View view) {
        String name = etName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        // Lưu URI ảnh đại diện nếu có
        if (imageUri != null){
            profilePictureUri = imageUri.toString();
            Log.d("AddContactActivity", "Saving profile picture URI: " + profilePictureUri);
        } else {
            Log.d("AddContactActivity", "profile picture URI is empty");
        }


        if (name.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Tên và số điện thoại không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setAddress(address);
        contact.setBirthday(birthday);
        contact.setProfilePictureUri(profilePictureUri);
        contact.setCategory(category);

        long id = databaseHelper.addContact(contact);
        Log.d("AddContactActivity", "Contact added with id: " + id); // Thêm log id
        if (id > 0) {
            Toast.makeText(this, "Thêm liên hệ thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi thêm liên hệ", Toast.LENGTH_SHORT).show();
        }

    }

    public void cancelAdd(View view) {
        finish();
    }
}