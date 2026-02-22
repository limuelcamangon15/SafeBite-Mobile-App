package com.project.safebite.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.project.safebite.R;
import com.project.safebite.utils.UIUtil;

public class RegisterActivity extends AppCompatActivity {

    Context context = RegisterActivity.this;
    TextView tvLogIn;
    MaterialButton btnSignUp;
    ProgressBar pbSignUp;
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
        tvLogIn = findViewById(R.id.tvLogIn);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        pbSignUp = findViewById(R.id.pbSignUp);

        tvLogIn.setOnClickListener(v -> {
           finish();
        });

        btnSignUp.setOnClickListener(v -> {
            UIUtil.showLoading(context, btnSignUp, pbSignUp);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    UIUtil.hideLoading(context, btnSignUp, pbSignUp, "Sign Up");
                    UIUtil.showSnackbar(findViewById(R.id.activityRegister),"Registered Successfully!");
            },3000);
        });
    }


}