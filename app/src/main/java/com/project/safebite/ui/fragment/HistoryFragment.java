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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

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

    private enum Tab { SCANS, POSTS }

    private RecyclerView rvHistory, rvPosts;
    private HistoryAdapter historyAdapter;
    private PostAdapter postAdapter;

    private final List<Product> historyList = new ArrayList<>();
    private final List<Post> postList = new ArrayList<>();

    private FirebaseAuth auth;
    private String uid;

    private NetworkViewModel networkViewModel;
    private boolean isWifiConnected;

    private TextView tvNoHistory, btnFilterScans, btnFilterPosts;
    private LinearLayout llFilter;

    private ValueEventListener postListener;
    private ValueEventListener scanListener;

    private DatabaseReference postRef;
    private DatabaseReference scanRef;

    private Tab currentTab = Tab.SCANS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);

        rvHistory = view.findViewById(R.id.rvHistory);
        rvPosts = view.findViewById(R.id.rvPosts);
        tvNoHistory = view.findViewById(R.id.tvNoHistory);
        btnFilterPosts = view.findViewById(R.id.btnFilterPosts);
        btnFilterScans = view.findViewById(R.id.btnFilterScans);
        llFilter = view.findViewById(R.id.llFilter);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) return view;
        uid = auth.getCurrentUser().getUid();

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        rvHistory.setHasFixedSize(true);
        rvPosts.setHasFixedSize(true);

        postAdapter = new PostAdapter(getContext(), postList, view, "history");
        historyAdapter = new HistoryAdapter(getContext(), historyList);

        rvPosts.setAdapter(postAdapter);
        rvHistory.setAdapter(historyAdapter);

        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected -> {
            isWifiConnected = isConnected;
        });

        btnFilterScans.setOnClickListener(v -> handleTabClick(Tab.SCANS));
        btnFilterPosts.setOnClickListener(v -> handleTabClick(Tab.POSTS));

        handleTabClick(Tab.SCANS);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (postListener != null && postRef != null) {
            postRef.removeEventListener(postListener);
        }

        if (scanListener != null && scanRef != null) {
            scanRef.removeEventListener(scanListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentTab == Tab.POSTS) {
            loadPosts();
        }
    }

    private void handleTabClick(Tab tab) {
        currentTab = tab;
        updateFilterUI(tab);

        if (tab == Tab.POSTS) {
            loadPosts();
        } else {
            loadScans();
        }
    }

    private void updateFilterUI(Tab tab) {

        for (int i = 0; i < llFilter.getChildCount(); i++) {
            View child = llFilter.getChildAt(i);
            if (child instanceof TextView) {
                child.setBackgroundResource(R.drawable.filter_bg);
                ((TextView) child).setTextColor(Color.parseColor("#3b6d11"));
            }
        }

        TextView active = (tab == Tab.POSTS) ? btnFilterPosts : btnFilterScans;

        active.setBackgroundResource(R.drawable.filter_bg_active);
        active.setTextColor(Color.parseColor("#A4C639"));

        llFilter.removeView(active);
        llFilter.addView(active, 0);

        ViewParent parent = llFilter.getParent();
        if (parent instanceof HorizontalScrollView) {
            ((HorizontalScrollView) parent).smoothScrollTo(0, 0);
        }
    }

    private void showEmpty(String message) {
        tvNoHistory.setVisibility(View.VISIBLE);
        tvNoHistory.setText(message);
        rvHistory.setVisibility(View.GONE);
        rvPosts.setVisibility(View.GONE);
    }

    private void loadScans() {

        if (postListener != null && postRef != null) {
            postRef.removeEventListener(postListener);
            postListener = null;
        }

        scanRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/scanHistory");

        scanListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                historyList.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    Product p = s.getValue(Product.class);
                    if (p != null) historyList.add(p);
                }

                if (historyList.isEmpty()) {
                    showEmpty("No scans to show");
                    return;
                }

                historyList.sort(Comparator.comparingLong(Product::getTimestamp).reversed());

                if (historyList.size() > 15) {
                    historyList.subList(15, historyList.size()).clear();
                }

                rvHistory.setVisibility(View.VISIBLE);
                rvPosts.setVisibility(View.GONE);
                tvNoHistory.setVisibility(View.GONE);

                historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Scan load failed", error.toException());
            }
        };

        scanRef.addValueEventListener(scanListener);
    }

    private void loadPosts() {

        if (scanListener != null && scanRef != null) {
            scanRef.removeEventListener(scanListener);
            scanListener = null;
        }

        postRef = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                .getReference("users/" + uid + "/postList");

        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                postList.clear();

                int total = (int) snapshot.getChildrenCount();
                if (total == 0 || !isWifiConnected) {
                    showEmpty("No posts to show");
                    return;
                }

                final int[] loaded = {0};

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String postId = snap.getKey();

                    FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL)
                            .getReference("posts/" + postId)
                            .get()
                            .addOnSuccessListener(task -> {

                                Post post = task.getValue(Post.class);
                                if (post != null) postList.add(post);

                                loaded[0]++;

                                if (loaded[0] == total) {

                                    postList.sort(Comparator.comparingLong(Post::getPostedAt).reversed());

                                    rvPosts.setVisibility(View.VISIBLE);
                                    rvHistory.setVisibility(View.GONE);
                                    tvNoHistory.setVisibility(View.GONE);

                                    postAdapter.notifyDataSetChanged();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RTDB", "Post load failed", error.toException());
            }
        };

        postRef.addValueEventListener(postListener);
    }
}