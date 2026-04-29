package com.ivankudravcev.sandboxspringsite2026copy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class YooKassaNotification {

    private String type;
    private String event;
    private PaymentObject object;

    @Data
    public static class PaymentObject {
        private String id;
        private String status;
        private boolean paid;
        private Amount amount;

        @JsonProperty("authorization_details")
        private AuthorizationDetails authorizationDetails;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        private String description;

        @JsonProperty("expires_at")
        private OffsetDateTime expiresAt;

        private Map<String, Object> metadata;

        @JsonProperty("payment_method")
        private PaymentMethod paymentMethod;

        private boolean refundable;
        private boolean test;

        @JsonProperty("income_amount")
        private Amount incomeAmount;
    }

    @Data
    public static class Amount {
        private String value;
        private String currency;
    }

    @Data
    public static class AuthorizationDetails {
        private String rrn;

        @JsonProperty("auth_code")
        private String authCode;

        @JsonProperty("three_d_secure")
        private ThreeDSecure threeDSecure;
    }

    @Data
    public static class ThreeDSecure {
        private boolean applied;
    }

    @Data
    public static class PaymentMethod {
        private String type;
        private String id;
        private boolean saved;
        private Card card;
        private String title;

        @JsonProperty("issuer_country")
        private String issuerCountry;

        @JsonProperty("issuer_name")
        private String issuerName;
    }

    @Data
    public static class Card {
        private String first6;
        private String last4;

        @JsonProperty("expiry_month")
        private String expiryMonth;

        @JsonProperty("expiry_year")
        private String expiryYear;

        @JsonProperty("card_type")
        private String cardType;
    }
}