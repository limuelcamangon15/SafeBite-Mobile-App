package com.project.safebite.ui.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.project.safebite.R;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private TextView tvContent;
    private MaterialButton btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms_and_conditions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityTermsAndConditions), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvContent = findViewById(R.id.tvContent);
        btnAgree = findViewById(R.id.btnAgree);

        btnAgree.setOnClickListener(v -> {finish();});

        tvContent.setText("Welcome to SafeBite! \n\n"+"SafeBite is developed by Concurrent and provides information about food products, allergens, and ingredients using Open Food Facts API.\n\n" +
                "By using SafeBite, you agree to share your food experiences and understand that all information provided is based on product data and community contributions. SafeBite and Concurrent are not responsible for any allergic reactions or inaccuracies.\n\n" +
                "You may scan barcodes, search products, and share your food experiences in the community. Always verify allergen information on the product packaging.\n\n" +
                "All content you contribute may be shared publicly to help other users make informed food choices. You agree not to post offensive, harmful, or illegal content.\n\n" +
                "SafeBite respects your privacy. Data collected will be used solely for improving the app experience.\n\n" +
                "By using the app, you agree to these Terms & Conditions. Concurrent reserves the right to update these terms at any time. Continued use indicates acceptance of the updated terms.\n\n" +
                "Thank you for using SafeBite!");

        // Optional: you can set action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Terms & Conditions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}