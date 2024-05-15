package com.shares.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
public class QuoteRequest {

        @NotBlank(message = "symbol field empty")
        private final String symbol;

        @NotNull(message = "Shares field empty")
        @Digits(integer = Integer.MAX_VALUE, fraction = 2, message = "Invalid value for shares")
        private final BigDecimal shares;

        public QuoteRequest(String symbol, BigDecimal shares) {
            this.symbol = symbol;
            this.shares = shares;

        }

    }

