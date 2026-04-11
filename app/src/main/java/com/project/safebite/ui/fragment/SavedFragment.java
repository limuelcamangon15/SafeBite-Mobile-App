    package com.project.safebite.ui.fragment;

    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;

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
    import java.util.List;

    public class SavedFragment extends Fragment {

        public SavedFragment(){}

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
        }

        private RecyclerView rvSaved;

        private SavedAdapter adapter;

        private List<Product> savedProductList;
        FirebaseAuth auth;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

            View view = inflater.inflate(R.layout.fragment_saved_products, container, false);

            rvSaved = view.findViewById(R.id.rvSaved);
            auth = FirebaseAuth.getInstance();

            rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSaved.setHasFixedSize(true);

            savedProductList = new ArrayList<>();

            adapter = new SavedAdapter(view, getContext(), savedProductList, auth);
            rvSaved.setAdapter(adapter);

            if(savedProductList.isEmpty()){
                rvSaved.setVisibility(View.GONE);
            }

            String uid = auth.getCurrentUser().getUid();
            String path = "users/" + uid + "/savedProducts";
            DatabaseReference historyRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                    .getReference(path);

            historyRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    savedProductList.clear();
                    for(DataSnapshot savedSnapshot: snapshot.getChildren()){
                        Product product = savedSnapshot.getValue(Product.class);
                        if(product!=null)savedProductList.add(product);
                    }

                    Collections.reverse(savedProductList);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("RTDB", "Failed to fetch", error.toException());
                }
            });

            return view;
        }

    }
