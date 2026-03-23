package habitquest.marketplace.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.marketplace.application.MarketplaceLogger;
import habitquest.marketplace.application.MarketplaceRepository;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
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
import org.mockito.Mock;
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

  public static final String MARKETPLACE_FIELD = "marketplaceId";
  public static final String ITEM_NAME = "itemName";
  public static final String AVATAR_ID = "avatarId";
  public static final String OCCURRED_ON = "occurredOn";
  public static final String MARKET_1 = "market-1";

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

  // ── notifyItemBought ----------------------------------------------------
  @Nested
  @DisplayName("notifyItemBought")
  class NotifyItemBought {

    @Test
    @DisplayName("publishes a message to marketplace.item-bought")
    void shouldPublishToItemBoughtTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(
          new ItemBought(new Id<>(MARKET_1), "Iron Sword", new Id<>("avatar-42")));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_ITEM_BOUGHT);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get(MARKETPLACE_FIELD).asText()).isEqualTo(MARKET_1);
      assertThat(node.get(ITEM_NAME).asText()).isEqualTo("Iron Sword");
      assertThat(node.get(AVATAR_ID).asText()).isEqualTo("avatar-42");
      assertThat(node.has(OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves all fields in the item-bought payload")
    void shouldPreserveAllFields() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(
          new ItemBought(new Id<>("market-99"), "Dragon Shield", new Id<>("hero-7")));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(MARKETPLACE_FIELD).asText()).isEqualTo("market-99");
      assertThat(node.get(ITEM_NAME).asText()).isEqualTo("Dragon Shield");
      assertThat(node.get(AVATAR_ID).asText()).isEqualTo("hero-7");
    }

    @Test
    @DisplayName("each purchase event has its own occurredOn timestamp")
    void shouldHaveTimestamp() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_BOUGHT);
      notifier.notifyItemBought(
          new ItemBought(new Id<>(MARKET_1), "Mana Potion", new Id<>("avatar-1")));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(OCCURRED_ON).asText()).isNotBlank();
    }
  }

  // ── notifyItemSold ------------------------------------------------------
  @Nested
  @DisplayName("notifyItemSold")
  class NotifyItemSold {

    @Test
    @DisplayName("publishes a message to marketplace.item-sold")
    void shouldPublishToItemSoldTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(
          new ItemSold(new Id<>("market-2"), "Old Armor", new Id<>("avatar-10")));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_ITEM_SOLD);
      var node = objectMapper.readTree(record.value());
      assertThat(node.get(MARKETPLACE_FIELD).asText()).isEqualTo("market-2");
      assertThat(node.get(ITEM_NAME).asText()).isEqualTo("Old Armor");
      assertThat(node.get(AVATAR_ID).asText()).isEqualTo("avatar-10");
      assertThat(node.has(OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves all fields in the item-sold payload")
    void shouldPreserveAllFields() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(
          new ItemSold(new Id<>("market-5"), "Rusty Sword", new Id<>("warrior-3")));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(MARKETPLACE_FIELD).asText()).isEqualTo("market-5");
      assertThat(node.get(ITEM_NAME).asText()).isEqualTo("Rusty Sword");
      assertThat(node.get(AVATAR_ID).asText()).isEqualTo("warrior-3");
    }

    @Test
    @DisplayName("each sale event has its own occurredOn timestamp")
    void shouldHaveTimestamp() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ITEM_SOLD);
      notifier.notifyItemSold(
          new ItemSold(new Id<>(MARKET_1), "Health Potion", new Id<>("avatar-2")));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(OCCURRED_ON).asText()).isNotBlank();
    }
  }
}
