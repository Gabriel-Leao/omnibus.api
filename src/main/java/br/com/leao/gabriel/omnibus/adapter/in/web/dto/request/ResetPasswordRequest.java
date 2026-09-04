package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for completing a password reset.
 *
 * @param password the new password
 *
 * @param confirmPassword the password confirmation
 */
@PasswordMatches
@Schema(description = "New password and confirmation")
public record ResetPasswordRequest(
    @Schema(
            description = "New password",
            example = "NovaPassword@123",
            format = "password",
            minLength = 8)
        @NotBlank(message = "Password can't be empty")
        @Size(
            max = 72,
            min = 8,
            message = "Password must have a length between 8 and 72 characters")
        String password,
    @Schema(
            description = "New password confirmation",
            example = "NovaPassword@123",
            format = "password")
        @NotBlank(message = "confirm password can't be empty")
        String confirmPassword)
    implements PasswordConfirmable {}
