package com.freelanceflow.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ClientRequest {

    @NotBlank(message = "Client name is required")
    private String name;

    @NotBlank(message = "Client email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String phone;
    private String company;
    private String address;

    public ClientRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
