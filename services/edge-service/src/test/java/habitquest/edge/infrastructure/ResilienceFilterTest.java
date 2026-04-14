package habitquest.edge.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import habitquest.edge.application.port.out.EdgeLogger;
import habitquest.edge.infrastructure.inbound.ResilienceFilter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@SuppressWarnings({"checkstyle:VisibilityModifier"})
@ExtendWith(MockitoExtension.class)
class ResilienceFilterTest {

  @Mock CircuitBreakerRegistry circuitBreakerRegistry;
  @Mock RateLimiterRegistry rateLimiterRegistry;
  @Mock EdgeLogger log;
  @Mock WebFilterChain chain;

  ResilienceFilter resilienceFilter;

  private RateLimiter defaultRateLimiter;
  private CircuitBreaker defaultCircuitBreaker;

  @BeforeEach
  void setUp() {
    resilienceFilter = new ResilienceFilter(circuitBreakerRegistry, rateLimiterRegistry, log);
    defaultRateLimiter = RateLimiter.ofDefaults("test");
    defaultCircuitBreaker = CircuitBreaker.ofDefaults("test");
    lenient().when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(defaultRateLimiter);
    lenient()
        .when(circuitBreakerRegistry.circuitBreaker(anyString()))
        .thenReturn(defaultCircuitBreaker);
    lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
  }

  private MockServerWebExchange exchangeForPath(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }

  // ── happy path ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("richiesta normale passa attraverso il filtro senza errori")
  void doFilter_normalRequest_chainsThrough() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/avatars/1");

    resilienceFilter.filter(exchange, chain).block();

    verify(chain).filter(exchange);
    assertNull(exchange.getResponse().getStatusCode());
  }

  // ── rate limiter ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("rate limit superato → 429 TOO_MANY_REQUESTS")
  void doFilter_rateLimitExceeded_returns429() throws Exception {
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofHours(1))
            .timeoutDuration(Duration.ZERO)
            .build();
    RateLimiter exhausted = RateLimiter.of("exhausted", config);
    exhausted.acquirePermission();
    when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(exhausted);
    MockServerWebExchange exchange = exchangeForPath("/api/v1/avatars/1");

    resilienceFilter.filter(exchange, chain).block();
    assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
  }

  // ── circuit breaker ───────────────────────────────────────────────────────

  @Test
  @DisplayName("circuit breaker aperto → 503 SERVICE_UNAVAILABLE")
  void doFilter_circuitBreakerOpen_returns503() throws Exception {
    CircuitBreaker openCb = CircuitBreaker.ofDefaults("open-test");
    openCb.transitionToOpenState();
    when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(openCb);
    MockServerWebExchange exchange = exchangeForPath("/api/v1/guilds/1");

    resilienceFilter.filter(exchange, chain).block();

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
  }

  // ── service name resolution ───────────────────────────────────────────────

  @Test
  @DisplayName("/api/v1/battles → guildCircuitBreaker (stesso servizio)")
  void doFilter_battlesPath_usesGuildCircuitBreaker() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/battles/42");

    resilienceFilter.filter(exchange, chain).block();

    verify(circuitBreakerRegistry).circuitBreaker("guildCircuitBreaker");
  }

  @Test
  @DisplayName("/api/v1/guilds → guildCircuitBreaker")
  void doFilter_guildsPath_usesGuildCircuitBreaker() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/guilds/1");

    resilienceFilter.filter(exchange, chain).block();

    verify(circuitBreakerRegistry).circuitBreaker("guildCircuitBreaker");
  }

  @Test
  @DisplayName("/api/v1/quests → questCircuitBreaker")
  void doFilter_questsPath_usesQuestCircuitBreaker() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/quests/1");

    resilienceFilter.filter(exchange, chain).block();

    verify(circuitBreakerRegistry).circuitBreaker("questCircuitBreaker");
  }

  @Test
  @DisplayName("/api/v1/habits → habitCircuitBreaker")
  void doFilter_habitsPath_usesHabitCircuitBreaker() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/habits/1");

    resilienceFilter.filter(exchange, chain).block();

    verify(circuitBreakerRegistry).circuitBreaker("habitCircuitBreaker");
  }

  @Test
  @DisplayName("path sconosciuto → default circuit breaker")
  void doFilter_unknownPath_usesDefaultCircuitBreaker() throws Exception {
    MockServerWebExchange exchange = exchangeForPath("/api/v1/unknown/1");

    resilienceFilter.filter(exchange, chain).block();

    verify(circuitBreakerRegistry).circuitBreaker("default");
  }
}
