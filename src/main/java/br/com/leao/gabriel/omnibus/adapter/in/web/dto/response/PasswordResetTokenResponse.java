package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

/**
 * Response payload carrying a signed JWT password reset token.
 *
 * @param passwordResetToken the signed password reset token
 */
public record PasswordResetTokenResponse(String passwordResetToken) {}
