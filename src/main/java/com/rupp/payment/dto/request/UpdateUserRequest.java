package com.rupp.payment.dto.request;

import jakarta.validation.constraints.Email;
import com.rupp.payment.entity.Role;

public class UpdateUserRequest {

    private String username;

    @Email
    private String email;

    @jakarta.validation.constraints.Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

    private Role role;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
