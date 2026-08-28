package br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

/**
 * Validates that a birth date satisfies a configured minimum age.
 */
public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

  private int minimumAge;

  /**
   * Initialises the validator with the configured minimum age.
   *
   * @param constraintAnnotation the validation constraint configuration
   */
  @Override
  public void initialize(MinimumAge constraintAnnotation) {
    this.minimumAge = constraintAnnotation.value();
  }

  /**
   * Checks whether the supplied birth date satisfies the minimum age.
   */
  @Override
  public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
    if (birthDate == null) {
      return true;
    }
    return Period.between(birthDate, LocalDate.now()).getYears() >= minimumAge;
  }
}
