package com.project.safebite.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.project.safebite.model.NetworkViewModel;
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
    Spinner spinner;
    String uid = "";

    NetworkViewModel networkViewModel;
    boolean isWifiConnected;
    TextView tvNoHistory;
    private ValueEventListener postListener;
    private ValueEventListener scanListener;
    private DatabaseReference postRef;
    private DatabaseReference scanRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_history, container, false);
        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);

        postList = new ArrayList<>();
        historyList = new ArrayList<>();

        rvHistory = view.findViewById(R.id.rvHistory);
        rvPosts = view.findViewById(R.id.rvPosts);
        spinner = view.findViewById(R.id.spFilter);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);

        auth = FirebaseAuth.getInstance();

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setHasFixedSize(true);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setHasFixedSize(true);

       uid = auth.getCurrentUser().getUid();

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

        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected->{
            isWifiConnected = isConnected;
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = items.get(position);

                if(selected.equals("Posts")){
                    rvHistory.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.GONE);
                    rvPosts.setVisibility(View.VISIBLE);
                    renderPosts(uid, view);
                } else {
                    rvPosts.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                    renderScans(uid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner.setSelection(1);
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (spinner.getSelectedItem().toString().equals("Posts")) {
            postList.clear();
            renderPosts(uid, rvPosts);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postListener != null && postRef != null) postRef.removeEventListener(postListener);
        if (scanListener != null && scanRef != null) scanRef.removeEventListener(scanListener);
    }

    private void renderPosts(String uid, View parent){

        if (scanListener != null && scanRef != null) {
            scanRef.removeEventListener(scanListener);
            scanListener = null;
        }

        String path = "users/" + uid + "/postList";
         postRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);

        postList = new ArrayList<>();

        postAdapter = new PostAdapter(getContext(), postList, parent, "history");
        rvPosts.setAdapter(postAdapter);

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                int total = (int) snapshot.getChildrenCount();

                if (total == 0 || !isWifiConnected) {
                    rvPosts.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.VISIBLE);
                    tvNoHistory.setText("No posts to retrieve");
                    return;
                }

                final int[] loadedCount = {0};
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    String postId = postSnapshot.getKey();
                    Log.d("post", postId);
                    FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                            .getReference("posts/" + postId)
                            .get()
                            .addOnSuccessListener(postData -> {
                                Post post = postData.getValue(Post.class);
                                if (post != null) postList.add(post);

                                loadedCount[0]++;
                                if (loadedCount[0] == total) {
                                    postList.sort(Comparator.comparingLong(Post::getPostedAt).reversed());
                                    tvNoHistory.setVisibility(View.GONE);
                                    rvPosts.setVisibility(View.VISIBLE);
                                    postAdapter.notifyDataSetChanged();
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Failed to retrieve post history", error.toException());
            }
        };
        postRef.addValueEventListener(postListener);
    }

    private void renderScans(String uid){

        if (postListener != null && postRef != null) {
            postRef.removeEventListener(postListener);
            postListener = null;
        }

        String path = "users/" + uid + "/scanHistory";
        scanRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference(path);

        historyList = new ArrayList<>();

        historyAdapter = new HistoryAdapter(getContext(), historyList);
        rvHistory.setAdapter(historyAdapter);

        scanListener = new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for(DataSnapshot historySnapshot: snapshot.getChildren()){
                    Product product = historySnapshot.getValue(Product.class);
                    if(product!=null)historyList.add(product);
                }

                if (historyList.isEmpty()) {
                    rvHistory.setVisibility(View.GONE);
                    tvNoHistory.setVisibility(View.VISIBLE);
                    tvNoHistory.setText("No scans to retrieve");
                } else {
                    tvNoHistory.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                    historyList.sort(Comparator.comparingLong(Product::getTimestamp).reversed());
                    if (historyList.size() > 15) {
                        historyList = historyList.subList(0, 15);
                    }
                    historyAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Failed to retrieve scan history", error.toException());
            }
        };

        scanRef.addValueEventListener(scanListener);

    }
}
