package br.com.leao.gabriel.omnibus.adapter.out;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpIssuanceRateLimiterPort;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Tracks token issuance counts using Redis, leveraging atomic {@code INCR} plus a rolling
 * expiration to implement a 24-hour sliding-window rate limit without a cleanup job.
 */
@Component
public class RedisOtpIssuanceRateLimiterAdapter implements OtpIssuanceRateLimiterPort {

  private static final Duration WINDOW = Duration.ofHours(24);

  private final StringRedisTemplate redisTemplate;

  /**
   * Creates the adapter with the Redis template used to store issuance counters.
   */
  public RedisOtpIssuanceRateLimiterAdapter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public long incrementAndGet(String userId, OtpType type) {
    String key = "otp-rate-limit:%s:%s".formatted(userId, type.name());
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, WINDOW);
    }
    return count == null ? 0 : count;
  }
}
