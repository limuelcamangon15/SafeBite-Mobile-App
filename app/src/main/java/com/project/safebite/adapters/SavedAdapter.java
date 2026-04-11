package com.project.safebite.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Product;
import com.project.safebite.utils.UIUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.SavedViewHolder>{

    private List<Product> savedProductList = new ArrayList<>();

    private Context context;
    private boolean isProductSaved = false;
    private FirebaseAuth auth;
    private View parent;
    private String uid = null;
    List<String> userAllergen;
    public SavedAdapter(View parent, Context context, List<Product> savedProductList, FirebaseAuth auth){
        this.savedProductList = savedProductList;
        this.context = context;
        this.parent = parent;
        this.auth = auth;
        uid = auth.getCurrentUser().getUid();
        userAllergen = new ArrayList<>();
    }

    @NonNull
    @Override
    public SavedAdapter.SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_recommended_product, parent, false);
        return new SavedAdapter.SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedAdapter.SavedViewHolder holder, int position){

        Product product = savedProductList.get(position);

        String path = "users/" + uid + "/savedProducts";
        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);
        String imageUrl = product.getImageUrl();
        if(imageUrl != null && !imageUrl.isEmpty()){
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .override(300,300)
                    .into(holder.ivImage);
        }else{
            holder.ivImage.setImageResource(R.drawable.samplefudgee);
        }

        String pathAllergies = "users/" + uid + "/allergens";
        DatabaseReference allergenRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(pathAllergies);
        allergenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userAllergen.clear();
                for(DataSnapshot child : snapshot.getChildren()){
                    String allergen = child.getValue(String.class);
                    if(allergen!=null)userAllergen.add(allergen);
                }

                checkAllergens(product.getAllergens(), userAllergen, holder.tvAllergens);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {            }
        });

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        holder.tvAllergens.setText(android.text.TextUtils.join("\n", product.getAllergens()));
        holder.tvNutrimentAnalysis.setText(product.getNutrimentsAnalysis());
        holder.ibBookmark.setVisibility(View.VISIBLE);
        holder.ibBookmark.setOnClickListener(v->saveProduct(product, holder.ibBookmark));

        userRef.child(product.getBarcode()).get().addOnSuccessListener(snapshot -> {
            if(snapshot.exists()){
                isProductSaved = true;
                holder.ibBookmark.setBackgroundResource(R.drawable.bookmark_solid);
            } else {
                isProductSaved = false;
                holder.ibBookmark.setBackgroundResource(R.drawable.bookmark_regular);
            }
        });



    }

    private void saveProduct(Product savedProduct, ImageButton ibBookmark){
        isProductSaved = !isProductSaved;

        String path = "users/" + uid + "/savedProducts";
        String barcode = savedProduct.getBarcode();

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        if(isProductSaved){
            ;
            userRef.child(barcode).setValue(savedProduct);
            UIUtil.showSnackbar(parent, "Saved Product!");
            ibBookmark.setBackgroundResource(R.drawable.bookmark_solid);
        }
        else{
            userRef.child(barcode).removeValue();
            UIUtil.showSnackbar(parent, "Product Unsaved!");
            ibBookmark.setBackgroundResource(R.drawable.bookmark_regular);
        }

        userRef.keepSynced(true);

    }


    private void checkAllergens(List<String> productAllergens, List<String> userAllergies, TextView tvAllergens){
        if(!productAllergens.isEmpty() && !userAllergies.isEmpty()){
            boolean allergenMatched = !Collections.disjoint(
                    userAllergies.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()),
                    productAllergens.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()));
            if(allergenMatched){
                    tvAllergens.setTextColor(ContextCompat.getColor(context, R.color.red));
                }

            }
        }

    @Override
    public int getItemCount() {
        return savedProductList.size();
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder{
        TextView tvAllergens, tvBrand, tvName, tvNutrimentAnalysis;
        ImageView ivImage;
        ImageButton ibBookmark;

        public SavedViewHolder(@NonNull View foodView){
            super(foodView);
            tvAllergens = foodView.findViewById(R.id.tvAllergens);
            tvBrand = foodView.findViewById(R.id.tvBrand);
            tvName = foodView.findViewById(R.id.tvName);
            tvNutrimentAnalysis = foodView.findViewById(R.id.tvNutrimentsAnalysis);
            ivImage = foodView.findViewById(R.id.ivImage);
            ibBookmark = foodView.findViewById(R.id.ibBookmark);
        }
    }
}
