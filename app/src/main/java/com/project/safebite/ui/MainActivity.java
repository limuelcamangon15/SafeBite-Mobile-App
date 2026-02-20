package com.project.safebite.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.safebite.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //transform animation for nav (lift active)

//        bottomNav.setOnItemSelectedListener(item -> {
//
//            View view = bottomNav.findViewById(item.getItemId());
//
//            view.animate()
//                    .scaleX(1.3f)
//                    .scaleY(1.3f)
//                    .translationY(-10f)
//                    .setDuration(200)
//                    .start();
//
//            return true;
//        });


        // then reset others
//        for (int i = 0; i < bottomNav.getChildCount(); i++) {
//            View child = bottomNav.getChildAt(i);
//            child.setScaleX(1f);
//            child.setScaleY(1f);
//            child.setTranslationY(0f);
//        }
    }
}