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

import java.util.ArrayList;
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
    TextView tvNoFeed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        networkViewModel = new ViewModelProvider(requireActivity()).get(NetworkViewModel.class);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoFeed = view.findViewById(R.id.tvNoFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        postList = new ArrayList<>();

        adapter = new PostAdapter(getContext(), postList, view, "home");
        recyclerView.setAdapter(adapter);

        networkViewModel.getIsConnected().observe(getViewLifecycleOwner(), isConnected->{
            isWifiConnected = isConnected;

            if(isWifiConnected || !postList.isEmpty()){
                recyclerView.setVisibility(View.VISIBLE);
                tvNoFeed.setVisibility(View.GONE);
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

                        postList.sort(Comparator.comparingLong(Post::getPostedAt).reversed());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("RTDB", "Failed to read posts", error.toException());
                    }
                });

            }else{
                recyclerView.setVisibility(View.GONE);
                tvNoFeed.setVisibility(View.VISIBLE);
                tvNoFeed.setText("No posts to retrieve");
            }
        });

        return view;
    }
}