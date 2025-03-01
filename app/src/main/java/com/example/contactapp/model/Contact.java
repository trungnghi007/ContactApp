package com.example.contactapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
    private int id;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String birthday;
    private String profilePictureUri;
    private String category;
    private boolean isFavorite;

    public Contact() {
    }

    public Contact(int id, String name, String phoneNumber, String email, String address,
                   String birthday, String profilePictureUri, String category, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.birthday = birthday;
        this.profilePictureUri = profilePictureUri;
        this.category = category;
        this.isFavorite = isFavorite;
    }


    protected Contact(Parcel in) {
        id = in.readInt();
        name = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        address = in.readString();
        birthday = in.readString();
        profilePictureUri = in.readString();
        category = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeString(birthday);
        dest.writeString(profilePictureUri);
        dest.writeString(category);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}