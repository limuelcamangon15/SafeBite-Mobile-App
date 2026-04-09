package com.project.safebite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.project.safebite.R;
import com.project.safebite.model.Post;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder>{

    private List<Post> postList;
    private Context context;

    public PostAdapter(Context context, List<Post> postList){
        this.postList = postList;
        this.context = context;
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
        switch (post.getPostFeeling()){
            case "Angry" :
                colorRes = R.color.red;
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
                    .placeholder(R.drawable.samplefudgee)
                    .error(R.drawable.samplefudgee)
                    .into(holder.ivEmojiIcon);
        } else {
            holder.foodImageIv.setImageResource(R.drawable.samplefudgee);
        }

        // food image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.samplefudgee)
                    .error(R.drawable.samplefudgee)
                    .into(holder.foodImageIv);
        } else {
            holder.foodImageIv.setImageResource(R.drawable.samplefudgee);
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{
        TextView usernameTv, postContentTv, brandTv, allergensTv, emojiTv, foodTitleTv;
        ImageView ivEmojiIcon, foodImageIv; //for images to

        public PostViewHolder(@NonNull View itemView){
            super(itemView);

            usernameTv = itemView.findViewById(R.id.usernameTv);
            postContentTv = itemView.findViewById(R.id.postContentTv);
            brandTv = itemView.findViewById(R.id.brandTv);
            emojiTv = itemView.findViewById(R.id.emojiTv);
            foodTitleTv = itemView.findViewById(R.id.foodTitleTV);
            allergensTv = itemView.findViewById(R.id.allergensTv);

            foodImageIv = itemView.findViewById(R.id.foodImageIv);
            ivEmojiIcon = itemView.findViewById(R.id.ivEmojiIcon);
        }
    }
}


