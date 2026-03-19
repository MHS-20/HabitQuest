package habitquest.edge.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.edge.application.UserRepository;
import habitquest.edge.domain.User;
import habitquest.edge.domain.UserRole;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@DisplayName("UserNotifierImpl")
public class UserNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private UserNotifierImpl notifier;
  @MockitoBean private UserRepository userRepository;

  private static final String TOPIC_USER_REGISTERED = "user.registered";
  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);

  private KafkaConsumer<String, String> consumer;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void createConsumer() {
    consumer =
        new KafkaConsumer<>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG,
                "test-group-" + System.nanoTime(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName()));
  }

  @AfterEach
  void closeConsumer() {
    consumer.close();
  }

  private void subscribeAndSeekToEnd(String topic) {
    consumer.subscribe(Collections.singletonList(topic));
    long deadline = System.currentTimeMillis() + POLL_TIMEOUT.toMillis();
    while (consumer.assignment().isEmpty()) {
      consumer.poll(Duration.ofMillis(200));
      if (System.currentTimeMillis() > deadline) {
        throw new AssertionError("Consumer never got partition assignment for topic: " + topic);
      }
    }
    consumer.seekToEnd(consumer.assignment());
    consumer.assignment().forEach(tp -> consumer.position(tp));
  }

  private ConsumerRecord<String, String> pollOne() {
    long deadline = System.currentTimeMillis() + POLL_TIMEOUT.toMillis();
    while (System.currentTimeMillis() < deadline) {
      var records = consumer.poll(Duration.ofMillis(500));
      if (!records.isEmpty()) {
        return records.iterator().next();
      }
    }
    throw new AssertionError("No message received within timeout");
  }

  // ── notifyUserRegistered ──────────────────────────────────────────────────

  @Nested
  @DisplayName("notifyUserRegistered")
  class NotifyUserRegistered {

    @Test
    @DisplayName("publishes a message to user.registered")
    void shouldPublishToUserRegisteredTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_USER_REGISTERED);
      User user = new User("user-1", "mario@example.com", "hashedpw", UserRole.USER);
      notifier.notifyUserRegistered(user);

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_USER_REGISTERED);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get("userId").asText()).isEqualTo("user-1");
      assertThat(node.get("email").asText()).isEqualTo("mario@example.com");
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("preserves the userId in the payload")
    void shouldPreserveUserId() throws Exception {
      subscribeAndSeekToEnd(TOPIC_USER_REGISTERED);
      User user = new User("special-user-id", "test@example.com", "hashedpw", UserRole.USER);
      notifier.notifyUserRegistered(user);

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("userId").asText()).isEqualTo("special-user-id");
    }

    @Test
    @DisplayName("preserves the email in the payload")
    void shouldPreserveEmail() throws Exception {
      subscribeAndSeekToEnd(TOPIC_USER_REGISTERED);
      User user = new User("user-2", "luigi@example.com", "hashedpw", UserRole.USER);
      notifier.notifyUserRegistered(user);

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("email").asText()).isEqualTo("luigi@example.com");
    }

    @Test
    @DisplayName("does not include the password hash in the payload")
    void shouldNotExposePasswordHash() throws Exception {
      subscribeAndSeekToEnd(TOPIC_USER_REGISTERED);
      User user = new User("user-3", "peach@example.com", "supersecrethashedpw", UserRole.USER);
      notifier.notifyUserRegistered(user);

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.has("passwordHash")).isFalse();
      assertThat(node.toString()).doesNotContain("supersecrethashedpw");
    }
  }
}
