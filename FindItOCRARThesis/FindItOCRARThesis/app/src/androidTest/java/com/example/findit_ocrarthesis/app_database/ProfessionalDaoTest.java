package com.example.findit_ocrarthesis.app_database;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.services.storage.internal.TestStorageUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

@RunWith(AndroidJUnit4.class)
public class ProfessionalDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private ProfessionalDatabase database;
    private ProfessionalDao dao;

    @Before
    public void createDb(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
                ProfessionalDatabase.class
        ).allowMainThreadQueries().build();
        dao = database.professionalDao();
    }

    @After
    public void closeDb() throws IOException{
        database.close();
    }

    @Test
    public void insert() throws Exception {
        Professional professional = new Professional(1, "Νίκος", "Σπυρόπουλος", "Εξέταση διπλωματικής εργασίας",
                "Φοιτητής", "Κωνσταντινουπόλεως 398, Άγιοι Ανάργυροι, 13562, ΑΤΤΙΚΗΣ", "2104513561",
                "6977020385", "nikspyropoylos@gmail.com", "https://github.com/NikosSpyropoulos");
        Professional professional1 = new Professional(2, "Δημήτρης", "Σπυρόπουλος", "Εμπόριο αλουμινίων", "Έμπορος",
                "Ηρώων Πολυτεχνείου 38, Αχαρνές, 13786, ΑΤΤΙΚΗΣ", "2104246425", "6986115896",
                "dim1sp@gmail.com", "https://aluminia.gr");
        dao.insert(professional);
        dao.insert(professional1);
       LiveData<List<Professional>> addresses = dao.getAll();
        assertFalse(addresses.getValue().isEmpty() );
    }

}