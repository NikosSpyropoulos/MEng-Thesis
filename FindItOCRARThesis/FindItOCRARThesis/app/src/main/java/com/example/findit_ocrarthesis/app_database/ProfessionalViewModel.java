package com.example.findit_ocrarthesis.app_database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ProfessionalViewModel extends AndroidViewModel implements LifecycleObserver {

    public static ProfessionalRepository repository;
    public final LiveData<List<Professional>> allProfessionals;


    public ProfessionalViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfessionalRepository(application);
        allProfessionals = repository.getAllData();
    }

    public LiveData<List<Professional>> getAllProfessionals() { return allProfessionals; }

    public static void insert(Professional professional) { repository.insert(professional); }

    public void deleteAll(){
        repository.deleteAll();
    }

    public LiveData<List<String>> getAllAddresses(String name){
        return repository.getAllAddresses(name);
    }

    public LiveData<Professional> findAll(String office_address, String name){
        return repository.findAll(office_address, name);
    }

    public LiveData<String> findDescription(String office_address, String name){
        return repository.findDescription(office_address, name);
    }

    public LiveData<String> findCategory(String office_address, String name){
        return repository.findCategory(office_address, name);
    }

    public LiveData<String> findOfficePhone(String office_address, String name){
        return repository.findOfficePhone(office_address, name);
    }

    public LiveData<String> findEmail(String office_address, String name){
        return repository.findEmail(office_address, name);
    }

    public LiveData<String> findMobilePhone(String office_address, String name){
        return repository.findMobilePhone(office_address, name);
    }

    public LiveData<String> findWebsite(String office_address, String name){
        return repository.findWebsite(office_address, name);
    }

}
