// Confirmation.java
package com.ivankudravcev.sandboxspringsite2026copy.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Confirmation {
    private String type;

    @JsonProperty("return_url")
    private String returnUrl;

    // Конструктор для удобства
    public Confirmation(String type, String returnUrl) {
        this.type = type;
        this.returnUrl = returnUrl;
    }
}