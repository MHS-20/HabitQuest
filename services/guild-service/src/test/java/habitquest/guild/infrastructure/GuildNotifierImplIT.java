package habitquest.guild.infrastructure;

import static habitquest.guild.GuildFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.guild.application.port.out.BattleRepository;
import habitquest.guild.application.port.out.GuildRepository;
import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.infrastructure.outbound.GuildNotifierImpl;
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
@DisplayName("GuildNotifierImpl")
public class GuildNotifierImplIT {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  public static final String JSON_GUILD_ID = "guildId";
  public static final String JSON_MEMBER_ID = "memberId";
  public static final String JSON_OCCURRED_ON = "occurredOn";

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
  }

  @Autowired private GuildNotifierImpl notifier;

  @MockitoBean private GuildRepository guildRepository;

  @MockitoBean private BattleRepository battleRepository;

  // ── Topic names ───────────────────
  private static final String TOPIC_GUILD_CREATED = "guild.created";
  private static final String TOPIC_GUILD_DELETED = "guild.deleted";
  private static final String TOPIC_GUILD_JOINED = "guild.joined";
  private static final String TOPIC_GUILD_LEFT = "guild.left";
  private static final String TOPIC_MEMBER_REMOVED = "guild.removed";
  private static final String TOPIC_ROLE_ASSIGNED = "guild.role-assigned";

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

  // ── notifyGuildCreated ---------------------------------------------------
  @Nested
  @DisplayName("notifyGuildCreated")
  class NotifyGuildCreated {

    @Test
    @DisplayName("publishes a message to guild.created")
    void shouldPublishToGuildCreatedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_CREATED);
      notifier.notifyGuildCreated(new GuildCreated(GUILD_1, LEADER_1, GUILD_NAME));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_GUILD_CREATED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.get("name").asText()).isEqualTo(GUILD_NAME);
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves guildId and name in the payload")
    void shouldPreserveGuildIdAndName() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_CREATED);
      notifier.notifyGuildCreated(new GuildCreated("my-guild", LEADER_1, "Legends"));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo("my-guild");
      assertThat(node.get("name").asText()).isEqualTo("Legends");
    }
  }

  // ── notifyGuildDeleted ---------------------------------------------------
  @Nested
  @DisplayName("notifyGuildDeleted")
  class NotifyGuildDeleted {

    @Test
    @DisplayName("publishes a message to guild.deleted")
    void shouldPublishToGuildDeletedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_DELETED);
      notifier.notifyGuildDeleted(new GuildDeleted(GUILD_1));

      ConsumerRecord<String, String> record = pollOne();
      assertThat(record.topic()).isEqualTo(TOPIC_GUILD_DELETED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves guildId in the payload")
    void shouldPreserveGuildId() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_DELETED);
      notifier.notifyGuildDeleted(new GuildDeleted("deleted-guild"));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo("deleted-guild");
    }
  }

  // ── notifyGuildJoined ----------------------------------------------------
  @Nested
  @DisplayName("notifyGuildJoined")
  class NotifyGuildJoined {

    @Test
    @DisplayName("publishes a message to guild.joined")
    void shouldPublishToGuildJoinedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_JOINED);
      notifier.notifyGuildJoined(new GuildJoined(GUILD_ID, MEMBER_ID));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_GUILD_JOINED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.get(JSON_MEMBER_ID).asText()).isEqualTo(MEMBER_1);
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves both guildId and memberId in the payload")
    void shouldPreserveGuildIdAndMemberId() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_JOINED);
      notifier.notifyGuildJoined(new GuildJoined(new Id<>("g-10"), new Id<>("m-20")));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo("g-10");
      assertThat(node.get(JSON_MEMBER_ID).asText()).isEqualTo("m-20");
    }
  }

  // ── notifyGuildLeft ------------------------------------------------------
  @Nested
  @DisplayName("notifyGuildLeft")
  class NotifyGuildLeft {

    @Test
    @DisplayName("publishes a message to guild.left")
    void shouldPublishToGuildLeftTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_GUILD_LEFT);
      notifier.notifyGuildLeft(new GuildLeft(GUILD_ID, MEMBER_ID));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_GUILD_LEFT);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.get(JSON_MEMBER_ID).asText()).isEqualTo(MEMBER_1);
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }
  }

  // ── notifyRemovedFromGuild ------------------------------------------------
  @Nested
  @DisplayName("notifyRemovedFromGuild")
  class NotifyRemovedFromGuild {

    @Test
    @DisplayName("publishes a message to guild.member-removed")
    void shouldPublishToMemberRemovedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_MEMBER_REMOVED);
      notifier.notifyRemovedFromGuild(new RemovedFromGuild(GUILD_ID, MEMBER_ID));

      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_MEMBER_REMOVED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.get(JSON_MEMBER_ID).asText()).isEqualTo(MEMBER_1);
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }
  }

  // ── notifyRoleAssigned ---------------------------------------------------
  @Nested
  @DisplayName("notifyRoleAssigned")
  class NotifyRoleAssigned {

    @Test
    @DisplayName("publishes a message to guild.role-assigned with correct role name")
    void shouldPublishToRoleAssignedTopic() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ROLE_ASSIGNED);
      notifier.notifyRoleAssigned(new RoleAssigned(GUILD_ID, MEMBER_ID, OFFICER_ROLE));
      ConsumerRecord<String, String> record = pollOne();

      assertThat(record.topic()).isEqualTo(TOPIC_ROLE_ASSIGNED);

      var node = objectMapper.readTree(record.value());
      assertThat(node.get(JSON_GUILD_ID).asText()).isEqualTo(GUILD_1);
      assertThat(node.get(JSON_MEMBER_ID).asText()).isEqualTo(MEMBER_1);
      assertThat(node.get("roleName").asText()).isEqualTo("OFFICER");
      assertThat(node.has(JSON_OCCURRED_ON)).isTrue();
    }

    @Test
    @DisplayName("preserves the role name for a LEADER role assignment")
    void shouldPreserveLeaderRole() throws Exception {
      subscribeAndSeekToEnd(TOPIC_ROLE_ASSIGNED);
      notifier.notifyRoleAssigned(new RoleAssigned(GUILD_ID, MEMBER_ID, LEADER_ROLE));

      var node = objectMapper.readTree(pollOne().value());
      assertThat(node.get("roleName").asText()).isEqualTo("LEADER");
    }
  }
}
