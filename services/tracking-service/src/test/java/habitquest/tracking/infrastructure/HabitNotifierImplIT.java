package habitquest.tracking.infrastructure;

import static habitquest.tracking.HabitFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.infrastructure.outbound.HabitNotifierImpl;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@DisplayName("HabitNotifierImpl")
public class HabitNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private HabitNotifierImpl notifier;

  private static final String TOPIC_HABIT_DELETED = "habit.deleted";
  private static final String TOPIC_HABIT_ATTENDED = "habit.attended";
  private static final String TOPIC_HABIT_NOT_ATTENDED = "habit.not-attended";

  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);
  private KafkaConsumer<String, String> consumer;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void createConsumer() {
    consumer =
        new KafkaConsumer<>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "habit-test-group-" + System.nanoTime(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
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
      consumer.poll(Duration.ofMillis(100));
      if (System.currentTimeMillis() > deadline) {
        throw new AssertionError("Timeout assignment");
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

  @Nested
  @DisplayName("notifyHabitDeleted")
  class NotifyHabitDeleted {
    @Test
    @DisplayName("publishes a message to habit.deleted")
    void shouldPublishToDeletedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_HABIT_DELETED);
      notifier.notifyHabitDeleted(new HabitDeleted(new Id<>(SPECIAL_HABIT_ID), AVATAR_ID));

      ConsumerRecord<String, String> record = pollOne();
      var node = objectMapper.readTree(record.value());

      assertThat(record.topic()).isEqualTo(TOPIC_HABIT_DELETED);
      assertThat(node.get(FIELD_HABIT_ID).asText()).isEqualTo(SPECIAL_HABIT_ID);
      assertThat(node.has(FIELD_OCCURRED_ON)).isTrue();
    }
  }

  @Nested
  @DisplayName("notifyHabitAttended")
  class NotifyHabitAttended {
    @Test
    @DisplayName("publishes a message to habit.attended")
    void shouldPublishToAttendedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_HABIT_ATTENDED);
      notifier.notifyHabitAttended(new HabitAttended(hydrateHabit(), AVATAR_ID));

      ConsumerRecord<String, String> record = pollOne();
      var node = objectMapper.readTree(record.value());

      assertThat(record.topic()).isEqualTo(TOPIC_HABIT_ATTENDED);
      assertThat(node.get(FIELD_HABIT_ID).asText()).isEqualTo(HABIT_ID.value());
    }
  }

  @Nested
  @DisplayName("notifyHabitNotAttended")
  class NotifyHabitNotAttended {
    @Test
    @DisplayName("publishes a message to habit.not-attended")
    void shouldPublishToNotAttendedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_HABIT_NOT_ATTENDED);
      notifier.notifyHabitNotAttended(
          new HabitNotAttended(hydrateHabit(), new Id<>(SPECIAL_AVATAR_ID)));

      ConsumerRecord<String, String> record = pollOne();
      var node = objectMapper.readTree(record.value());

      assertThat(record.topic()).isEqualTo(TOPIC_HABIT_NOT_ATTENDED);
      assertThat(node.get(FIELD_AVATAR_ID).asText()).isEqualTo(SPECIAL_AVATAR_ID);
    }
  }
}
