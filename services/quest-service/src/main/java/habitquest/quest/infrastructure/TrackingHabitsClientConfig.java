package habitquest.quest.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class TrackingHabitsClientConfig {

  @Bean
  RestClient trackingHabitsRestClient(
      RestClient.Builder builder,
      @Value("${tracking.service-uri:http://tracking-service:8085}") String trackingServiceUri) {
    return builder
        .baseUrl(trackingServiceUri)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
