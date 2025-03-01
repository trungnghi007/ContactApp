package com.example.contactapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.contactapp.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_BIRTHDAY = "birthday";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IS_FAVORITE = "is_favorite";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_CONTACTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_PHONE_NUMBER + " TEXT," +
                    COLUMN_EMAIL + " TEXT," +
                    COLUMN_ADDRESS + " TEXT," +
                    COLUMN_BIRTHDAY + " TEXT," +
                    COLUMN_PROFILE_PICTURE + " TEXT," +
                    COLUMN_CATEGORY + " TEXT," +
                    COLUMN_IS_FAVORITE + " INTEGER" +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_CONTACTS;


    public ContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public long addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE_NUMBER, contact.getPhoneNumber());
        values.put(COLUMN_EMAIL, contact.getEmail());
        values.put(COLUMN_ADDRESS, contact.getAddress());
        values.put(COLUMN_BIRTHDAY, contact.getBirthday());
        values.put(COLUMN_PROFILE_PICTURE, contact.getProfilePictureUri());
        values.put(COLUMN_CATEGORY, contact.getCategory());
        values.put(COLUMN_IS_FAVORITE, contact.isFavorite() ? 1 : 0);

        long id = db.insert(TABLE_CONTACTS, null, values);
        db.close();
        return id;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        //thay đổi câu query để sắp xếp danh sách contact theo name
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                contact.setBirthday(cursor.getString(cursor.getColumnIndex(COLUMN_BIRTHDAY)));
                contact.setProfilePictureUri(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_PICTURE)));
                contact.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                contact.setFavorite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVORITE)) == 1);
                Log.d("ContactDatabaseHelper", "getAllContacts - id: " + contact.getId() + ", name: " + contact.getName()+", uri: "+ contact.getProfilePictureUri());
                contactList.add(contact);
            } while (cursor.moveToNext());
        }


        cursor.close();
        db.close();
        return contactList;
    }
    // Tìm kiếm liên hệ theo tên hoặc số điện thoại dựa trên chuỗi tìm kiếm
    public List<Contact> searchContacts(String query) {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_NAME + " LIKE ? OR " + COLUMN_PHONE_NUMBER + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_CONTACTS, null, selection, selectionArgs, null, null, null);


        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contact.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)));
                contact.setBirthday(cursor.getString(cursor.getColumnIndex(COLUMN_BIRTHDAY)));
                contact.setProfilePictureUri(cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_PICTURE)));
                contact.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                contact.setFavorite(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FAVORITE)) == 1);


                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }


    public int updateContact(Contact contact) {
        Log.d("ContactDatabaseHelper", "Updating contact with ID: " + contact.getId() + ", profile picture URI: " + contact.getProfilePictureUri()); // Thêm log
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE_NUMBER, contact.getPhoneNumber());
        values.put(COLUMN_EMAIL, contact.getEmail());
        values.put(COLUMN_ADDRESS, contact.getAddress());
        values.put(COLUMN_BIRTHDAY, contact.getBirthday());
        values.put(COLUMN_PROFILE_PICTURE, contact.getProfilePictureUri());
        values.put(COLUMN_CATEGORY, contact.getCategory());
        values.put(COLUMN_IS_FAVORITE, contact.isFavorite() ? 1 : 0);


        int rows = db.update(TABLE_CONTACTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
        return rows;
    }

    public int deleteContact(int contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CONTACTS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(contactId)});
        db.close();
        return rows;
    }
    // Cập nhật trạng thái yêu thích của một liên hệ dựa trên ID
    public void updateFavoriteStatus(int contactId, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_CONTACTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(contactId)});
        db.close();
    }

}