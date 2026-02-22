package com.project.safebite.ui.activity;

import android.content.Context;
import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {

    Context context = LoginActivity.this;
    TextView tvSignUp;
    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogIn;
    ProgressBar pbLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
    }

    private void initializeViews(){
        tvSignUp = findViewById(R.id.tvSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        pbLogIn = findViewById(R.id.pbLogIn);

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(context, RegisterActivity.class);

            startActivity(intent);
        });

        btnLogIn.setOnClickListener(v -> {
            UIUtil.showLoading(context, btnLogIn, pbLogIn);
            UIUtil.showSnackbar(findViewById(R.id.activityLogin),"Logged In Successfully!");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                UIUtil.hideLoading(context, btnLogIn, pbLogIn, "Log In");

                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            },2000);
        });
    }
}