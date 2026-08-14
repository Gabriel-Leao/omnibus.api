package br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that an object's password fields match.
 */
@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {

  /**
   * Returns the default validation message.
   */
  String message() default "Passwords do not match";

  /**
   * Returns the validation groups.
   */
  Class<?>[] groups() default {};

  /**
   * Returns the validation payload types.
   */
  Class<? extends Payload>[] payload() default {};
}
