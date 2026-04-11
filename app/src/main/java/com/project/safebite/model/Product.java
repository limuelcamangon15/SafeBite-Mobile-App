package com.project.safebite.model;

import java.util.List;

public class Product {

    String imageUrl;
    String name;
    String brand;
    List<String> allergens;
    String score;
    String barcode;
    long scannedAt;
    long timestamp;
    private String nutrimentsAnalysis;
    private String category;

    public Product(){};

    public Product(String name, String brand, String barcode, long scannedAt){
        this.name = name;
        this.brand = brand;
        this.barcode = barcode;
        this.scannedAt = scannedAt;
    }

    public Product(
            String imageUrl,
            String name,
            String brand,
            List<String> allergens,
            String barcode,
            String score,
            long timestamp,
            String nutrimentsAnalysis,
            String category
    ){
        this.imageUrl = imageUrl;
        this.name = name;
        this.brand = brand;
        this.allergens = allergens;
        this.score = score;
        this.barcode = barcode;
        this.timestamp = timestamp;
        this.nutrimentsAnalysis = nutrimentsAnalysis;
        this.category = category;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public String getBrand() {
        return brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getScore() {
        return score;
    }

    public long getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(long scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNutrimentsAnalysis() {
        return nutrimentsAnalysis;
    }

    public void setNutrimentsAnalysis(String nutrimentsAnalysis) {
        this.nutrimentsAnalysis = nutrimentsAnalysis;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
