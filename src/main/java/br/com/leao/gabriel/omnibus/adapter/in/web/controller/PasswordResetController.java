package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.EmailRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.ResetPasswordRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.VerifyCodeRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.PasswordResetTokenResponse;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.RegistrationResponse;
import br.com.leao.gabriel.omnibus.application.usecase.ResetPasswordUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.SendOtpUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.VerifyPasswordResetUseCase;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for password reset operations.
 */
@RestController
@RequestMapping("/password-reset")
@RequiredArgsConstructor
@Tag(name = "Password reset", description = "Password reset flow using OTP")
public class PasswordResetController {

  private final SendOtpUseCase sendOtpUseCase;
  private final VerifyPasswordResetUseCase verifyPasswordResetOtpUseCase;
  private final ResetPasswordUseCase resetPasswordUseCase;

  /**
   * Requests a password reset OTP to be sent to the given email address.
   *
   * <p>The response does not reveal whether an account is associated with the given email address.
   *
   * @param request the request containing the email address associated with the account
   */
  @PostMapping()
  @Operation(
      summary = "Requests a password reset",
      description = "Sends an OTP to the email address.")
  @ApiResponse(responseCode = "202", description = "Password reset request accepted")
  public ResponseEntity<RegistrationResponse> requestPasswordReset(
      @Valid @RequestBody EmailRequest request) {
    sendOtpUseCase.execute(request.email(), OtpType.PASSWORD_RESET);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(RegistrationResponse.standard());
  }

  /**
   * Verifies the password reset OTP and issues a short-lived password reset token.
   *
   * @param request the password reset verification request containing the email and OTP
   * @return a short-lived token authorising the password reset
   */
  @PostMapping("/verify")
  @Operation(
      summary = "Validates the password reset code",
      description = "Returns a temporary token to confirm the new password.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Code validated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid or expired code")
  })
  public ResponseEntity<PasswordResetTokenResponse> verifyPasswordResetOtp(
      @Valid @RequestBody VerifyCodeRequest request) {

    String resetToken = verifyPasswordResetOtpUseCase.execute(request.email(), request.code());

    return ResponseEntity.ok(new PasswordResetTokenResponse(resetToken));
  }

  /**
   * Resets the authenticated user's password.
   *
   * <p>The user must be authenticated with a valid password reset token issued after successfully
   * verifying the password reset code.
   *
   * @param userId  the ID of the user represented by the password reset token
   * @param request the request containing the new password
   */
  @PostMapping("/confirm")
  @Operation(summary = "Confirms the new password")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Password redefinida com sucesso"),
    @ApiResponse(responseCode = "401", description = "Password reset token inválido ou expirado")
  })
  public ResponseEntity<Void> resetPassword(
      @AuthenticationPrincipal String userId, @Valid @RequestBody ResetPasswordRequest request) {

    resetPasswordUseCase.execute(UUID.fromString(userId), request.password());

    return ResponseEntity.noContent().build();
  }
}
