package com.project.safebite.model;

import java.time.LocalDateTime;
import java.util.List;

public class User {

    private String fullName;
    private String email;
    private String password;
    private List<String> allergies;
    private LocalDateTime registeredAt;

    public User(){
        this.registeredAt = LocalDateTime.now();
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

    public LocalDateTime getRegisteredAt(){
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt){
        this.registeredAt = registeredAt;
    }
}
