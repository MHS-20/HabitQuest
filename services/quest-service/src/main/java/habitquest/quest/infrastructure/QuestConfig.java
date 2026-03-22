package habitquest.quest.infrastructure;

import habitquest.quest.domain.factory.QuestFactory;
import habitquest.quest.domain.factory.UUIDGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestConfig {

  @Bean
  public QuestFactory questFactory() {
    return new QuestFactory(new UUIDGenerator());
  }
}
