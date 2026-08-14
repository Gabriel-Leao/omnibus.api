package br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a birth date meets a minimum age requirement.
 */
@Documented
@Constraint(validatedBy = MinimumAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinimumAge {

  /**
   * Returns the minimum age required by the validation.
   */
  int value() default 18;

  /**
   * Returns the default validation message.
   */
  String message() default "User must be at least {value} years old";

  /**
   * Returns the validation groups.
   */
  Class<?>[] groups() default {};

  /**
   * Returns the validation payload types.
   */
  Class<? extends Payload>[] payload() default {};
}
