package habitquest.quest.infrastructure;

import static habitquest.quest.QuestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestLeft;
import habitquest.quest.domain.events.QuestNotCompleted;
import habitquest.quest.infrastructure.outbound.QuestNotifierImpl;
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
@DisplayName("QuestNotifierImpl Integration Tests")
public class QuestNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private QuestNotifierImpl notifier;

  private static final String TOPIC_CREATED = "quest.created";
  private static final String TOPIC_COMPLETED = "quest.completed";
  private static final String TOPIC_NOT_COMPLETED = "quest.not-completed";
  private static final String TOPIC_JOINED = "quest.joined";
  private static final String TOPIC_LEFT = "quest.left";

  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);

  private KafkaConsumer<String, String> consumer;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void createConsumer() {
    consumer =
        new KafkaConsumer<>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "quest-test-group-" + System.nanoTime(),
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
  @DisplayName("notifyQuestCreated")
  class NotifyQuestCreated {
    @Test
    void shouldPublishQuestCreated() throws Exception {
      subscribeAndSeekToEnd(TOPIC_CREATED);
      notifier.notifyQuestCreated(new QuestCreated(fullQuest()));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(FIELD_QUEST_ID).asText()).isEqualTo(QUEST_ID.value());
      assertThat(node.get(FIELD_QUEST_NAME).asText()).isEqualTo(QUEST_NAME);
    }
  }

  @Nested
  @DisplayName("notifyQuestCompleted")
  class NotifyQuestCompleted {
    @Test
    void shouldPublishQuestCompleted() throws Exception {
      subscribeAndSeekToEnd(TOPIC_COMPLETED);
      notifier.notifyQuestCompleted(new QuestCompleted(fullQuest(), AVATAR_ID_1));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(FIELD_QUEST_ID).asText()).isEqualTo(QUEST_ID.value());
      assertThat(node.get(FIELD_AVATAR_ID).asText()).isEqualTo(AVATAR_1);
    }
  }

  @Nested
  @DisplayName("notifyQuestNotCompleted")
  class NotifyQuestNotCompleted {
    @Test
    void shouldPublishQuestNotCompleted() throws Exception {
      subscribeAndSeekToEnd(TOPIC_NOT_COMPLETED);
      notifier.notifyQuestNotCompleted(new QuestNotCompleted());

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.has(FIELD_OCCURRED_ON)).isTrue();
    }
  }

  @Nested
  @DisplayName("notifyQuestJoined")
  class NotifyQuestJoined {
    @Test
    void shouldPublishQuestJoined() throws Exception {
      subscribeAndSeekToEnd(TOPIC_JOINED);
      notifier.notifyQuestJoined(new QuestJoined(fullQuest(), AVATAR_ID_1));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(FIELD_QUEST_ID).asText()).isEqualTo(QUEST_ID.value());
      assertThat(node.get(FIELD_AVATAR_ID).asText()).isEqualTo(AVATAR_1);
    }
  }

  @Nested
  @DisplayName("notifyQuestLeft")
  class NotifyQuestLeft {
    @Test
    void shouldPublishQuestLeft() throws Exception {
      subscribeAndSeekToEnd(TOPIC_LEFT);
      notifier.notifyQuestLeft(new QuestLeft(fullQuest(), AVATAR_ID_1));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(FIELD_QUEST_ID).asText()).isEqualTo(QUEST_ID.value());
      assertThat(node.get(FIELD_AVATAR_ID).asText()).isEqualTo(AVATAR_1);
    }
  }
}
