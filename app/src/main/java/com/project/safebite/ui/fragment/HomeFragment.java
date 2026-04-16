package com.project.safebite.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.safebite.R;
import com.project.safebite.adapters.PostAdapter;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.NetworkViewModel;
import com.project.safebite.model.Post;
import com.project.safebite.offlineAuth.AuthStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    NetworkViewModel networkViewModel;
    boolean isWifiConnected;
    TextView tvNoFeed, tvAllergenMatchedCount, tvMarkedAsUnsafeCount, tvSavedCount;
    FirebaseAuth auth;
    String uid= null;
    AuthStorage offlineAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoFeed = view.findViewById(R.id.tvNoFeed);
        tvAllergenMatchedCount = view.findViewById(R.id.tvAllergenMatchedCount);
        tvMarkedAsUnsafeCount = view.findViewById(R.id.tvMarkedAsUnsafeCount);
        tvSavedCount = view.findViewById(R.id.tvSavedCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        postList = new ArrayList<>();

        adapter = new PostAdapter(getContext(), postList, view, "home");
        recyclerView.setAdapter(adapter);
        auth = FirebaseAuth.getInstance();
        offlineAuth = new AuthStorage(getContext());

        uid = offlineAuth.getUserId();


        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected->{
            isWifiConnected = isConnected;
            Log.d("PostLIST IS Empty?", String.valueOf(postList.isEmpty()));
            if(isWifiConnected || !postList.isEmpty()){

                uid = auth.getCurrentUser().getUid();
                DatabaseReference postsRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                        .getReference("posts");

                postsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for(DataSnapshot postSnapshot: snapshot.getChildren()){
                            Post post = postSnapshot.getValue(Post.class);
                            if(post!=null)postList.add(post);
                        }


                        if(postList.isEmpty()){
                            recyclerView.setVisibility(View.GONE);
                            tvNoFeed.setVisibility(View.VISIBLE);
                            tvNoFeed.setText("No posts to show");
                        }else{
                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoFeed.setVisibility(View.GONE);
                            postList.sort(Comparator.comparingLong(Post::getPostedAt).reversed());
                            adapter.notifyDataSetChanged();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("RTDB", "Failed to read posts", error.toException());
                    }
                });



            }else{
                uid = offlineAuth.getUserId();
                recyclerView.setVisibility(View.GONE);
                tvNoFeed.setVisibility(View.VISIBLE);
                tvNoFeed.setText("No posts to show");
            }
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfDay = calendar.getTimeInMillis();

        int[] counts = {0, 0, 0};
        boolean[] done = {false, false, false};

        userRef.child("savedProducts")
                .orderByChild("timestamp")
                .startAt(startOfDay)
                .endAt(endOfDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        counts[0] = (int) snapshot.getChildrenCount();
                        done[0] = true;
                        updateUIDashboard(counts, done);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Dashboard", "savedProducts error: " + error.getMessage());
                    }
                });

        userRef.child("savedAllergicProducts")
                .orderByChild("timestamp")
                .startAt(startOfDay)
                .endAt(endOfDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        counts[1] = (int) snapshot.getChildrenCount();
                        done[1] = true;
                        updateUIDashboard(counts, done);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Dashboard", "savedAllergic error: " + error.getMessage());
                    }
                });

        userRef.child("scanHistory")
                .orderByChild("timestamp")
                .startAt(startOfDay)
                .endAt(endOfDay)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int matchedCount = 0;

                        for (DataSnapshot scan : snapshot.getChildren()) {
                            Boolean isMatched = scan.child("allergenMatched").getValue(Boolean.class);
                            if (isMatched != null && isMatched) {
                                matchedCount++;
                            }
                        }

                        counts[2] = matchedCount;
                        done[2] = true;
                        updateUIDashboard(counts, done);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Dashboard", "allergenScans error: " + error.getMessage());
                    }
                });

        return view;
    }


private void updateUIDashboard(int[] counts, boolean[] done){
    if (!done[0] || !done[1] || !done[2]) return;

    requireActivity().runOnUiThread(() -> {
        tvSavedCount.setText(String.valueOf(counts[0]));
        tvMarkedAsUnsafeCount.setText(String.valueOf(counts[1]));
        tvAllergenMatchedCount.setText(String.valueOf(counts[2]));
    });
}





}