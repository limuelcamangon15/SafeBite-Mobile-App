package com.project.safebite.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.offlineAuth.AuthStorage;
import com.project.safebite.utils.UIUtil;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Context context = LoginActivity.this;
    TextView tvSignUp;
    TextInputLayout tilEmail, tilPassword;
    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogIn;
    ProgressBar pbLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);



        AuthStorage authStorage = new AuthStorage(context);
        if(authStorage.isLoggedIn()){
            authStorage.refreshSession();
            Intent loggedIntent = new Intent(context, MainActivity.class);
            loggedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loggedIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
    }

    private void initializeViews(){
        auth = FirebaseAuth.getInstance();

        tvSignUp = findViewById(R.id.tvSignUp);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogIn = findViewById(R.id.btnLogIn);
        pbLogIn = findViewById(R.id.pbLogIn);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        setRealtimeEditTextListener();

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(context, RegisterActivity.class);

            startActivity(intent);
        });

        btnLogIn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(email.isEmpty() && password.isEmpty()){
            tilEmail.setError("Invalid email.");
            tilPassword.setError("Invalid password.");

            return;
        }

        if(email.isEmpty()){
            tilEmail.setError("Invalid email.");
            return;
        }

        if(password.isEmpty()){
            tilPassword.setError("Invalid password.");
            return;
        }

        UIUtil.showLoading(context, btnLogIn, pbLogIn);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){


                        FirebaseUser userLogged = task.getResult().getUser();
                        String uid = userLogged.getUid();
                        String path = "users/" + uid + "/fullName";
                        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = snapshot.getValue(String.class);
                                new AuthStorage(context).saveUser(task.getResult().getUser(), name);

                                //short delay
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    Intent mainIntent = new Intent(context, MainActivity.class);
                                    UIUtil.hideLoading(context, btnLogIn, pbLogIn, "Log In");
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                },1000);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                    }
                    else {
                        UIUtil.showSnackbar(findViewById(R.id.activityLogin),"Log in failed, please try again.");

                        UIUtil.hideLoading(context, btnLogIn, pbLogIn, "Log In");
                    }
                });
    }

    private void setRealtimeEditTextListener(){
        //Email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (input.isEmpty()) {
                    tilEmail.setError("Invalid email.");
                }
                else {
                    tilEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Password
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (input.isEmpty()) {
                    tilPassword.setError("Invalid password.");
                }
                else {
                    tilPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}