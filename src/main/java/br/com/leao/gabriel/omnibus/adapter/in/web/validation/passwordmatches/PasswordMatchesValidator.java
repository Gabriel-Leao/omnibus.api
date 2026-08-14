package br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validates that two password fields contain the same value.
 */
public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, PasswordConfirmable> {

  /**
   * Checks whether the password and confirmation password match.
   */
  @Override
  public boolean isValid(PasswordConfirmable value, ConstraintValidatorContext context) {
    if (value.password() == null || value.confirmPassword() == null) {
      return true;
    }

    boolean matches = Objects.equals(value.password(), value.confirmPassword());

    if (!matches) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Passwords do not match")
          .addPropertyNode("confirmPassword")
          .addConstraintViolation();
    }

    return matches;
  }
}
