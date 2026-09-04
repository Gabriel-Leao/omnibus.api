package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload carrying a signed JWT password reset token.
 *
 * @param passwordResetToken the signed password reset token
 */
@Schema(description = "Temporary JWT token used to reset the password")
public record PasswordResetTokenResponse(
    @Schema(description = "Password reset token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String passwordResetToken) {}
