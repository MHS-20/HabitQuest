package habitquest.guild.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.guild.application.BattleRepository;
import habitquest.guild.application.GuildRepository;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
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
@DisplayName("BattleNotifierImpl")
public class BattleNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  public static final String BATTLE_ID = "battleId";
  public static final String GUILD_ID = "guildId";

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private BattleNotifierImpl notifier;

  @MockitoBean private GuildRepository guildRepository;

  @MockitoBean private BattleRepository battleRepository;

  // ── Topic names (must match application.yml destinations) ───────────────────
  private static final String TOPIC_BATTLE_STARTED = "guild.battle-started";
  private static final String TOPIC_BATTLE_WON = "guild.battle-won";
  private static final String TOPIC_BATTLE_LOST = "guild.battle-lost";

  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);

  private KafkaConsumer<String, String> consumer;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void createConsumer() {
    consumer =
        new KafkaConsumer<>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.nanoTime(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                    StringDeserializer.class.getName()));
  }

  @AfterEach
  void closeConsumer() {
    if (consumer != null) {
      consumer.close();
    }
  }

  // Subscribe and force partition assignment to complete BEFORE publishing
  private void subscribeAndSeekToEnd(String topic) {
    consumer.subscribe(Collections.singletonList(topic));
    long deadline = System.currentTimeMillis() + POLL_TIMEOUT.toMillis();
    while (consumer.assignment().isEmpty()) {
      consumer.poll(Duration.ofMillis(200));
      if (System.currentTimeMillis() > deadline) {
        throw new AssertionError("Consumer never got partition assignment for topic: " + topic);
      }
    }
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

  // ── notifyBattleStarted ---------------------------------------------------
  @Nested
  @DisplayName("notifyBattleStarted")
  class NotifyBattleStarted {
    @Test
    @DisplayName("publishes a message to guild.battle-started")
    void shouldPublishToBattleStartedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_STARTED);
      notifier.notifyBattleStarted(new BattleStarted("battle-1", "guild-1"));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_STARTED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("battle-1");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("guild-1");
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("preserves battleId and guildId in the payload")
    void shouldPreserveBattleIdAndGuildId() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_STARTED);
      notifier.notifyBattleStarted(new BattleStarted("battle-42", "guild-99"));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("battle-42");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("guild-99");
    }
  }

  // ── notifyBattleWon ------------------------------------------------------
  @Nested
  @DisplayName("notifyBattleWon")
  class NotifyBattleWon {
    @Test
    @DisplayName("publishes a message to guild.battle-won")
    void shouldPublishToBattleWonTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_WON);

      notifier.notifyBattleWon(new BattleWon("battle-2", "guild-2", 200, 100));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_WON);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("battle-2");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("guild-2");
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("preserves battleId and guildId when publishing victory event")
    void shouldPreserveIdsInWonPayload() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_WON);
      notifier.notifyBattleWon(new BattleWon("epic-battle", "champion-guild", 500, 250));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("epic-battle");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("champion-guild");
    }
  }

  // ── notifyBattleLost -----------------------------------------------------
  @Nested
  @DisplayName("notifyBattleLost")
  class NotifyBattleLost {
    @Test
    @DisplayName("publishes a message to guild.battle-lost")
    void shouldPublishToBattleLostTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_LOST);
      notifier.notifyBattleLost(new BattleLost("battle-3", "guild-3", 50));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_LOST);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("battle-3");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("guild-3");
      assertThat(node.has("occurredOn")).isTrue();
    }

    @Test
    @DisplayName("preserves battleId and guildId in the defeat payload")
    void shouldPreserveIdsInLostPayload() throws Exception {
      subscribeAndSeekToEnd(TOPIC_BATTLE_LOST);
      notifier.notifyBattleLost(new BattleLost("lost-battle", "defeated-guild", 100));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(BATTLE_ID).asText()).isEqualTo("lost-battle");
      assertThat(node.get(GUILD_ID).asText()).isEqualTo("defeated-guild");
    }
  }
}
