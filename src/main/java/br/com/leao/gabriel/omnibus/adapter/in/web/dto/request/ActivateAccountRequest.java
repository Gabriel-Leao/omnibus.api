package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for confirming an account activation code.
 */
public record ActivateAccountRequest(
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "^\\d{6}$", message = "Code must be 6 digits") String code) {}
