package br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimunAge;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

  private int minimumAge;

  @Override
  public void initialize(MinimumAge constraintAnnotation) {
    this.minimumAge = constraintAnnotation.value();
  }

  @Override
  public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
    if (birthDate == null) {
      return true;
    }
    return Period.between(birthDate, LocalDate.now()).getYears() >= minimumAge;
  }
}
