package habitquest.edge.infrastructure;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ResilienceFilter implements Filter {

  private final CircuitBreakerRegistry circuitBreakerRegistry;
  private final RateLimiterRegistry rateLimiterRegistry;

  public ResilienceFilter(
      CircuitBreakerRegistry circuitBreakerRegistry, RateLimiterRegistry rateLimiterRegistry) {
    this.circuitBreakerRegistry = circuitBreakerRegistry;
    this.rateLimiterRegistry = rateLimiterRegistry;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpReq = (HttpServletRequest) req;
    HttpServletResponse httpRes = (HttpServletResponse) res;
    String uri = httpReq.getRequestURI();
    String serviceName = resolveServiceName(uri);

    RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("default");
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);

    try {
      Runnable decorated =
          RateLimiter.decorateRunnable(
              rateLimiter,
              CircuitBreaker.decorateRunnable(
                  circuitBreaker,
                  () -> {
                    try {
                      chain.doFilter(req, res);
                    } catch (IOException | ServletException ex) {
                      throw new RuntimeException(ex);
                    }
                  }));

      decorated.run();
    } catch (RequestNotPermitted e) {
      httpRes.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    } catch (CallNotPermittedException e) {
      httpRes.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
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
}
