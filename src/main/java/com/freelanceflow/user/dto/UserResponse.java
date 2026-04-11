package com.freelanceflow.user.dto;

import com.freelanceflow.user.User;

import java.time.Instant;

public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String businessName;
    private String phone;
    private Instant createdAt;

    public UserResponse() {}

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.email = user.getEmail();
        r.fullName = user.getFullName();
        r.businessName = user.getBusinessName();
        r.phone = user.getPhone();
        r.createdAt = user.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
