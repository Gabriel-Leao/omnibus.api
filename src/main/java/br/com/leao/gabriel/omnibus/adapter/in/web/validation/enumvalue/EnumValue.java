package br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumvalue;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a value matches one of the constants of a specified enum.
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

  /**
   * Returns the enum type whose constants are accepted.
   */
  Class<? extends Enum<?>> enumClass();

  /**
   * Returns the default validation message.
   */
  String message() default "Value must be one of the accepted options";

  /**
   * Returns the validation groups.
   */
  Class<?>[] groups() default {};

  /**
   * Returns the validation payload types.
   */
  Class<? extends Payload>[] payload() default {};
}
