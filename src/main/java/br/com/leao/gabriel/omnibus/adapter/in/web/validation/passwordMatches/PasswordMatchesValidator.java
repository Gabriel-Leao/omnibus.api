package br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordMatches;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, PasswordConfirmable> {

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