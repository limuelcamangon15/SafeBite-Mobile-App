package com.project.safebite.model;

import java.time.LocalDateTime;
import java.util.List;

public class User {

    private String fullName;
    private String email;
    private String password;
    private List<String> allergies;
    private long registeredAt;

    public User(){
        this.registeredAt = System.currentTimeMillis();
    }

    // constructor for registration
    public User(String fullName, String email){
        this.fullName = fullName;
        this.email = email;
        this.registeredAt = System.currentTimeMillis();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public long getRegisteredAt(){
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt){
        this.registeredAt = registeredAt;
    }
}
