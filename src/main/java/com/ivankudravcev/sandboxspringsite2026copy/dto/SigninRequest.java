package com.ivankudravcev.sandboxspringsite2026copy.dto;

import lombok.Data;

@Data
public class SigninRequest {
    private String password;
    private String username;
    private boolean rememberMe;
}
