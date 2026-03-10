/**
 * package habitquest.guild.infrastructure;
 *
 * <p>import static org.assertj.core.api.Assertions.assertThat;
 *
 * <p>import com.fasterxml.jackson.databind.ObjectMapper; import
 * habitquest.guild.application.BattleRepository; import
 * habitquest.guild.application.GuildRepository; import habitquest.guild.domain.battle.Experience;
 * import habitquest.guild.domain.battle.Money; import habitquest.guild.domain.battle.Penalty;
 * import habitquest.guild.domain.events.battleEvents.BattleLost; import
 * habitquest.guild.domain.events.battleEvents.BattleStarted; import
 * habitquest.guild.domain.events.battleEvents.BattleWon; import java.time.Duration; import
 * java.util.Collections; import java.util.Map; import
 * org.apache.kafka.clients.consumer.ConsumerConfig; import
 * org.apache.kafka.clients.consumer.ConsumerRecord; import
 * org.apache.kafka.clients.consumer.KafkaConsumer; import
 * org.apache.kafka.common.serialization.StringDeserializer; import org.junit.jupiter.api.AfterEach;
 * import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.DisplayName; import
 * org.junit.jupiter.api.Nested; import org.junit.jupiter.api.Test; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.test.context.DynamicPropertyRegistry; import
 * org.springframework.test.context.DynamicPropertySource; import
 * org.springframework.test.context.bean.override.mockito.MockitoBean; import
 * org.testcontainers.junit.jupiter.Container; import
 * org.testcontainers.junit.jupiter.Testcontainers; import org.testcontainers.kafka.KafkaContainer;
 * import
 * org.testcontainers.utility.DockerImageName; @SpringBootTest @Testcontainers @DisplayName("BattleNotifierImpl")
 * public class BattleNotifierImplIT { @Container static final KafkaContainer KAFKA = new
 * KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0")); @DynamicPropertySource static void
 * kafkaProperties(DynamicPropertyRegistry registry) {
 * registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
 * } @Autowired private BattleNotifierImpl notifier; @MockitoBean private GuildRepository
 * guildRepository; @MockitoBean private BattleRepository battleRepository;
 *
 * <p>// ── Topic names (must match application.yml destinations) ───────────────────
 *
 * <p>private static final String TOPIC_BATTLE_STARTED = "guild.battle-started"; private static
 * final String TOPIC_BATTLE_WON = "guild.battle-won"; private static final String TOPIC_BATTLE_LOST
 * = "guild.battle-lost";
 *
 * <p>private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);
 *
 * <p>private KafkaConsumer<String, String> consumer; private final ObjectMapper objectMapper = new
 * ObjectMapper().findAndRegisterModules(); @BeforeEach void createConsumer() { consumer = new
 * KafkaConsumer<>( Map.of( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
 * ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.nanoTime(),
 * ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
 * ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
 * ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName()));
 * } @AfterEach void closeConsumer() { consumer.close(); }
 *
 * <p>private ConsumerRecord<String, String> pollOne(String topic) {
 * consumer.subscribe(Collections.singletonList(topic)); long deadline = System.currentTimeMillis()
 * + POLL_TIMEOUT.toMillis(); while (System.currentTimeMillis() < deadline) { for
 * (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) { return record;
 * } } throw new AssertionError("No message received on topic '" + topic + "' within timeout"); }
 *
 * <p>// ── notifyBattleStarted
 * ────────────────────────────────────────────────────── @Nested @DisplayName("notifyBattleStarted")
 * class NotifyBattleStarted { @Test @DisplayName("publishes a message to guild.battle-started")
 * void shouldPublishToBattleStartedTopic() throws Exception { notifier.notifyBattleStarted(new
 * BattleStarted("battle-1", "guild-1"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_BATTLE_STARTED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_STARTED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("battleId").asText()).isEqualTo("battle-1");
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-1");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves battleId and guildId
 * in the payload") void shouldPreserveBattleIdAndGuildId() throws Exception {
 * notifier.notifyBattleStarted(new BattleStarted("battle-42", "guild-99"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_BATTLE_STARTED).value());
 * assertThat(node.get("battleId").asText()).isEqualTo("battle-42");
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-99"); } }
 *
 * <p>// ── notifyBattleWon
 * ────────────────────────────────────────────────────────── @Nested @DisplayName("notifyBattleWon")
 * class NotifyBattleWon { @Test @DisplayName("publishes a message to guild.battle-won") void
 * shouldPublishToBattleWonTopic() throws Exception { notifier.notifyBattleWon( new
 * BattleWon("battle-2", "guild-2", new Experience(200), new Money(100)));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_BATTLE_WON);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_WON);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("battleId").asText()).isEqualTo("battle-2");
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-2");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves battleId and guildId
 * when publishing victory event") void shouldPreserveIdsInWonPayload() throws Exception {
 * notifier.notifyBattleWon( new BattleWon("epic-battle", "champion-guild", new Experience(500), new
 * Money(250)));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_BATTLE_WON).value());
 * assertThat(node.get("battleId").asText()).isEqualTo("epic-battle");
 * assertThat(node.get("guildId").asText()).isEqualTo("champion-guild"); } }
 *
 * <p>// ── notifyBattleLost
 * ───────────────────────────────────────────────────────── @Nested @DisplayName("notifyBattleLost")
 * class NotifyBattleLost { @Test @DisplayName("publishes a message to guild.battle-lost") void
 * shouldPublishToBattleLostTopic() throws Exception { notifier.notifyBattleLost(new
 * BattleLost("battle-3", "guild-3", new Penalty(50)));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_BATTLE_LOST);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_BATTLE_LOST);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("battleId").asText()).isEqualTo("battle-3");
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-3");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves battleId and guildId
 * in the defeat payload") void shouldPreserveIdsInLostPayload() throws Exception {
 * notifier.notifyBattleLost(new BattleLost("lost-battle", "defeated-guild", new Penalty(100)));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_BATTLE_LOST).value());
 * assertThat(node.get("battleId").asText()).isEqualTo("lost-battle");
 * assertThat(node.get("guildId").asText()).isEqualTo("defeated-guild"); } } }
 */
