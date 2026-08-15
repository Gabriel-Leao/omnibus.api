package br.com.leao.gabriel.omnibus.adapter.in.web.controller;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.LoginRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.request.RegisterCustomerRequest;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.RegistrationResponse;
import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.TokenResponse;
import br.com.leao.gabriel.omnibus.domain.port.in.LoginUseCase;
import br.com.leao.gabriel.omnibus.domain.port.in.RegisterCustomerUseCase;
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
public class AuthController {

  private final RegisterCustomerUseCase registerCustomerUseCase;
  private final LoginUseCase loginUseCase;

  /**
   * Authenticates a customer or staff member and issues a JWT access token.
   *
   * @param request the login credentials
   * @return {@code 200 OK} with the issued access token
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    String token = loginUseCase.execute(request.email(), request.password());
    return ResponseEntity.ok(new TokenResponse(token));
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
}