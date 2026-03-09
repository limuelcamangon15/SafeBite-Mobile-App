package com.project.safebite.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.project.safebite.R;
import com.project.safebite.utils.UIUtil;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    View registerView;
    Context context = RegisterActivity.this;
    TextView tvLogIn;
    MaterialButton btnSignUp;
    ProgressBar pbSignUp;
    TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityRegister), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
    }

    private void initializeViews(){
        auth = FirebaseAuth.getInstance();

        registerView  = findViewById(R.id.activityRegister);

        tvLogIn = findViewById(R.id.tvLogIn);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        pbSignUp = findViewById(R.id.pbSignUp);

        //text input layouts
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        setRealtimeEditTextListener();

        tvLogIn.setOnClickListener(v -> {
           finish();
        });

        btnSignUp.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email = etEmail.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if(fullName.isEmpty() && email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty()){
            tilFullName.setError("Full Name is required.");
            tilEmail.setError("Email is required.");
            tilPassword.setError("Password is required.");
            tilConfirmPassword.setError("Re-enter your password.");

            return;
        }

        if(fullName.isEmpty()){
            tilFullName.setError("Full Name is required.");
            return;
        }

        if(email.isEmpty()){
            tilEmail.setError("Email is required.");
            return;
        }

        if(password.isEmpty()){
            tilPassword.setError("Password is required.");
            return;
        }

        if(confirmPassword.isEmpty()){
            tilConfirmPassword.setError("Re-enter your password.");
            return;
        }

        if(!password.equals(confirmPassword)){
            UIUtil.showSnackbar(registerView,"Passwords did not match.");
            return;
        }

        UIUtil.showLoading(context, btnSignUp, pbSignUp);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                UIUtil.hideLoading(context, btnSignUp, pbSignUp, "Sign Up");

                UIUtil.showSnackbar(registerView,"Registered Successfully!");

                Intent intent = new Intent(context, LoginActivity.class);
                //short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(intent);
                },1200);

            }
            else{
                UIUtil.showSnackbar(registerView,"Failed to register, please try again.");

                UIUtil.hideLoading(context, btnSignUp, pbSignUp, "Sign Up");
            }
        });
    }

    private void setRealtimeEditTextListener(){
        //FullName
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (input.isEmpty()) {
                    tilFullName.setError("Full Name is required.");
                }
                else {
                    tilFullName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Email
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (input.isEmpty()) {
                    tilEmail.setError("Email is required.");
                }
                else if (!input.contains("@")) {
                    tilEmail.setError("Invalid email format.");
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
                    tilPassword.setError("Password is required.");
                }
                else if (input.length() < 8) {
                    tilPassword.setError("Password must be at least 8 characters long.");
                }
                else {
                    tilPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //Confirm Password
        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (input.isEmpty()) {
                    tilConfirmPassword.setError("Re-enter your password.");
                }
                else if (input.length() < 8) {
                    tilConfirmPassword.setError("Password must be at least 8 characters long.");
                }
                else {
                    tilConfirmPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}