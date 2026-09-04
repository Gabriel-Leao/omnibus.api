package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for confirming an account activation code.
 *
 * @param email the customer's email address
 * @param code  the six-digit activation code
 */
@Schema(description = "OTP code received by email")
public record VerifyCodeRequest(
    @Schema(description = "Account Email address", example = "user@example.com") @NotBlank @Email
        String email,
    @Schema(description = "Six-digit OTP code", example = "123456", pattern = "^\\d{6}$")
        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits")
        String code) {}
