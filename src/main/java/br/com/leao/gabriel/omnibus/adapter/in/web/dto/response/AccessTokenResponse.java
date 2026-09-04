package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload carrying a signed JWT access token.
 *
 * @param accessToken the signed access token
 */
@Schema(description = "JWT access token")
public record AccessTokenResponse(
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken) {}
