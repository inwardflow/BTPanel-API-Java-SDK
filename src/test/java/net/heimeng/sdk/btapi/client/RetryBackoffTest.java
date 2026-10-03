package net.heimeng.sdk.btapi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.heimeng.sdk.btapi.config.BtSdkConfig;

@DisplayName("RetryPolicy backoff tests")
class RetryBackoffTest {

  private static final BtSdkConfig CONFIG =
      BtSdkConfig.builder()
          .baseUrl("http://localhost:8888")
          .apiKey("test-api-key")
          .retryInterval(Duration.ofMillis(100))
          .maxRetryInterval(Duration.ofMillis(1000))
          .build();

  @Test
  @DisplayName("delay should grow exponentially with jitter between half and full window")
  void exponentialBackoffWithEqualJitter() {
    RetryPolicy lowest = new RetryPolicy(CONFIG, () -> 0.0);
    RetryPolicy highest = new RetryPolicy(CONFIG, () -> 0.999999);

    assertEquals(Duration.ofMillis(50), lowest.backoffDelay(0, Optional.empty()));
    assertEquals(Duration.ofMillis(99), highest.backoffDelay(0, Optional.empty()));
    assertEquals(Duration.ofMillis(100), lowest.backoffDelay(1, Optional.empty()));
    assertEquals(Duration.ofMillis(200), lowest.backoffDelay(2, Optional.empty()));
  }

  @Test
  @DisplayName("delay should never exceed maxRetryInterval, even for large attempts")
  void backoffIsCapped() {
    RetryPolicy highest = new RetryPolicy(CONFIG, () -> 0.999999);

    for (int attempt = 0; attempt < 64; attempt++) {
      Duration delay = highest.backoffDelay(attempt, Optional.empty());
      assertTrue(delay.compareTo(Duration.ofMillis(1000)) <= 0, "attempt " + attempt);
      assertTrue(!delay.isNegative(), "attempt " + attempt);
    }
  }

  @Test
  @DisplayName("Retry-After should be honoured but capped by maxRetryInterval")
  void retryAfterIsHonouredAndCapped() {
    RetryPolicy lowest = new RetryPolicy(CONFIG, () -> 0.0);

    assertEquals(
        Duration.ofMillis(800), lowest.backoffDelay(0, Optional.of(Duration.ofMillis(800))));
    assertEquals(
        Duration.ofMillis(1000), lowest.backoffDelay(0, Optional.of(Duration.ofSeconds(120))));
  }

  @Test
  @DisplayName("Retry-After parsing should support seconds and HTTP-date")
  void parsesRetryAfter() {
    Instant now = Instant.parse("2026-10-02T12:00:00Z");

    assertEquals(Optional.of(Duration.ofSeconds(5)), RetryPolicy.parseRetryAfter(" 5 ", now));
    assertEquals(
        Optional.of(Duration.ofSeconds(30)),
        RetryPolicy.parseRetryAfter("Fri, 02 Oct 2026 12:00:30 GMT", now));
    assertEquals(
        Optional.of(Duration.ZERO),
        RetryPolicy.parseRetryAfter("Fri, 02 Oct 2026 11:00:00 GMT", now));
    assertEquals(Optional.empty(), RetryPolicy.parseRetryAfter("-1", now));
    assertEquals(Optional.empty(), RetryPolicy.parseRetryAfter("soon", now));
    assertEquals(Optional.empty(), RetryPolicy.parseRetryAfter(null, now));
  }
}
