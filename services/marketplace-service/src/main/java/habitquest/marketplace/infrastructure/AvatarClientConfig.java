package habitquest.marketplace.infrastructure;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AvatarClientConfig {

  @Value("${avatar.service-uri:http://localhost:8080}")
  private String avatarServiceUri;

  @Bean
  RestClient avatarRestClient(RestClient.Builder builder) {
    return builder
        .baseUrl(avatarServiceUri)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .requestFactory(clientHttpRequestFactory())
        .build();
  }

  private ClientHttpRequestFactory clientHttpRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(3));
    factory.setReadTimeout(Duration.ofSeconds(5));
    return factory;
  }
}
