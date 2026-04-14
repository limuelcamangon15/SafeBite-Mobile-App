package com.project.safebite.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.safebite.R;
import com.project.safebite.constants.DatabaseConstants;
import com.project.safebite.model.Post;
import com.project.safebite.ui.activity.PostFormActivity;
import com.project.safebite.utils.UIUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> postList;
    private Context context;
    private FirebaseAuth auth;
    String uid = "";
    View parent;
    String source;
    public PostAdapter(Context context, List<Post> postList, View parent, String source){
        this.postList = postList;
        this.context = context;
        this.parent = parent;
        this.source = source;
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.usernameTv.setText(post.getUsername());

        int colorRes = 0;
        int emojiIconRes = 0;
        int textColorRes = R.color.black;
        switch (post.getPostFeeling()){
            case "Angry" :
                colorRes = R.color.red;
                textColorRes = R.color.white;
                emojiIconRes = R.drawable.angryemoji;
                break;
            case "Happy" :
                colorRes = R.color.yellow;
                emojiIconRes = R.drawable.likeemoji;
                break;
            case "Sad" :
                colorRes = R.color.blue;
                emojiIconRes = R.drawable.sademoji;
                break;
            case "Neutral" :
                colorRes = R.color.gray;
                emojiIconRes = R.drawable.neutralemoji;
                break;
            default:
                colorRes = R.color.white;
                emojiIconRes = R.drawable.samplefudgee;
                break;
        }

        holder.emojiTv.setTextColor(ContextCompat.getColor(context, colorRes));
        holder.postContentTv.setBackgroundTintList(ContextCompat.getColorStateList(context, colorRes));
        holder.postContentTv.setTextColor(ContextCompat.getColorStateList(context, textColorRes));
        holder.postContentTv.setText(post.getPostContent());
        holder.allergensTv.setText(post.getAllergens());
        holder.brandTv.setText(post.getBrand());
        holder.foodTitleTv.setText(post.getFoodTitle());
        holder.emojiTv.setText(post.getPostFeeling());
        String imageUrl = post.getImageURL();

        //emoji icon
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(emojiIconRes)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .override(300,300)
                    .into(holder.ivEmojiIcon);
        } else {
            holder.foodImageIv.setImageResource(R.drawable.samplefudgee);
        }

        // food image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .override(300,300)
                    .into(holder.foodImageIv);
        } else {
            holder.foodImageIv.setImageResource(R.drawable.samplefudgee);
        }

        if(source.equals("history")){
            holder.ibOptions.setVisibility(View.VISIBLE);
            holder.ibOptions.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.post_menu);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        Log.d("debug", post.getPostId());
                        Bundle bundle = new Bundle();
                        bundle.putString("username", post.getUsername());
                        bundle.putString("postId", post.getPostId());
                        bundle.putString("imageUrl", post.getImageURL());
                        bundle.putString("productName", post.getFoodTitle());
                        bundle.putString("productBrand", post.getBrand());
                        bundle.putString("productAllergens", post.getAllergens());
                        bundle.putString("emoji", post.getPostFeeling());
                        bundle.putString("postContent", post.getPostContent());
                        bundle.putString("source", "history");

                        Intent intent = new Intent(context, PostFormActivity.class);
                        intent.putExtras(bundle);
                        context.startActivity(intent);

                    } else if (id == R.id.action_delete) {

                        Log.d("DELETE", "uid: " + uid);
                        Log.d("DELETE", "postId: " + post.getPostId());
                        new MaterialAlertDialogBuilder(context)
                                .setTitle("Delete Item")
                                .setMessage("Are you sure you want to delete this?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    deletePost(post.getPostId());

                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });


        }




    }




    private void deletePost(String postId){
        String userPath = "users/" + uid +"/postList/" + postId;
        String postPath = "posts/" + postId;

        DatabaseReference db = FirebaseDatabase.getInstance(DatabaseConstants.DATABASE_URL).getReference();
        Map<String, Object> deleteUpdates = new HashMap<>();

        deleteUpdates.put(postPath, null);
        deleteUpdates.put(userPath, null);
        db.updateChildren(deleteUpdates)
                .addOnSuccessListener(unused -> {
                    UIUtil.showSnackbar(parent, "Post deleted!");
                })
                .addOnFailureListener(e -> {
                    UIUtil.showSnackbar(parent, "Failed to delete post");
                });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        TextView usernameTv, postContentTv, brandTv, allergensTv, emojiTv, foodTitleTv;
        ImageView ivEmojiIcon, foodImageIv; //for images to

        ImageButton ibOptions;

        public PostViewHolder(@NonNull View itemView){
            super(itemView);

            usernameTv = itemView.findViewById(R.id.usernameTv);
            postContentTv = itemView.findViewById(R.id.postContentTv);
            brandTv = itemView.findViewById(R.id.brandTv);
            emojiTv = itemView.findViewById(R.id.emojiTv);
            foodTitleTv = itemView.findViewById(R.id.foodTitleTV);
            allergensTv = itemView.findViewById(R.id.tvAllergens);

            foodImageIv = itemView.findViewById(R.id.foodImageIv);
            ivEmojiIcon = itemView.findViewById(R.id.ivEmojiIcon);
            ibOptions = itemView.findViewById(R.id.ibOptions);
        }
    }
}


