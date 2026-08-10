package br.com.leao.gabriel.omnibus.adapter.in.web.validation.enumValue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

  private Class<? extends Enum<?>> enumClass;
  private String allowedValues;

  @Override
  public void initialize(EnumValue constraintAnnotation) {
    this.enumClass = constraintAnnotation.enumClass();
    this.allowedValues = Arrays.stream(enumClass.getEnumConstants())
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    boolean isValid = Arrays.stream(enumClass.getEnumConstants())
        .anyMatch(constant -> constant.name().equals(value));

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Must be one of: " + allowedValues)
          .addConstraintViolation();
    }

    return isValid;
  }
}