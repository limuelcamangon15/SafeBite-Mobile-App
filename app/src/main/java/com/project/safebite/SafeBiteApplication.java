package com.project.safebite;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.constants.DatabaseConstants;

public class SafeBiteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .setPersistenceEnabled(true);
    }
}