package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for confirming an account activation code.
 *
 * @param email the customer's email address
 *
 * @param code the six-digit activation code
 */
public record VerifyCodeRequest(
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits") String code) {}
