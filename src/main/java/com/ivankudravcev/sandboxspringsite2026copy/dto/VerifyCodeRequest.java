package com.ivankudravcev.sandboxspringsite2026copy.dto;

import lombok.Data;

@Data
public class VerifyCodeRequest {
    private String mailCode;
    private String username;
    private String password;
    private boolean rememberMe;

}
