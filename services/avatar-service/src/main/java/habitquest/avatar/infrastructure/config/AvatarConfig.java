package habitquest.avatar.infrastructure.config;

import habitquest.avatar.domain.factory.AvatarFactory;
import habitquest.avatar.domain.factory.UUIDGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AvatarConfig {

  @Bean
  public AvatarFactory avatarFactory() {
    return new AvatarFactory(new UUIDGenerator());
  }
}
