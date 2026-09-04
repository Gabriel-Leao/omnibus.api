package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.LoginRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.ResendActivationCodeRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.VerifyCodeRequest;
import br.com.leao.gabriel.omnibus.application.usecase.ActivateAccountUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.LoginUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.RegisterCustomerUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.SendOtpUseCase;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private RegisterCustomerUseCase registerCustomerUseCase;
  @Mock private LoginUseCase loginUseCase;
  @Mock private ActivateAccountUseCase activateAccountUseCase;
  @Mock private SendOtpUseCase sendOtpUseCase;
  @InjectMocks private AuthController controller;

  @Test
  void shouldReturnAccessTokenWhenLoginSucceeds() {
    when(loginUseCase.execute("user@example.com", "Senha@123")).thenReturn("access-token");

    var response = controller.login(new LoginRequest("user@example.com", "Senha@123"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().accessToken()).isEqualTo("access-token");
  }

  @Test
  void shouldRegisterCustomerAndReturnAccepted() {
    var request =
        new br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.RegisterCustomerRequest(
            "Maria Silva",
            "maria@example.com",
            "Senha@123",
            "Senha@123",
            LocalDate.of(1990, 1, 1),
            null);

    var response = controller.register(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    verify(registerCustomerUseCase)
        .execute("Maria Silva", "maria@example.com", "Senha@123", LocalDate.of(1990, 1, 1), null);
  }

  @Test
  void shouldActivateAccountAndReturnAccessToken() {
    when(activateAccountUseCase.execute("user@example.com", "123456")).thenReturn("access-token");

    var response = controller.activate(new VerifyCodeRequest("user@example.com", "123456"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().accessToken()).isEqualTo("access-token");
  }

  @Test
  void shouldResendActivationCodeAndReturnAccepted() {
    var response = controller.resend(new ResendActivationCodeRequest("user@example.com"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    verify(sendOtpUseCase).execute("user@example.com", OtpType.ACCOUNT_ACTIVATION);
  }
}
