package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;

/**
 * Output port for tracking how many verification codes have been issued to a user within a rolling
 * 24-hour window, independent of how the count is stored.
 */
public interface OtpIssuanceRateLimiterPort {

  /**
   * Increments the issuance counter for the given user/type and returns the new count.
   *
   * @param userId the account requesting a new code
   * @param type   the type of token being issued
   * @return the count after this increment
   */
  long incrementAndGet(String userId, OtpType type);
}
