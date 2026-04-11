package com.project.safebite.offlineAuth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseUser;

public class AuthStorage {

    private static final String PREFS_NAME = "secure_auth";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_DISPLAY_NAME = "key_display_name";
    private static final String KEY_IS_LOGGED_IN = "key_is_logged_in";
    private static final String KEY_LOGIN_TIMESTAMP = "key_login_timestamp";
    private static final long SESSION_DURATION_MS =  7 * 24 * 60 * 60 * 1000L; //about 7 days

    private SharedPreferences sharedPreferences;


    public AuthStorage(Context context){
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveUser(FirebaseUser user, String displayName) {
        sharedPreferences.edit()
                .putString(KEY_USER_ID, user.getUid())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_DISPLAY_NAME, displayName)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    public boolean isLoggedIn() {
        boolean loggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!loggedIn) return false;
        if (isSessionExpired()) {
            clearUser();
            return false;
        }
        return true;
    }

    public void refreshSession() {
        sharedPreferences.edit()
                .putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                .apply();
    }

    private boolean isSessionExpired() {
        long loginTimestamp = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0);
        return System.currentTimeMillis() - loginTimestamp > SESSION_DURATION_MS;
    }

    public String getUserId() { return sharedPreferences.getString(KEY_USER_ID, null); }
    public String getEmail() { return sharedPreferences.getString(KEY_EMAIL, null); }
    public String getDisplayName() { return sharedPreferences.getString(KEY_DISPLAY_NAME, null); }

    public void clearUser() {
        sharedPreferences.edit().clear().apply();
    }
}
