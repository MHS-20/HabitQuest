package habitquest.edge.security;

import habitquest.edge.infrastructure.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final List<String> PUBLIC_PATHS =
      List.of("/auth/register", "/auth/login", "/actuator");

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(request, response); // SecurityConfig decide se bloccare
      return;
    }

    String token = header.substring(7);

    try {
      Claims claims = jwtService.validateAndExtract(token);
      String userId = claims.getSubject();
      String role = claims.get("role", String.class);

      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
      var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

      SecurityContextHolder.getContext().setAuthentication(auth);
      chain.doFilter(request, response);

    } catch (JwtException | IllegalArgumentException e) {
      SecurityContextHolder.clearContext();
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
  }
}
