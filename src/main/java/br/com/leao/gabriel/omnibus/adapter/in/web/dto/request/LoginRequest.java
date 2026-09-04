package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for authenticating with email and password.
 *
 * @param email    the user's email address
 * @param password the user's password
 */
@Schema(description = "Authentication credentials")
public record LoginRequest(
    @Schema(description = "Account Email address", example = "user@example.com") @NotBlank @Email
        String email,
    @Schema(description = "Account password", example = "Password@123", format = "password")
        @NotBlank
        String password) {}
