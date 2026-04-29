// PaymentMethodData.java
package com.ivankudravcev.sandboxspringsite2026copy.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodData {
    private String type;
}