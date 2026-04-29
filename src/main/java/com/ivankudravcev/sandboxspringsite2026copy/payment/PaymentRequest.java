// PaymentRequest.java - ГЛАВНЫЙ КЛАСС
package com.ivankudravcev.sandboxspringsite2026copy.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    private Amount amount;

    @JsonProperty("payment_method_data")
    private PaymentMethodData paymentMethodData;

    private Confirmation confirmation;

    private String description;

    private boolean capture;
}