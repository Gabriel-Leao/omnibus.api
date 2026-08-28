package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordConfirmable;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches.PasswordMatches;
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
public record ResetPasswordRequest(
    @NotBlank(message = "Password can't be empty")
        @Size(
            max = 72,
            min = 8,
            message = "Password must have a length between 8 and 72 characters")
        String password,
    @NotBlank(message = "confirm password can't be empty") String confirmPassword)
    implements PasswordConfirmable {}
