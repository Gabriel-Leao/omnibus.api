package br.com.leao.gabriel.omnibus.adapter.in.web.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage.MinimumAge;
import br.com.leao.gabriel.omnibus.adapter.in.web.validation.minimumage.MinimumAgeValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinimumAgeValidatorTest {

  private MinimumAgeValidator validator;

  @Mock private MinimumAge annotation;

  @Mock private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new MinimumAgeValidator();

    when(annotation.value()).thenReturn(18);

    validator.initialize(annotation);
  }

  @Test
  void shouldReturnTrueWhenUserIsOlderThanMinimumAge() {
    var birthDate = LocalDate.now().minusYears(20);

    var result = validator.isValid(birthDate, context);

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrueWhenUserIsExactlyMinimumAge() {
    var birthDate = LocalDate.now().minusYears(18);

    var result = validator.isValid(birthDate, context);

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalseWhenUserIsYoungerThanMinimumAge() {
    var birthDate = LocalDate.now().minusYears(17);

    var result = validator.isValid(birthDate, context);

    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrueWhenBirthDateIsNull() {
    var result = validator.isValid(null, context);

    assertThat(result).isTrue();
  }
}
