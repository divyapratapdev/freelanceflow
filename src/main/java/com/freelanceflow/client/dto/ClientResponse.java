package com.freelanceflow.client.dto;

import com.freelanceflow.client.Client;

import java.time.Instant;

public class ClientResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;

    public ClientResponse() {}

    public static ClientResponse from(Client c) {
        ClientResponse r = new ClientResponse();
        r.id = c.getId();
        r.userId = c.getUserId();
        r.name = c.getName();
        r.email = c.getEmail();
        r.phone = c.getPhone();
        r.company = c.getCompany();
        r.address = c.getAddress();
        r.createdAt = c.getCreatedAt();
        r.updatedAt = c.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getCompany() { return company; }
    public String getAddress() { return address; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
