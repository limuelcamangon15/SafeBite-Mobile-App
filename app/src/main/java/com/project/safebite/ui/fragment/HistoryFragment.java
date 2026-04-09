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
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {

    public HistoryFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    private RecyclerView rvHistory;

    private HistoryAdapter adapter;

    private List<Product> historyList;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        auth = FirebaseAuth.getInstance();

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setHasFixedSize(true);

        historyList = new ArrayList<>();

        adapter = new HistoryAdapter(getContext(), historyList);
        rvHistory.setAdapter(adapter);

        String uid = auth.getCurrentUser().getUid();
        String path = "users/" + uid + "/scanHistory";
        DatabaseReference historyRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for(DataSnapshot historySnapshot: snapshot.getChildren()){
                    Product product = historySnapshot.getValue(Product.class);
                    if(product!=null)historyList.add(product);
                }

                historyList.sort(Comparator.comparingLong(Product::getScannedAt).reversed());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Failed to scan history", error.toException());
            }
        });

        return view;
    }
}
