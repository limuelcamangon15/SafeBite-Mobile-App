package com.project.safebite.adapters;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Post;
import com.project.safebite.model.Product;
import com.project.safebite.utils.UIUtil;

import java.util.Collections;
import java.util.List;

public class RecommendedProductAdapter extends RecyclerView.Adapter<RecommendedProductAdapter.RecommendedViewHolder>{

    private List<Product> productList;
    private Context context;
    private boolean isProductSaved = false;
    private FirebaseAuth auth;
    private View parent;
    String uid = null;
    List<String> userAllergen;
    public RecommendedProductAdapter(View parent, Context context, List<Product> productList, List<String> userAllergen){
        this.productList = productList;
        this.context = context;
        this.parent = parent;
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        this.userAllergen = userAllergen;
    }

    @NonNull
    @Override
    public RecommendedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_recommended_product, parent, false);
        return new RecommendedProductAdapter.RecommendedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedProductAdapter.RecommendedViewHolder holder, int position) {
        // logic

        Product product = productList.get(position);
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

        checkAllergens(product.getAllergens(), userAllergen, holder.tvAllergens);

    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void saveProduct(Product savedProduct, ImageButton ibBookmark){
        isProductSaved = !isProductSaved;

        String path = "users/" + uid + "/savedProducts";
        String barcode = savedProduct.getBarcode();

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(path);

        if(isProductSaved){
;
            userRef.child(barcode).setValue(savedProduct);
            UIUtil.showSnackbar(parent, "Product Saved!");
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

    public static class RecommendedViewHolder extends RecyclerView.ViewHolder{

        TextView tvAllergens, tvBrand, tvName, tvNutrimentAnalysis;
        ImageView ivImage;
        ImageButton ibBookmark;

        public RecommendedViewHolder(@NonNull View foodView){
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
