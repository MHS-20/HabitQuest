package habitquest.avatar.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.avatar.application.AvatarRepository;
import habitquest.avatar.domain.avatar.Experience;
import habitquest.avatar.domain.avatar.Level;
import habitquest.avatar.domain.events.Dead;
import habitquest.avatar.domain.events.LevelUpped;
import habitquest.avatar.domain.events.SkillPointAssigned;
import habitquest.avatar.domain.stats.Defense;
import habitquest.avatar.domain.stats.Intelligence;
import habitquest.avatar.domain.stats.Strength;
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
@DisplayName("AvatarNotifierImpl")
public class AvatarNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private AvatarNotifierImpl notifier;
  @MockitoBean private AvatarRepository avatarRepository;

  private static final String TOPIC_LEVEL_UPPED = "avatar.level-upped";
  private static final String TOPIC_DEAD = "avatar.dead";
  private static final String TOPIC_SKILL_POINT = "avatar.skill-point-assigned";

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

  // ── notifyLevelUpped ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("notifyLevelUpped")
  class NotifyLevelUpped {

    @Test
    @DisplayName("publishes a message to avatar.level-upped")
    void shouldPublishToLevelUppedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_LEVEL_UPPED);
      Level level = new Level(5, new Experience(0), new Experience(500));
      notifier.notifyLevelUpped(new LevelUpped(level));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_LEVEL_UPPED);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get("newLevel").asInt()).isEqualTo(5);
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("includes the correct level number in the payload")
    void shouldIncludeCorrectLevelNumber() throws Exception {
      subscribeAndSeekToEnd(TOPIC_LEVEL_UPPED);
      Level level = new Level(10, new Experience(0), new Experience(1000));
      notifier.notifyLevelUpped(new LevelUpped(level));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("newLevel").asInt()).isEqualTo(10);
    }
  }

  // ── notifyDead ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("notifyDead")
  class NotifyDead {

    @Test
    @DisplayName("publishes a message to avatar.dead")
    void shouldPublishToDeadTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_DEAD);
      notifier.notifyDead(new Dead("avatar-42"));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_DEAD);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get("avatarId").asText()).isEqualTo("avatar-42");
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("preserves the avatarId in the payload")
    void shouldPreserveAvatarId() throws Exception {
      subscribeAndSeekToEnd(TOPIC_DEAD);
      notifier.notifyDead(new Dead("special-avatar-id"));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("avatarId").asText()).isEqualTo("special-avatar-id");
    }
  }

  // ── notifySkillPointAssigned ──────────────────────────────────────────────────

  @Nested
  @DisplayName("notifySkillPointAssigned")
  class NotifySkillPointAssigned {

    @Test
    @DisplayName("publishes a Strength assignment to avatar.skill-point-assigned")
    void shouldPublishStrengthAssignment() throws Exception {
      subscribeAndSeekToEnd(TOPIC_SKILL_POINT);
      notifier.notifySkillPointAssigned(new SkillPointAssigned(new Strength(15)));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_SKILL_POINT);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get("statType").asText()).isEqualTo("Strength");
      assertThat(node.get("newValue").asInt()).isEqualTo(15);
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("publishes a Defense assignment with the correct stat type")
    void shouldPublishDefenseAssignment() throws Exception {
      subscribeAndSeekToEnd(TOPIC_SKILL_POINT);
      notifier.notifySkillPointAssigned(new SkillPointAssigned(new Defense(8)));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("statType").asText()).isEqualTo("Defense");
      assertThat(node.get("newValue").asInt()).isEqualTo(8);
    }

    @Test
    @DisplayName("publishes an Intelligence assignment with the correct stat type")
    void shouldPublishIntelligenceAssignment() throws Exception {
      subscribeAndSeekToEnd(TOPIC_SKILL_POINT);
      notifier.notifySkillPointAssigned(new SkillPointAssigned(new Intelligence(20)));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("statType").asText()).isEqualTo("Intelligence");
      assertThat(node.get("newValue").asInt()).isEqualTo(20);
    }
  }
}
