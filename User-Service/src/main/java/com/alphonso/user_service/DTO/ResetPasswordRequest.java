package com.alphonso.user_service.DTO;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "OTP is required")
    @Digits(integer = 6, fraction = 0, message = "OTP must be a 6-digit number")
    @Min(value = 100000, message = "OTP must be 6 digits")
    @Max(value = 999999, message = "OTP must be 6 digits")
    private Integer otp;

    @NotBlank(message = "New password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[@#$%^&+=!])(?=\\S+$).{6,20}$",
        message = "Password must be 6–20 characters long, include at least one digit and one special character"
    )
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
