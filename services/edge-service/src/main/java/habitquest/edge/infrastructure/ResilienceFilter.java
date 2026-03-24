package habitquest.edge.infrastructure;

import habitquest.edge.application.EdgeLogger;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.concurrent.TimeUnit;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class ResilienceFilter implements WebFilter {

  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RateLimiterRegistry rateLimiterRegistry;
  private final EdgeLogger log;

  public ResilienceFilter(
      CircuitBreakerRegistry circuitBreakerRegistry,
      RateLimiterRegistry rateLimiterRegistry,
      EdgeLogger log) {
    this.circuitBreakerRegistry = circuitBreakerRegistry;
    this.rateLimiterRegistry = rateLimiterRegistry;
    this.log = log;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String uri = exchange.getRequest().getPath().value();
    String serviceName = resolveServiceName(uri);

    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("default");
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);

    log.info(new FilterRequest(uri, serviceName), "Incoming request");

    if (!rateLimiter.acquirePermission()) {
      log.warn(new FilterRequest(uri, serviceName), "Rate limit exceeded");
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      return exchange.getResponse().setComplete();
    }

    if (!circuitBreaker.tryAcquirePermission()) {
      log.warn(
          new FilterRequest(uri, serviceName), "Circuit breaker open for service: " + serviceName);
      exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
      return exchange.getResponse().setComplete();
    }

    try {
      return chain
          .filter(exchange)
          .doOnSuccess(ignored -> circuitBreaker.onSuccess(0, TimeUnit.NANOSECONDS))
          .doOnError(error -> circuitBreaker.onError(0, TimeUnit.NANOSECONDS, error));
    } catch (RequestNotPermitted e) {
      log.warn(new FilterRequest(uri, serviceName), "Rate limit exceeded");
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      return exchange.getResponse().setComplete();
    } catch (CallNotPermittedException e) {
      log.warn(
          new FilterRequest(uri, serviceName), "Circuit breaker open for service: " + serviceName);
      exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
      return exchange.getResponse().setComplete();
    }
  }

  private String resolveServiceName(String uri) {
    if (uri.startsWith("/api/v1/avatars")) {
      return "avatarCircuitBreaker";
    }
    if (uri.startsWith("/api/v1/guilds")) {
      return "guildCircuitBreaker";
    }
    if (uri.startsWith("/api/v1/battles")) {
      return "guildCircuitBreaker";
    }
    if (uri.startsWith("/api/v1/quests")) {
      return "questCircuitBreaker";
    }
    if (uri.startsWith("/api/v1/habits")) {
      return "habitCircuitBreaker";
    }
    return "default";
  }

  public record FilterRequest(String uri, String serviceName) {}
}
