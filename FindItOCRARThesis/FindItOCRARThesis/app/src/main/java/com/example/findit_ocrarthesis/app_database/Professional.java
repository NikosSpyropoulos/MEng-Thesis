package com.example.findit_ocrarthesis.app_database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "professional_table")
public class Professional {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name = "profName")
    @NonNull
    private String profName;

    @ColumnInfo(name = "surname")
    @NonNull
    private String surname;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "address")
    @NonNull
    private String address;

    @ColumnInfo(name = "officePhone")
    private String officePhone;

    @ColumnInfo(name = "mobilePhone")
    private String mobilePhone;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "website")
    private String website;

    public Professional(int id, @NonNull String profName, @NonNull String surname, String description, String category, @NonNull String address, String officePhone, String mobilePhone, String email, String website) {
        this.id = id;
        this.profName = profName;
        this.surname = surname;
        this.description = description;
        this.category = category;
        this.address = address;
        this.officePhone = officePhone;
        this.mobilePhone = mobilePhone;
        this.email = email;
        this.website = website;

    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getProfName() {
        return profName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    @NonNull
    public String getSurname() {
        return surname;
    }

    public void setSurname(@NonNull String surname) {
        this.surname = surname;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProfName(@NonNull String profName) {
        this.profName = profName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAddress(@NonNull String address) {
        this.address = address;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}