package habitquest.guild;

import habitquest.guild.application.BattleRepository;
import habitquest.guild.application.GuildRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class GuildServiceApplicationTests {

  @Container
  private static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
  }

  @MockitoBean private GuildRepository guildRepository;

  @MockitoBean private BattleRepository battleRepository;

  public KafkaContainer getKafka() {
    return kafka;
  }

  @Test
  void contextLoads() {}
}
