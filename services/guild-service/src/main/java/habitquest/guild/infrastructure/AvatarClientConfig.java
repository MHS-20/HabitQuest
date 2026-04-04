package habitquest.guild.infrastructure;

import habitquest.guild.application.GuildLogger;
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
      @Value("${AVATAR_SERVICE_URI:${habitquest.avatar-service-uri:http://localhost:8081}}")
          String avatarServiceUri,
      GuildLogger log) {

    log.info(avatarServiceUri, "Resolved avatar service URI for guild-service");

    return builder
        .baseUrl(avatarServiceUri)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
