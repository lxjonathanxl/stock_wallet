package com.shares.wallet.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionRequest {

    @NotBlank(message = "symbol field empty")
    private String symbol;

    @NotNull(message = "Shares field empty")
    @Digits(integer = Integer.MAX_VALUE, fraction = 2, message = "Invalid value for shares")
    private BigDecimal shares;

    @NotNull(message = "price field empty")
    @Digits(integer = Integer.MAX_VALUE, fraction = 2, message = "Invalid value for price")
    private BigDecimal price;

    public TransactionRequest(String symbol, BigDecimal shares, BigDecimal price) {
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true; // Same reference
        }
        if (!(o instanceof TransactionRequest)) {
            return false; // Not an instance of TransactionRequest
        }
        TransactionRequest other = (TransactionRequest) o;
        return symbol.equals(other.symbol) &&
                shares.equals(other.shares) &&
                price.compareTo(other.price) == 0;
    }
}
