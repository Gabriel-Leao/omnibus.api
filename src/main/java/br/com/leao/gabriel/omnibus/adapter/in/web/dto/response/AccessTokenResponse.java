package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

/**
 * Response payload carrying a signed JWT access token.
 *
 * @param accessToken the signed access token
 */
public record AccessTokenResponse(String accessToken) {}
