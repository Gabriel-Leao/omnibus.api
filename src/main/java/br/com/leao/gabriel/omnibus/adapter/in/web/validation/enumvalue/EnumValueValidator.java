package br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumvalue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Validates values against the constants of a configured enum.
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

  private Class<? extends Enum<?>> enumClass;
  private String allowedValues;

  /**
   * Initialises the validator with the enum type configured by the constraint.
   *
   * @param constraintAnnotation the validation constraint configuration
   */
  @Override
  public void initialize(EnumValue constraintAnnotation) {
    this.enumClass = constraintAnnotation.enumClass();
    this.allowedValues =
        Arrays.stream(enumClass.getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.joining(", "));
  }

  /**
   * Checks whether the supplied value is a valid enum constant.
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    boolean isValid =
        Arrays.stream(enumClass.getEnumConstants())
            .anyMatch(constant -> constant.name().equals(value));

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Must be one of: " + allowedValues)
          .addConstraintViolation();
    }

    return isValid;
  }
}
