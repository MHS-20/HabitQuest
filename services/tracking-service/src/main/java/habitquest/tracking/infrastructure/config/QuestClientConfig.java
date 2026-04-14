package habitquest.tracking.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class QuestClientConfig {

  @Bean
  RestClient questRestClient(
      RestClient.Builder builder,
      @Value("${quest.service-uri:http://quest-service:8084}") String questServiceUri) {
    return builder
        .baseUrl(questServiceUri)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
