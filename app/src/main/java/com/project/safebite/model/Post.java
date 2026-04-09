package com.project.safebite.model;

import java.util.ArrayList;

public class Post {
    private String username;
    private String postFeeling;

    private String postContent;
    private String imageURL;
    private String brand;
    private String foodTitle;
    private String allergens;
    private long postedAt;

    public Post(){}

    public Post(
            String username,
            String postFeeling,
            String postContent,
            String imageURL,
            String brand,
            String foodTitle,
            String allergens
    ){
        this.username = username;
        this.foodTitle = foodTitle;
        this.postFeeling = postFeeling;
        this.postContent = postContent;
        this.imageURL = imageURL;
        this.allergens = allergens;
        this.brand = brand;
        this.postedAt = System.currentTimeMillis();
    }

    public String getUsername(){ return username; }
    public String getPostFeeling(){ return postFeeling; }
    public String getPostContent(){ return postContent; }

    public String getImageURL(){ return imageURL; }
    public String getAllergens(){ return allergens; }
    public String getBrand(){ return brand; }
    public String getFoodTitle(){ return foodTitle; }

    public long getPostedAt(){ return postedAt; }
    public void setPostedAt(long postedAt){ this.postedAt = postedAt; }


}
