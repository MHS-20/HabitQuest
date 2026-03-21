package habitquest.notification.infrastructure.consumers;

import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class SharedKafkaContainer {

  public static final KafkaContainer INSTANCE =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  static {
    INSTANCE.start();
  }
}
