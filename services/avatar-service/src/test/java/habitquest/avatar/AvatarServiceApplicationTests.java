package habitquest.avatar;

import habitquest.avatar.application.AvatarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
class AvatarServiceApplicationTests {

  @Container
  private static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
  }

  public KafkaContainer getKafka() {
    return kafka;
  }

  @MockitoBean private AvatarRepository avatarRepository;

  @Test
  void contextLoads() {}
}
