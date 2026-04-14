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
            handleRendering("All", uid);

            btnFilterAll.setOnClickListener(v -> handleFilterClick(btnFilterAll, llFilter, uid));
            btnFilterAllergic.setOnClickListener(v -> handleFilterClick(btnFilterAllergic, llFilter, uid));
            btnFilterSaved.setOnClickListener(v -> handleFilterClick(btnFilterSaved, llFilter, uid));

            return view;
        }


        private void handleFilterClick(TextView clickedView, LinearLayout parent, String uid){
//            if (parent == null) return;
//
//            for (int i = 0; i < parent.getChildCount(); i++) {
//                View child = parent.getChildAt(i);
//                if (child instanceof TextView) {
//                    child.setBackgroundResource(R.drawable.filter_bg);
//                    ((TextView) child).setTextColor(Color.parseColor("#3b6d11"));
//                }
//            }

            clickedView.setBackgroundResource(R.drawable.filter_bg_active);
            clickedView.setTextColor(Color.parseColor("#A4C639"));

            parent.removeView(clickedView);
            parent.addView(clickedView, 0);

            ViewParent layoutParent = parent.getParent();
            if (layoutParent instanceof HorizontalScrollView) {
                ((HorizontalScrollView) layoutParent).smoothScrollTo(0, 0);
            }

            String clicked = clickedView.getText().toString();
            handleRendering(clicked, uid);

        }

        private void handleRendering(String clicked, String uid){

            List<String> pathToFetch = new ArrayList<>();
            Map<String, Product> productMap = new LinkedHashMap<>();
            switch (clicked){
                case "All":
                    pathToFetch.add("users/" + uid + "/savedProducts");
                    pathToFetch.add("users/" + uid + "/savedAllergicProducts");
                    break;
                case "Saved":
                    pathToFetch.add("users/" + uid + "/savedProducts");
                    break;
                case "Allergic":
                    pathToFetch.add("users/" + uid + "/savedAllergicProducts");
                    break;
            }

            for (String path : pathToFetch){
                DatabaseReference userRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                        .getReference(path);

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot savedSnapshot: snapshot.getChildren()){
                            Product product = savedSnapshot.getValue(Product.class);
                            if(product!=null)productMap.put(savedSnapshot.getKey(), product);
                        }

                        productList.clear();
                        productList.addAll(productMap.values());

                        Collections.reverse(productList);
                        adapter.notifyDataSetChanged();

                        if(productList.isEmpty()){
                            rvSaved.setVisibility(View.GONE);
                            tvNoSaved.setVisibility(View.VISIBLE);
                            tvNoSaved.setText("No Saved Products");
                        } else {
                            tvNoSaved.setVisibility(View.GONE);
                            rvSaved.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("RTDB", "Failed to fetch", error.toException());
                    }
                });
            }

        }

    }
