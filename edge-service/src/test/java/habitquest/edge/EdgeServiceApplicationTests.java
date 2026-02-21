package habitquest.edge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@EnableAutoConfiguration(
    exclude = {
      SecurityAutoConfiguration.class,
      OAuth2ClientAutoConfiguration.class,
      ReactiveSecurityAutoConfiguration.class,
      ReactiveOAuth2ClientAutoConfiguration.class
    })
class EdgeServiceApplicationTests {

  @Test
  void contextLoads() {}
}
