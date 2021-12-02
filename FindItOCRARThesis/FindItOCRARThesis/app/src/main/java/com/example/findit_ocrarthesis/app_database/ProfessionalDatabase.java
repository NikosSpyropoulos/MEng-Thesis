package com.example.findit_ocrarthesis.app_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Professional.class}, version = 1, exportSchema = false)
public abstract class ProfessionalDatabase extends RoomDatabase {
    public abstract ProfessionalDao professionalDao();

    private static volatile ProfessionalDatabase INSTANCE;

    private static final int NUM_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(NUM_OF_THREADS);

    public static ProfessionalDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (ProfessionalDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ProfessionalDatabase.class, "professionals_db1.db")
                            .createFromAsset("professionals_db1.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

