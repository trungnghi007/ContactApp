package com.example.contactapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.contactapp.R;
import com.example.contactapp.adapter.ContactAdapter;
import com.example.contactapp.database.ContactDatabaseHelper;
import com.example.contactapp.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener, ContactAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private FloatingActionButton fabAdd;
    private ContactDatabaseHelper databaseHelper;
    private List<Contact> contactList;
    private SearchView searchView;
    private Spinner categorySpinner;
    private String currentCategory = "Tất cả";

    // Khởi tạo
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        databaseHelper = new ContactDatabaseHelper(this);
        contactList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Object> list = new ArrayList<>();
        adapter = new ContactAdapter(this, list ,this, this);
        recyclerView.setAdapter(adapter);
        loadContacts();
        setupCategorySpinner();

        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddContactActivity.class)));
    }


    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        categorySpinner = findViewById(R.id.categorySpinner);


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts(); // Tải lại danh sách liên hệ và làm mới giao diện
        setupCategorySpinner(); // Đảm bảo danh sách các category được cập nhật
    }


    private void loadContacts() {
        contactList = databaseHelper.getAllContacts(); // Lấy danh sách liên hệ mới nhất từ DB
        updateContactList(currentCategory); // Cập nhật giao diện với category hiện tại
    }

    // Lọc danh sách liên hệ dựa trên category được chọn
    private void updateContactList(String category) {
        List<Object> filteredContacts = new ArrayList<>();
        Map<String, List<Contact>> groupedContacts = new TreeMap<>();
        List<Contact> favoriteContacts = new ArrayList<>();

        if(category.equals("Tất cả")){
            for (Contact contact : contactList) {
                if(contact.isFavorite()){
                    favoriteContacts.add(contact);
                }
                String firstChar = contact.getName().substring(0, 1).toUpperCase();
                if (!groupedContacts.containsKey(firstChar)) {
                    groupedContacts.put(firstChar, new ArrayList<>());
                }
                groupedContacts.get(firstChar).add(contact);
            }
            if(favoriteContacts.size() > 0){
                filteredContacts.add("Yêu thích");
                filteredContacts.addAll(favoriteContacts);
            }

            for (Map.Entry<String, List<Contact>> entry : groupedContacts.entrySet()) {
                if (!entry.getKey().equals("Yêu thích")) {
                    filteredContacts.add(entry.getKey());
                    filteredContacts.addAll(entry.getValue());
                }

            }
        } else {
            for (Contact contact : contactList) {
                if (contact.getCategory() != null && contact.getCategory().equals(category)){
                    if(contact.isFavorite()){
                        favoriteContacts.add(contact);
                    }
                    String firstChar = contact.getName().substring(0, 1).toUpperCase();
                    if (!groupedContacts.containsKey(firstChar)) {
                        groupedContacts.put(firstChar, new ArrayList<>());
                    }
                    groupedContacts.get(firstChar).add(contact);
                }
            }
            if(favoriteContacts.size() > 0){
                filteredContacts.add("Yêu thích");
                filteredContacts.addAll(favoriteContacts);
            }
            for (Map.Entry<String, List<Contact>> entry : groupedContacts.entrySet()) {
                if (!entry.getKey().equals("Yêu thích")) {
                    filteredContacts.add(entry.getKey());
                    filteredContacts.addAll(entry.getValue());
                }

            }

        }


        adapter.setContactList(filteredContacts);
        adapter.setCurrentCategory(category);
    }

    // Thiết lập Spinner để chọn danh mục.
    private void setupCategorySpinner() {
        Set<String> categories = new HashSet<>();
        categories.add("Tất cả");
        for (Contact contact : contactList) {
            if (contact.getCategory() != null && !contact.getCategory().isEmpty()) {
                categories.add(contact.getCategory());
            }

        }
        List<String> categoryList = new ArrayList<>(categories);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        int defaultPosition = categoryList.indexOf("Tất cả");
        categorySpinner.setSelection(defaultPosition);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                updateContactList(currentCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    // Khởi tạo menu với item tìm kiếm.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // Thiết lập SearchView để lọc danh sách liên hệ dựa trên từ khóa người dùng nhập.
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
        Log.d("MainActivity","Click contact - id: "+contact.getId() +", name: "+ contact.getName());
        intent.putExtra("contact_id", contact.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Contact contact, int position) {
        boolean isFavorite = !contact.isFavorite();
        databaseHelper.updateFavoriteStatus(contact.getId(), isFavorite);
        contact.setFavorite(isFavorite);
        adapter.updateItem(contact, position);
        loadContacts();

    }
}