package habitquest.marketplace.infrastructure;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.marketplace.application.MarketplaceRepository;
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
@DisplayName("MarketplaceNotifierImpl")
public class MarketplaceNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private MarketplaceNotifierImpl notifier;
  @MockitoBean private MarketplaceRepository marketplaceRepository;

  private static final String TOPIC_ITEM_BOUGHT = "marketplace.item-bought";
  private static final String TOPIC_ITEM_SOLD = "marketplace.item-sold";
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

  // ── notifyItemBought ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("notifyItemBought")
  class NotifyItemBought {

    @Test
    @DisplayName("publishes a message to marketplace.item-bought")
    void shouldPublishToItemBoughtTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(itemBought(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_ITEM_BOUGHT);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_KEY_MARKETPLACE_ID).asText()).isEqualTo(MARKETPLACE_1);
      assertThat(node.get(JSON_KEY_ITEM_NAME).asText()).isEqualTo(SWORD_NAME);
      assertThat(node.get(JSON_KEY_AVATAR_ID).asText()).isEqualTo(AVATAR_1);
      assertThat(node.has(JSON_KEY_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves all fields in the item-bought payload")
    void shouldPreserveAllFields() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(itemBought(MARKETPLACE_MP_ID, "Dragon Shield", AVATAR_ID_99));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_KEY_MARKETPLACE_ID).asText()).isEqualTo(MARKETPLACE_2);
      assertThat(node.get(JSON_KEY_ITEM_NAME).asText()).isEqualTo("Dragon Shield");
      assertThat(node.get(JSON_KEY_AVATAR_ID).asText()).isEqualTo(AVATAR_99);
    }

    @Test
    @DisplayName("each purchase event has its own occurredOn timestamp")
    void shouldHaveTimestamp() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(itemBought(MARKETPLACE_ID, "Mana Potion", AVATAR_ID));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_KEY_OCCURRED_ON).asText()).isNotBlank();
    }
  }

  // ── notifyItemSold ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("notifyItemSold")
  class NotifyItemSold {

    @Test
    @DisplayName("publishes a message to marketplace.item-sold")
    void shouldPublishToItemSoldTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(itemSold(UNKNOWN_MARKETPLACE_ID, SHIELD_NAME, AVATAR_ID_99));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_ITEM_SOLD);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_KEY_MARKETPLACE_ID).asText()).isEqualTo(UNKNOWN_MARKETPLACE);
      assertThat(node.get(JSON_KEY_ITEM_NAME).asText()).isEqualTo(SHIELD_NAME);
      assertThat(node.get(JSON_KEY_AVATAR_ID).asText()).isEqualTo(AVATAR_99);
      assertThat(node.has(JSON_KEY_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves all fields in the item-sold payload")
    void shouldPreserveAllFields() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(itemSold(MISSING_MARKETPLACE_ID, "Rusty Sword", AVATAR_ID));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_KEY_MARKETPLACE_ID).asText()).isEqualTo(MISSING_MARKETPLACE);
      assertThat(node.get(JSON_KEY_ITEM_NAME).asText()).isEqualTo("Rusty Sword");
      assertThat(node.get(JSON_KEY_AVATAR_ID).asText()).isEqualTo(AVATAR_1);
    }

    @Test
    @DisplayName("each sale event has its own occurredOn timestamp")
    void shouldHaveTimestamp() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(itemSold(MARKETPLACE_ID, "Health Potion", AVATAR_ID));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_KEY_OCCURRED_ON).asText()).isNotBlank();
    }
  }
}
