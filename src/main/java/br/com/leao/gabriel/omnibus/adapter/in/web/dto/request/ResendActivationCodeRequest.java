package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for resending an OTP code with email.
 *
 * @param email the customer's email address
 */
@Schema(description = "Request to resend the account activation code")
public record ResendActivationCodeRequest(
    @Schema(description = "Account Email address", example = "user@example.com") @NotBlank @Email
        String email) {}
