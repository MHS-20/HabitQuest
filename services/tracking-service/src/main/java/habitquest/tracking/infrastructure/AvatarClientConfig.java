package habitquest.tracking.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class AvatarClientConfig {

  @Bean
  RestClient avatarRestClient(
      RestClient.Builder builder,
      @Value("${avatar.service-uri:http://avatar-service:8081}") String avatarServiceUri) {
    return builder
        .baseUrl(avatarServiceUri)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
