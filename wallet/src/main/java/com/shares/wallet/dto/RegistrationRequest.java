package com.shares.wallet.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class RegistrationRequest {

    @NotBlank(message = "username cant be empty")
    private String username;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password doesn't meet the criteria")
    private String password;

    private String confirmation;

    @AssertTrue(message = "Password and confirmation do not match")
    private boolean isConfirmationValid() {
        return password != null && password.equals(confirmation);
    }

    public RegistrationRequest(String username, String password, String confirmation) {

        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
    }

    public RegistrationRequest() {

    }

    public boolean isEmpty() {
        return username.isEmpty() || password.isEmpty() || confirmation.isEmpty();
    }
}
