package com.project.safebite.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.safebite.R;
import com.project.safebite.adapters.HistoryAdapter;
import com.project.safebite.adapters.SavedAdapter;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SavedFragment extends Fragment {

    public SavedFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    private RecyclerView rvSaved;

    private SavedAdapter adapter;

    private List<Product> productList;
    TextView tvNoSaved;
    FirebaseAuth auth;

    private TextView btnFilterAll, btnFilterAllergic,btnFilterSaved;
    String text = "No Products to show";
    private List<Product> savedList = new ArrayList<>();
    private List<Product> allergicList = new ArrayList<>();
    private String currentFilter = "All";
    private LinearLayout llFilter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_saved_products, container, false);

        rvSaved = view.findViewById(R.id.rvSaved);
        tvNoSaved = view.findViewById(R.id.tvNoSaved);
        auth = FirebaseAuth.getInstance();

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterAllergic = view.findViewById(R.id.btnFilterAllergic);
        btnFilterSaved = view.findViewById(R.id.btnFilterSaved);
        llFilter = view.findViewById(R.id.llFilter);

        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSaved.setHasFixedSize(true);

        productList = new ArrayList<>();

        adapter = new SavedAdapter(view, getContext(), productList, auth);
        rvSaved.setAdapter(adapter);

        String uid = auth.getCurrentUser().getUid();
        handleRendering(uid); //hello im here

        btnFilterAll.setOnClickListener(v -> handleFilterClick(btnFilterAll, llFilter, uid));
        btnFilterAllergic.setOnClickListener(v -> handleFilterClick(btnFilterAllergic, llFilter, uid));
        btnFilterSaved.setOnClickListener(v -> handleFilterClick(btnFilterSaved, llFilter, uid));

        return view;
    }


    private void handleFilterClick(TextView clickedView, LinearLayout parent, String uid){
        if (parent == null) return;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                child.setBackgroundResource(R.drawable.filter_bg);
                ((TextView) child).setTextColor(Color.parseColor("#3b6d11"));
            }
        }

        clickedView.setBackgroundResource(R.drawable.filter_bg_active);
        clickedView.setTextColor(Color.parseColor("#A4C639"));

        parent.removeView(clickedView);
        parent.addView(clickedView, 0);

        ViewParent layoutParent = parent.getParent();
        if (layoutParent instanceof HorizontalScrollView) {
            ((HorizontalScrollView) layoutParent).smoothScrollTo(0, 0);
        }

       currentFilter = clickedView.getText().toString();
        applyFilter();

    }

    private void handleRendering(String uid){

        String path = "users/" + uid;
        Map<String, Product> productMap = new LinkedHashMap<>();
//        switch (clicked){
//            case "All":
//                pathToFetch.add("users/" + uid + "/savedProducts");
//                pathToFetch.add("users/" + uid + "/savedAllergicProducts");
//                text = "No Products to show";
//                break;
//            case "Saved":
//                pathToFetch.add("users/" + uid + "/savedProducts");
//                text = "No Saved Products to show";
//                break;
//            case "Allergic":
//                pathToFetch.add("users/" + uid + "/savedAllergicProducts");
//                text = "No Allergic Products to show";
//                break;
//        }

        DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedList.clear();
                allergicList.clear();

                DataSnapshot savedSnap = snapshot.child("savedProducts");
                DataSnapshot allergicSnap = snapshot.child("savedAllergicProducts");

                if (savedSnap.exists()) {
                    for (DataSnapshot d : savedSnap.getChildren()) {
                        Product p = d.getValue(Product.class);
                        if (p != null) savedList.add(p);
                    }
                }

                if (allergicSnap.exists()) {
                    for (DataSnapshot d : allergicSnap.getChildren()) {
                        Product p = d.getValue(Product.class);
                        if (p != null) allergicList.add(p);
                    }
                }

            applyFilter();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        for (String path : pathToFetch){
//
//
//            userRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    for(DataSnapshot savedSnapshot: snapshot.getChildren()){
//                        Product product = savedSnapshot.getValue(Product.class);
//                        if(product!=null)productMap.put(savedSnapshot.getKey(), product);
//                    }
//
//                    productList.clear();
//                    productList.addAll(productMap.values());
//
//                    Collections.reverse(productList);
//                    adapter.notifyDataSetChanged();
//
//                    if(productList.isEmpty()){
//                        rvSaved.setVisibility(View.GONE);
//                        tvNoSaved.setVisibility(View.VISIBLE);
//                        tvNoSaved.setText(text);
//                    } else {
//                        tvNoSaved.setVisibility(View.GONE);
//                        rvSaved.setVisibility(View.VISIBLE);
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    Log.e("RTDB", "Failed to fetch", error.toException());
//                }
//            });


    }

    private void applyFilter() {

        List<Product> result = new ArrayList<>();
        java.util.HashMap<String, Product> uniqueMap = new java.util.HashMap<>();

        if (currentFilter.equals("Saved")) {
            for (Product p : savedList) {
                if (p != null && p.getBarcode() != null) {
                    uniqueMap.put(p.getBarcode(), p);
                }
            }
        }

        else if (currentFilter.equals("Allergic")) {
            for (Product p : allergicList) {
                if (p != null && p.getBarcode() != null) {
                    uniqueMap.put(p.getBarcode(), p);
                }
            }
        }

        else {
            for (Product p : savedList) {
                if (p != null && p.getBarcode() != null) {
                    uniqueMap.put(p.getBarcode(), p);
                }
            }

            for (Product p : allergicList) {
                if (p != null && p.getBarcode() != null) {
                    uniqueMap.put(p.getBarcode(), p);
                }
            }
        }

        result.addAll(uniqueMap.values());

        productList.clear();
        productList.addAll(result);

        adapter.notifyDataSetChanged();

        if (productList.isEmpty()) {
            rvSaved.setVisibility(View.GONE);
            tvNoSaved.setVisibility(View.VISIBLE);
            if(currentFilter.equals("Saved")){
                tvNoSaved.setText("No saved products to show");
            }else if(currentFilter.equals("Allergic")){
                tvNoSaved.setText("No allergic products to show");
            }else{
                tvNoSaved.setText("No products to show");
            }

        } else {
            rvSaved.setVisibility(View.VISIBLE);
            tvNoSaved.setVisibility(View.GONE);
        }

    }



}
