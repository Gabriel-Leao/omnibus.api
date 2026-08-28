package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.exception.DailyTokenLimitExceededException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpIssuanceRateLimiterPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Centralises verification code issuance.
 *
 * <p>Responsible for enforcing issuance limits, revoking previous active tokens, generating and
 * hashing the code, persisting the token, and returning the plain-text code to the caller.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class VerificationOtpIssuer {

  private static final int MAX_TOKENS_PER_DAY = 3;
  private static final long DEFAULT_EXPIRATION_MINUTES = 15;

  private final OtpGeneratorPort otpGenerator;
  private final UserTokenRepositoryPort userTokenRepository;
  private final OtpIssuanceRateLimiterPort rateLimiter;

  /**
   * Issues a verification code using the default expiration time.
   *
   * @param userId the user's identifier
   *
   * @param type   the type of token to issue
   *
   * @return the generated plain-text verification code
   */
  public String issue(String userId, OtpType type) {
    return issue(userId, type, DEFAULT_EXPIRATION_MINUTES);
  }

  /**
   * Issues a verification code using a custom expiration time.
   *
   * @param userId            the user's identifier
   *
   * @param type              the type of token to issue
   *
   * @param expirationMinutes the number of minutes until the token expires
   *
   * @return the generated plain-text verification code
   */
  public String issue(String userId, OtpType type, long expirationMinutes) {

    return issueInternal(userId, type, null, expirationMinutes);
  }

  /**
   * Issues a verification code for an email change.
   *
   * @param userId      the user's identifier
   *
   * @param targetEmail the new email address
   *
   * @return the generated plain-text verification code
   */
  public String issueForEmailChange(String userId, String targetEmail) {

    return issueInternal(userId, OtpType.EMAIL_CHANGE, targetEmail, DEFAULT_EXPIRATION_MINUTES);
  }

  /**
   * Creates, persists, and returns a verification code.
   *
   * @param userId            the user's identifier
   *
   * @param type              the type of token to issue
   *
   * @param targetEmail       the target email address for email-change tokens
   *
   * @param expirationMinutes the number of minutes until the token expires
   *
   * @return the generated plain-text verification code
   */
  protected String issueInternal(
      String userId, OtpType type, String targetEmail, long expirationMinutes) {

    long count = rateLimiter.incrementAndGet(userId, type);

    if (count > MAX_TOKENS_PER_DAY) {
      throw new DailyTokenLimitExceededException();
    }

    var activeToken = userTokenRepository.findActiveByUserId(userId);

    activeToken.ifPresent(token -> userTokenRepository.save(token.revoke()));

    userTokenRepository.flush();

    String code = otpGenerator.generateCode();
    String codeHash = otpGenerator.hash(code);
    Instant expiresAt = Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES);

    UserToken token =
        type == OtpType.EMAIL_CHANGE
            ? UserToken.issueForEmailChange(userId, codeHash, targetEmail, expiresAt)
            : UserToken.issue(userId, codeHash, type, expiresAt);

    userTokenRepository.save(token);

    return code;
  }
}
