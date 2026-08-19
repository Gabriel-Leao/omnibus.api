package br.com.leao.gabriel.omnibus.adapter.in.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for authenticating with email and password.
 */
public record LoginRequest(
    @NotBlank
    @Email
    String email,
    @NotBlank
    String password) {

}