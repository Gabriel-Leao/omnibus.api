package br.com.leao.gabriel.omnibus.identity.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload carrying a signed JWT password reset token.
 *
 * @param passwordResetToken the signed password reset token
 */
@Schema(description = "Token JWT temporário para redefinir a senha")
public record PasswordResetTokenResponse(
    @Schema(description = "Token de recuperação", example = "eyJhbGciOiJIUzI1NiJ9...")
        String passwordResetToken) {}
