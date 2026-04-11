package com.project.safebite.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.project.safebite.adapters.PostAdapter;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Post;
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

    private RecyclerView rvHistory, rvPosts;

    private HistoryAdapter historyAdapter;
    private PostAdapter postAdapter;

    private List<Product> historyList;
    private List<Post> postList;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvPosts = view.findViewById(R.id.rvPosts);
        Spinner spinner = view.findViewById(R.id.spFilter);

        auth = FirebaseAuth.getInstance();

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setHasFixedSize(true);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setHasFixedSize(true);


        String uid = auth.getCurrentUser().getUid();

        List<String> items = new ArrayList<>();
        items.add("Posts");
        items.add("Scans");

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                items
        );
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spAdapter);
        spinner.setSelection(1);
        renderScans(uid);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = items.get(position);

                if(selected.equals("Posts")){
                    rvHistory.setVisibility(View.GONE);
                    rvPosts.setVisibility(View.VISIBLE);
                    renderPosts(uid, view);
                }else{
                    rvPosts.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                    renderScans(uid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }


    private void renderPosts(String uid, View parent){
        String path = "users/" + uid + "/postList";
        DatabaseReference historyRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);

        postList = new ArrayList<>();

        postAdapter = new PostAdapter(getContext(), postList, parent, "history");
        rvPosts.setAdapter(postAdapter);

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                        String postId = postSnapshot.getKey(); // get the postId

                        FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                                .getReference("posts/" + postId)
                                .get()
                                .addOnSuccessListener(postData -> {
                                    Post post = postData.getValue(Post.class);
                                    if (post != null) {
                                        postList.add(post);
                                        postList.sort(Comparator.comparingLong(Post::getPostedAt).reversed());
                                        postAdapter.notifyDataSetChanged();
                                    }
                                });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Failed to retrieve post history", error.toException());
            }
        });
    }

    private void renderScans(String uid){
        String path = "users/" + uid + "/scanHistory";
        DatabaseReference historyRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);

        historyList = new ArrayList<>();

        historyAdapter = new HistoryAdapter(getContext(), historyList);
        rvHistory.setAdapter(historyAdapter);

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for(DataSnapshot historySnapshot: snapshot.getChildren()){
                    Product product = historySnapshot.getValue(Product.class);
                    if(product!=null)historyList.add(product);
                }

                historyList.sort(Comparator.comparingLong(Product::getScannedAt).reversed());
                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Failed to retrieve scan history", error.toException());
            }
        });
    }
}
