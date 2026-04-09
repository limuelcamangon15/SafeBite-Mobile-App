package com.project.safebite.ui.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Post;
import com.project.safebite.utils.UIUtil;

import java.util.HashMap;
import java.util.Map;

public class PostFormActivity extends AppCompatActivity {

    MaterialButton emoji1, emoji2, emoji3, emoji4, btnSubmit, btnCancel;
    EditText etPostContent;
    TextView tvPostContentCount, tvFoodTitle, tvBrand, tvAllergens;
    ImageView ivFoodImage;
    String selectedEmoji = "";
    String postContent = "";
    String imageUrl = "";
    String name = "";
    String brand = "";
    String allergens = "";
    MaterialButton selectedBtn = null;
    Context context = PostFormActivity.this;
    private FirebaseAuth auth;
    int maxContentLength = 200;
    int postContentLength = 0;
    String fullName = "";
    View parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activityPostForm), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialize();
    }

    private void initialize(){

        parent = findViewById(R.id.activityPostForm);
        auth = FirebaseAuth.getInstance();
        btnCancel = findViewById(R.id.btnCancel);
        btnSubmit = findViewById(R.id.btnSubmit);
        emoji1 = findViewById(R.id.emoji1);
        emoji2 = findViewById(R.id.emoji2);
        emoji3 = findViewById(R.id.emoji3);
        emoji4 = findViewById(R.id.emoji4);
        etPostContent = findViewById(R.id.etPostContent);
        tvPostContentCount = findViewById(R.id.tvPostContentCount);
        tvFoodTitle = findViewById(R.id.tvFoodTitle);
        tvBrand = findViewById(R.id.tvBrand);
        tvAllergens = findViewById(R.id.tvAllergens);
        ivFoodImage = findViewById(R.id.ivFoodImage);

        emoji1.setOnClickListener(v -> handleFeelingSelection(emoji1));
        emoji2.setOnClickListener(v-> handleFeelingSelection(emoji2));
        emoji3.setOnClickListener(v -> handleFeelingSelection(emoji3));
        emoji4.setOnClickListener(v -> handleFeelingSelection(emoji4));

        Bundle receivedBundle = getIntent().getExtras();
        if(receivedBundle != null){

            name = receivedBundle.getString("name");
            brand = receivedBundle.getString("brand");
            allergens = receivedBundle.getString("allergens");
            imageUrl = receivedBundle.getString("imageUrl");

        }

        Glide.with(PostFormActivity.this)
                .load(imageUrl)
                .into(ivFoodImage);

        tvFoodTitle.setText(name);
        tvBrand.setText(brand);
        tvAllergens.setText(allergens
                .replace("[","")
                .replace("]",""));

        etPostContent.addTextChangedListener(new TextWatcher() {

            private String previousContent = "";
            private boolean isEditing = false;
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(!isEditing) previousContent = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(isEditing) return;

                postContent = etPostContent.getText().toString();
                postContentLength = postContent.trim().length();

                if(postContentLength > maxContentLength){
                    isEditing = true;

                    etPostContent.setText(previousContent);
                    etPostContent.setSelection(previousContent.length());

                    isEditing = false;
                }else{
                    tvPostContentCount.setText(String.valueOf(postContentLength));
                }
            }
        });

        btnSubmit.setOnClickListener(v -> createNewPost());
        btnCancel.setOnClickListener(v -> {finish();});

    }

    private void handleFeelingSelection(MaterialButton clickedButton){
        if(selectedBtn != null){
            selectedBtn.setBackgroundTintList(
                    ColorStateList.valueOf(getResources().getColor(R.color.black))
            );
        }

        selectedBtn = clickedButton;
        selectedBtn.setBackgroundTintList(
                ColorStateList.valueOf(getResources().getColor(R.color.white))
        );

        selectedEmoji = selectedBtn.getTag().toString();
    }

    private void createNewPost(){

        String uid = auth.getCurrentUser().getUid();
        String postContent = etPostContent.getText().toString();
        String allergenList = tvAllergens.getText().toString();
        String brand = tvBrand.getText().toString();
        String foodTitle = tvFoodTitle.getText().toString();


       if(postContent.isEmpty() || selectedEmoji.isEmpty()){

           UIUtil.showSnackbar(parent, "All fields are required!");

       }else{

           DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                   .getReference("users")
                   .child(uid);

           userRef.child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   fullName = snapshot.getValue(String.class);

                   DatabaseReference rootRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference();

                   String postId = rootRef.child("posts").push().getKey();

                   Post postObject = new Post(
                           fullName,
                           selectedEmoji,
                           postContent,
                           imageUrl,
                           foodTitle,
                           brand,
                           allergenList
                   );

                   Map<String, Object> updates = new HashMap<>();

                   updates.put("posts/" + postId, postObject);

                   updates.put("users/" + uid + "/postList/" + postId, true);

                   rootRef.updateChildren(updates);
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {}
           });

           UIUtil.showSnackbar(parent, "Posted Successfully!");

           new Handler(Looper.getMainLooper()).postDelayed(() -> {
               finish();
           }, 1500);

       }

    }

}
