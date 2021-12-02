package com.example.findit_ocrarthesis.app_database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProfessionalDao {


    @Query("SELECT * FROM professional_table")
    LiveData<List<Professional>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Professional user);

    @Delete
    void delete(Professional user);

    @Query("DELETE FROM professional_table")
    void deleteAll();


    @Query("select address from professional_table where surname like :name")
    LiveData<List<String>> getAllAddresses(String name);

    @Query("select description from professional_table where address like :office_address and surname like :name")
    LiveData<String> findDescription(String office_address, String name);

    @Query("select category from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<String> findCategory(String office_address, String name);

    @Query("select officePhone from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<String> findOfficePhone(String office_address, String name);

    @Query("select mobilePhone from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<String> findMobilePhone(String office_address, String name);

    @Query("select email from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<String> findEmail(String office_address, String name);

    @Query("select website from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<String> findWebsite(String office_address, String name);

    @Query("select * from professional_table WHERE address LIKE :office_address AND surname LIKE :name")
    LiveData<Professional> findAll(String office_address, String name);
}