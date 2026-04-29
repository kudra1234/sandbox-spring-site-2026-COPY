package com.ivankudravcev.sandboxspringsite2026copy.dto;

import lombok.Data;

@Data
public class ChangePasswordUUIDRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
