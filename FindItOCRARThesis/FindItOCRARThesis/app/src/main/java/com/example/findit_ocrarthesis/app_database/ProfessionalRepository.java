package com.example.findit_ocrarthesis.app_database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ProfessionalRepository {
    private ProfessionalDao professionalDao;
    private LiveData<List<Professional>> allProfessionals;

    public ProfessionalRepository(Application application) {
        ProfessionalDatabase db = ProfessionalDatabase.getDatabase(application);
        professionalDao = db.professionalDao();
        allProfessionals = professionalDao.getAll();

    }

    public LiveData<List<Professional>> getAllData() { return allProfessionals; }

    public void insert(Professional professional){
        ProfessionalDatabase.databaseWriteExecutor.execute(() -> {
            professionalDao.insert(professional);
        });
    }

    public void deleteAll(){
        ProfessionalDatabase.databaseWriteExecutor.execute(() -> {
            professionalDao.deleteAll();
        });
    }

    public LiveData<List<String>> getAllAddresses(String name){
        return professionalDao.getAllAddresses(name);
    }

    public LiveData<Professional> findAll(String office_address, String name){
        return professionalDao.findAll(office_address, name);
    }

    public LiveData<String> findDescription(String office_address, String name){
        return professionalDao.findDescription(office_address, name);
    }

    public LiveData<String> findCategory(String office_address, String name){
        return professionalDao.findCategory(office_address, name);
    }

    public LiveData<String> findOfficePhone(String office_address, String name){
        return professionalDao.findOfficePhone(office_address, name);
    }

    public LiveData<String> findEmail(String office_address, String name){
        return professionalDao.findEmail(office_address, name);
    }

    public LiveData<String> findMobilePhone(String office_address, String name){
        return professionalDao.findMobilePhone(office_address, name);
    }

    public LiveData<String> findWebsite(String office_address, String name){
        return professionalDao.findWebsite(office_address, name);
    }

}
