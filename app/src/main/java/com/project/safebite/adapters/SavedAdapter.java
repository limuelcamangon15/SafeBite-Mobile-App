package com.project.safebite.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.ViewHolder> {

    private List<Product> list;
    private Context context;
    private View parent;
    private String uid;
    private List<String> userAllergen = new ArrayList<>();

    public SavedAdapter(View parent, Context context, List<Product> list, FirebaseAuth auth) {
        this.parent = parent;
        this.context = context;
        this.list = list;
        this.uid = auth.getCurrentUser().getUid();

        // Fetch user allergens ONCE when the adapter is created to prevent lag during scrolling
        fetchUserAllergens();
    }

    private void fetchUserAllergens() {
        String pathAllergies = "users/" + uid + "/allergens";
        DatabaseReference allergenRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference(pathAllergies);

        allergenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userAllergen.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String allergen = child.getValue(String.class);
                    if (allergen != null) userAllergen.add(allergen);
                }
                notifyDataSetChanged(); // Refresh list to highlight allergens
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SavedAdapter", "Failed to load allergens", error.toException());
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBrand, tvAllergens, tvNutriments;
        ImageView ivImage;
        ImageButton ibSave, ibAllergic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvAllergens = itemView.findViewById(R.id.tvAllergens);
            tvNutriments = itemView.findViewById(R.id.tvNutrimentsAnalysis);
            ivImage = itemView.findViewById(R.id.ivImage);
            ibSave = itemView.findViewById(R.id.ibSave);
            ibAllergic = itemView.findViewById(R.id.ibAllergic);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_recommended_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        holder.tvNutriments.setText(product.getNutrimentsAnalysis());

        // Handle Allergens Text
        List<String> allergenList = product.getAllergens();
        if (allergenList != null && !allergenList.isEmpty()) {
            holder.tvAllergens.setText(android.text.TextUtils.join("\n", allergenList));
        } else {
            holder.tvAllergens.setText("No Allergen Listed");
        }

        checkAllergens(allergenList != null ? allergenList : new ArrayList<>(), userAllergen, holder.tvAllergens);

        // Handle Image
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .override(300, 300)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder);
        }

        holder.ibSave.setTag("save_" + product.getBarcode());
        holder.ibAllergic.setTag("allergic_" + product.getBarcode());

        setUnsavedUI(holder.ibSave);
        setUnallergicUI(holder.ibAllergic);

        DatabaseReference savedRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/savedProducts/" + product.getBarcode());
        DatabaseReference allergicRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/savedAllergicProducts/" + product.getBarcode());

        Task<DataSnapshot> savedTask = savedRef.get();
        Task<DataSnapshot> allergicTask = allergicRef.get();

        Tasks.whenAllSuccess(savedTask, allergicTask).addOnSuccessListener(results -> {
            DataSnapshot savedSnapshot = (DataSnapshot) results.get(0);
            DataSnapshot allergicSnapshot = (DataSnapshot) results.get(1);

            if (savedSnapshot.exists() && holder.ibSave.getTag().equals("save_" + product.getBarcode())) {
                setSavedUI(holder.ibSave);
            }

            if (allergicSnapshot.exists() && holder.ibAllergic.getTag().equals("allergic_" + product.getBarcode())) {
                setAllergicUI(holder.ibAllergic);
            }
        });

        holder.ibSave.setOnClickListener(v -> toggleSave(product, holder.ibSave));
        holder.ibAllergic.setOnClickListener(v -> toggleAllergic(product, holder.ibAllergic));
    }

    private void toggleSave(Product product, ImageButton ibSave) {
        DatabaseReference ref = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/savedProducts");

        String id = product.getBarcode();

        ref.child(id).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                ref.child(id).removeValue();
                setUnsavedUI(ibSave);
                UIUtil.showSnackbar(parent, "Product Unsaved!");
            } else {
                ref.child(id).setValue(product);
                setSavedUI(ibSave);
                UIUtil.showSnackbar(parent, "Product Saved!");
            }
            ref.keepSynced(true);
        });
    }

    private void toggleAllergic(Product product, ImageButton ibAllergic) {
        DatabaseReference ref = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/savedAllergicProducts");

        String id = product.getBarcode();

        ref.child(id).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                ref.child(id).removeValue();
                setUnallergicUI(ibAllergic);
                UIUtil.showSnackbar(parent, "Product Unmarked as Allergic!");
            } else {
                ref.child(id).setValue(product);
                setAllergicUI(ibAllergic);
                UIUtil.showSnackbar(parent, "Product Marked as Allergic!");
            }
            ref.keepSynced(true);
        });
    }

    private void checkAllergens(List<String> productAllergens, List<String> userAllergies, TextView tvAllergens) {
        tvAllergens.setTextColor(ContextCompat.getColor(context, R.color.white));
        if (!productAllergens.isEmpty() && !userAllergies.isEmpty()) {
            boolean allergenMatched = !Collections.disjoint(
                    userAllergies.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()),
                    productAllergens.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toList()));

            if (allergenMatched) {
                tvAllergens.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }
    }

    private void setSavedUI(ImageButton ibSave) {
        ibSave.setImageResource(R.drawable.bookmark_check_24px);
        ibSave.setBackgroundResource(R.drawable.rounded_bg_green_active);
        ibSave.setColorFilter(Color.parseColor("#639922"));
    }

    private void setUnsavedUI(ImageButton ibSave) {
        ibSave.setImageResource(R.drawable.bookmark_24px);
        ibSave.setBackgroundResource(R.drawable.rounded_bg_green);
        ibSave.setColorFilter(Color.parseColor("#639922"));
    }

    private void setAllergicUI(ImageButton ibAllergic) {
        ibAllergic.setImageResource(R.drawable.warning_24px);
        ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red_active);
        ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
    }

    private void setUnallergicUI(ImageButton ibAllergic) {
        ibAllergic.setImageResource(R.drawable.warning_24px);
        ibAllergic.setBackgroundResource(R.drawable.rounded_bg_red);
        ibAllergic.setColorFilter(Color.parseColor("#E24B4A"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}