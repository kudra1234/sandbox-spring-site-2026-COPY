package com.ivankudravcev.sandboxspringsite2026copy.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String userName;
    private String email;
}
