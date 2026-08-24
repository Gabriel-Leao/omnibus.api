package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for resending an OTP code with email.
 */
public record ResendActivationCodeRequest(@NotBlank @Email String email) {

}
