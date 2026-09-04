package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.LoginRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.RegisterCustomerRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.ResendActivationCodeRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.VerifyCodeRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.AccessTokenResponse;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.RegistrationResponse;
import br.com.leao.gabriel.omnibus.application.usecase.ActivateAccountUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.LoginUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.RegisterCustomerUseCase;
import br.com.leao.gabriel.omnibus.application.usecase.SendOtpUseCase;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles authentication and customer registration endpoints.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, registration, and account activation")
public class AuthController {

  private final RegisterCustomerUseCase registerCustomerUseCase;
  private final LoginUseCase loginUseCase;
  private final ActivateAccountUseCase activateAccountUseCase;
  private final SendOtpUseCase sendOtpUseCase;

  /**
   * Authenticates a customer or staff member and issues a JWT access token.
   *
   * @param request the login credentials
   * @return {@code 200 OK} with the issued access token
   */
  @PostMapping("/login")
  @Operation(summary = "Authenticates a user", description = "Returns a JWT access token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Authentication realizada com sucesso"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody LoginRequest request) {
    String accessToken = loginUseCase.execute(request.email(), request.password());
    return ResponseEntity.ok(new AccessTokenResponse(accessToken));
  }

  /**
   * Accepts a registration request. The response is intentionally identical whether or not the
   * email address was already registered, to prevent user enumeration; the actual outcome is
   * communicated exclusively by email.
   *
   * @param requestData the registration data, already validated by Bean Validation
   * @return {@code 202 Accepted} with a generic confirmation message
   */
  @PostMapping("/register")
  @Operation(
      summary = "Requests customer registration",
      description = "Sends an activation code to the provided email address.")
  @ApiResponse(responseCode = "202", description = "Registration request accepted")
  public ResponseEntity<RegistrationResponse> register(
      @Valid @RequestBody RegisterCustomerRequest requestData) {
    registerCustomerUseCase.execute(
        requestData.name(),
        requestData.email(),
        requestData.password(),
        requestData.birthDate(),
        requestData.photoUrl());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(RegistrationResponse.standard());
  }

  /**
   * Activates a customer account using the one-time code sent by email during registration.
   *
   * @param request the email and verification code
   * @return {@code 200 OK} with the issued access token
   */
  @PostMapping("/activate")
  @Operation(
      summary = "Activates an account",
      description = "Validates the code received by email.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Account activated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid or expired code")
  })
  public ResponseEntity<AccessTokenResponse> activate(
      @Valid @RequestBody VerifyCodeRequest request) {
    var accessToken = activateAccountUseCase.execute(request.email(), request.code());
    return ResponseEntity.ok(new AccessTokenResponse(accessToken));
  }

  /**
   * Resends the account activation code to the requested email address.
   *
   * <p>The response is intentionally identical whether or not the email address is associated with
   * a customer, to prevent user enumeration; the actual outcome is communicated exclusively by
   * email.
   *
   * @param request the email address for which the activation code should be resent, already
   *                validated by Bean Validation
   * @return {@code 202 Accepted} with a generic confirmation message
   */
  @PostMapping("/resend-activation")
  @Operation(summary = "Resends the account activation code")
  @ApiResponse(responseCode = "202", description = "Resend request accepted")
  public ResponseEntity<RegistrationResponse> resend(
      @Valid @RequestBody ResendActivationCodeRequest request) {
    sendOtpUseCase.execute(request.email(), OtpType.ACCOUNT_ACTIVATION);
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(RegistrationResponse.standard());
  }
}
