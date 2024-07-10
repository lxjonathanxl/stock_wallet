package com.api.notificationApi.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "email cant be empty")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "username cant be empty")
    private String username;

    @NotBlank(message = "action field empty")
    private String action;

    @NotNull(message = "Shares field empty")
    @Digits(integer = 10, fraction = 2, message = "Invalid value for shares")
    private BigDecimal quant;

    @NotBlank(message = "symbol field empty")
    private String name;

    private String date;

    @NotNull(message = "price field empty")
    @Digits(integer = Integer.MAX_VALUE, fraction = 2, message = "Invalid value for price")
    private BigDecimal price;

}