package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.EmailRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.ResetPasswordRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.VerifyCodeRequest;
import br.com.leao.gabriel.omnibus.application.usecase.ResetPasswordUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.SendOtpUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.VerifyPasswordResetUseCase;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

  @Mock private SendOtpUseCase sendOtpUseCase;
  @Mock private VerifyPasswordResetUseCase verifyPasswordResetUseCase;
  @Mock private ResetPasswordUseCase resetPasswordUseCase;
  @InjectMocks private PasswordResetController controller;

  @Test
  void shouldRequestPasswordReset() {
    var response = controller.requestPasswordReset(new EmailRequest("user@example.com"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    verify(sendOtpUseCase).execute("user@example.com", OtpType.PASSWORD_RESET);
  }

  @Test
  void shouldReturnResetTokenAfterVerification() {
    when(verifyPasswordResetUseCase.execute("user@example.com", "123456"))
        .thenReturn("reset-token");

    var response =
        controller.verifyPasswordResetOtp(new VerifyCodeRequest("user@example.com", "123456"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().passwordResetToken()).isEqualTo("reset-token");
  }

  @Test
  void shouldResetPasswordForAuthenticatedUser() {
    UUID userId = UUID.randomUUID();
    var response =
        controller.resetPassword(
            userId.toString(), new ResetPasswordRequest("NovaSenha@123", "NovaSenha@123"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(resetPasswordUseCase).execute(userId, "NovaSenha@123");
  }
}
