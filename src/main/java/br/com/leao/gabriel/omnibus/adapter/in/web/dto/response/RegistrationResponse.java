package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic registration confirmation, deliberately uninformative about account existence.
 *
 * @param message the generic confirmation message
 */
@Schema(description = "Generic confirmation for a request that sends an email")
public record RegistrationResponse(
    @Schema(
            description = "Mensagem genérica, sem revelar a existência da conta",
            example = "If the provided email is valid, further instructions have been sent to it.")
        String message) {

  /**
   * Creates the standard successful registration response.
   *
   * @return a successful registration response
   */
  public static RegistrationResponse standard() {
    return new RegistrationResponse(
        "If the provided email is valid, further instructions have been sent to it.");
  }
}
