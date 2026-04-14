package habitquest.tracking.infrastructure.config;

import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.factory.UUIDGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HabitConfig {

  @Bean
  public HabitFactory habitFactory() {
    return new HabitFactory(new UUIDGenerator());
  }
}
