package com.example.contactapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.contactapp.R;
import com.example.contactapp.database.ContactDatabaseHelper;
import com.example.contactapp.model.Contact;
import com.example.contactapp.utils.Utils;

import java.io.IOException;

public class ContactDetailActivity extends AppCompatActivity {

    private TextView tvName, tvPhoneNumber, tvEmail, tvAddress, tvBirthday, tvCategory;
    private ImageView ivProfile;
    private Contact contact;
    private ImageView btnCall, btnMessage, btnEdit, btnDelete, btnEmail;
    private ContactDatabaseHelper databaseHelper;

    private static final int EDIT_CONTACT_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        initView();
        databaseHelper = new ContactDatabaseHelper(this);

        // Kiểm tra Intent có chứa contact_id hay không
        if (getIntent().hasExtra("contact_id")) {
            int contactId = getIntent().getIntExtra("contact_id",-1);
            if(contactId != -1){
                loadContact(contactId); // Tải thông tin liên hệ từ cơ sở dữ liệu
            } else {
                Toast.makeText(this, "Không có thông tin liên hệ", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity nếu không có contact_id hợp lệ
            }
        } else {
            Toast.makeText(this, "Không có thông tin liên hệ", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không nhận được contact_id
        }
        setupClickListeners();
    }
    // Tải thông tin liên hệ từ cơ sở dữ liệu dựa trên contactId
    private void loadContact(int contactId){
        contact = databaseHelper.getAllContacts()
                .stream()
                .filter(c -> c.getId() == contactId)
                .findFirst()
                .orElse(null);
        if (contact != null){
            Log.d("ContactDetailActivity","onCreate - id: "+contact.getId() +", name: "+ contact.getName() + " after reload db" +", uri: "+contact.getProfilePictureUri());
            updateUI(); // Cập nhật giao diện nếu tìm thấy liên hệ
        } else {
            Toast.makeText(this,"Contact not found",Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không tìm thấy liên hệ
        }
    }


    private void initView() {
        tvName = findViewById(R.id.tvName);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.tvAddress);
        tvBirthday = findViewById(R.id.tvBirthday);
        ivProfile = findViewById(R.id.ivProfile);
        btnCall = findViewById(R.id.btnCall);
        btnMessage = findViewById(R.id.btnMessage);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        tvCategory = findViewById(R.id.tvCategory);
        btnEmail = findViewById(R.id.btnEmail);
    }

    // Cập nhật giao diện với thông tin của liên hệ
    private void updateUI() {
        if(contact == null){
            Log.e("ContactDetailActivity","contact is null when calling updateUI");
            return;
        }
        // Cập nhật các TextView với thông tin từ Contact
        tvName.setText(contact.getName());
        tvPhoneNumber.setText(contact.getPhoneNumber());
        tvEmail.setText(contact.getEmail());
        tvAddress.setText(contact.getAddress());
        tvBirthday.setText(contact.getBirthday());
        tvCategory.setText(contact.getCategory());
        // Cập nhật hình ảnh đại diện nếu có URI, ngược lại hiển thị ảnh mặc định
        if (contact.getProfilePictureUri() != null && !contact.getProfilePictureUri().isEmpty()) {
            Uri imageUri = Uri.parse(contact.getProfilePictureUri());
            try {
                ivProfile.setImageBitmap(Utils.uriToBitmap(this, imageUri));
            } catch (IOException e) {
                ivProfile.setImageResource(R.drawable.ic_person);
                Log.e("ContactDetailActivity","Error loading bitmap",e);
                e.printStackTrace();
            }
        } else {
            ivProfile.setImageResource(R.drawable.ic_person);
        }
    }

    // Thiết lập các sự kiện click cho các nút hành động
    private void setupClickListeners() {
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
            startActivity(intent); // Mở ứng dụng gọi điện
        });

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + contact.getPhoneNumber()));
            startActivity(intent); // Mở ứng dụng nhắn tin
        });
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            if(contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{contact.getEmail()});
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent); // Mở ứng dụng email
                } else {
                    Toast.makeText(this,"No email application found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this,"No email address", Toast.LENGTH_SHORT).show();
            }
        });
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditContactActivity.class);
            intent.putExtra("contact", contact); // Truyền đối tượng contact để chỉnh sửa
            startActivityForResult(intent, EDIT_CONTACT_REQUEST);
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }
    // Hiển thị hộp thoại xác nhận trước khi xóa liên hệ
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa liên hệ");
        builder.setMessage("Bạn có chắc chắn muốn xóa liên hệ này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> deleteContact());
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void deleteContact() {
        int rowsAffected = databaseHelper.deleteContact(contact.getId());
        if (rowsAffected > 0) {
            Toast.makeText(this, "Xóa liên hệ thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi xóa liên hệ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_CONTACT_REQUEST && resultCode == RESULT_OK) {
            if(data != null && data.hasExtra("contact_id")) {
                int contactId = data.getIntExtra("contact_id",-1);
                if(contactId != -1){
                    loadContact(contactId); // Tải lại liên hệ sau khi chỉnh sửa
                }
            }
        }
    }
}