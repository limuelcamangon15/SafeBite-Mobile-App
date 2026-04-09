package com.project.safebite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Product;
import com.project.safebite.utils.UIUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.SavedViewHolder>{

    private List<Product> savedProductList;

    private Context context;
    private boolean isProductSaved = false;
    private FirebaseAuth auth;
    private View parent;
    private String uid = null;
    public SavedAdapter(View parent, Context context, List<Product> savedProductList, FirebaseAuth auth){
        this.savedProductList = savedProductList;
        this.context = context;
        this.parent = parent;
        this.auth = auth;
        uid = auth.getCurrentUser().getUid();
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
                    .placeholder(R.drawable.samplefudgee)
                    .error(R.drawable.samplefudgee)
                    .into(holder.ivImage);
        }else{
            holder.ivImage.setImageResource(R.drawable.samplefudgee);
        }

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        holder.tvAllergens.setText(product.getAllergens().toString());
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

    }

    @Override
    public int getItemCount() {
        return savedProductList.size();
    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder{
        TextView tvAllergens, tvBrand, tvName;
        ImageView ivImage;
        ImageButton ibBookmark;

        public SavedViewHolder(@NonNull View foodView){
            super(foodView);
            tvAllergens = foodView.findViewById(R.id.tvAllergens);
            tvBrand = foodView.findViewById(R.id.tvBrand);
            tvName = foodView.findViewById(R.id.tvName);
            ivImage = foodView.findViewById(R.id.ivImage);
            ibBookmark = foodView.findViewById(R.id.ibBookmark);
        }
    }
}
