package com.nguyenhuutai.example304.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RegisterDto {
    private String username;
    private String email;
    private String password;
    private Set<String> roles;
}