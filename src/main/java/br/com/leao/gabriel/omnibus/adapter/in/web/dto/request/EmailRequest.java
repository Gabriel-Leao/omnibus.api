package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing an email address.
 *
 * @param email the email address
 */
@Schema(description = "Request containing an email address")
public record EmailRequest(
    @Schema(description = "Account Email address", example = "user@example.com") @NotBlank @Email
        String email) {}
