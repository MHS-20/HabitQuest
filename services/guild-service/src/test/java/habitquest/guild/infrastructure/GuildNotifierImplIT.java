/**
 * package habitquest.guild.infrastructure;
 *
 * <p>import static org.assertj.core.api.Assertions.assertThat;
 *
 * <p>import com.fasterxml.jackson.databind.ObjectMapper; import
 * habitquest.guild.application.BattleRepository; import
 * habitquest.guild.application.GuildRepository; import
 * habitquest.guild.domain.events.guildEvents.*; import habitquest.guild.domain.guild.GuildRole;
 * import java.time.Duration; import java.util.Collections; import java.util.Map; import
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
 * org.testcontainers.utility.DockerImageName; @SpringBootTest @Testcontainers @DisplayName("GuildNotifierImpl")
 * public class GuildNotifierImplIT { @Container static final KafkaContainer KAFKA = new
 * KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0")); @DynamicPropertySource static void
 * kafkaProperties(DynamicPropertyRegistry registry) {
 * registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
 * } @Autowired private GuildNotifierImpl notifier; @MockitoBean private GuildRepository
 * guildRepository; @MockitoBean private BattleRepository battleRepository;
 *
 * <p>// ── Topic names (must match application.yml destinations) ───────────────────
 *
 * <p>private static final String TOPIC_GUILD_CREATED = "guild.created"; private static final String
 * TOPIC_GUILD_DELETED = "guild.deleted"; private static final String TOPIC_GUILD_JOINED =
 * "guild.joined"; private static final String TOPIC_GUILD_LEFT = "guild.left"; private static final
 * String TOPIC_MEMBER_REMOVED = "guild.member-removed"; private static final String
 * TOPIC_ROLE_ASSIGNED = "guild.role-assigned";
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
 * <p>// ── notifyGuildCreated
 * ─────────────────────────────────────────────────────── @Nested @DisplayName("notifyGuildCreated")
 * class NotifyGuildCreated { @Test @DisplayName("publishes a message to guild.created") void
 * shouldPublishToGuildCreatedTopic() throws Exception { notifier.notifyGuildCreated(new
 * GuildCreated("guild-42", "The Brave"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_GUILD_CREATED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_GUILD_CREATED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-42");
 * assertThat(node.get("name").asText()).isEqualTo("The Brave");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves guildId and name in
 * the payload") void shouldPreserveGuildIdAndName() throws Exception {
 * notifier.notifyGuildCreated(new GuildCreated("my-guild", "Legends"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_GUILD_CREATED).value());
 * assertThat(node.get("guildId").asText()).isEqualTo("my-guild");
 * assertThat(node.get("name").asText()).isEqualTo("Legends"); } }
 *
 * <p>// ── notifyGuildDeleted
 * ─────────────────────────────────────────────────────── @Nested @DisplayName("notifyGuildDeleted")
 * class NotifyGuildDeleted { @Test @DisplayName("publishes a message to guild.deleted") void
 * shouldPublishToGuildDeletedTopic() throws Exception { notifier.notifyGuildDeleted(new
 * GuildDeleted("guild-99"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_GUILD_DELETED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_GUILD_DELETED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-99");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves guildId in the
 * payload") void shouldPreserveGuildId() throws Exception { notifier.notifyGuildDeleted(new
 * GuildDeleted("deleted-guild"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_GUILD_DELETED).value());
 * assertThat(node.get("guildId").asText()).isEqualTo("deleted-guild"); } }
 *
 * <p>// ── notifyGuildJoined
 * ──────────────────────────────────────────────────────── @Nested @DisplayName("notifyGuildJoined")
 * class NotifyGuildJoined { @Test @DisplayName("publishes a message to guild.joined") void
 * shouldPublishToGuildJoinedTopic() throws Exception { notifier.notifyGuildJoined(new
 * GuildJoined("guild-1", "avatar-5"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_GUILD_JOINED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_GUILD_JOINED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-1");
 * assertThat(node.get("memberId").asText()).isEqualTo("avatar-5");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves both guildId and
 * memberId in the payload") void shouldPreserveGuildIdAndMemberId() throws Exception {
 * notifier.notifyGuildJoined(new GuildJoined("g-10", "m-20"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_GUILD_JOINED).value());
 * assertThat(node.get("guildId").asText()).isEqualTo("g-10");
 * assertThat(node.get("memberId").asText()).isEqualTo("m-20"); } }
 *
 * <p>// ── notifyGuildLeft
 * ────────────────────────────────────────────────────────── @Nested @DisplayName("notifyGuildLeft")
 * class NotifyGuildLeft { @Test @DisplayName("publishes a message to guild.left") void
 * shouldPublishToGuildLeftTopic() throws Exception { notifier.notifyGuildLeft(new
 * GuildLeft("guild-3", "avatar-7"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_GUILD_LEFT);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_GUILD_LEFT);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-3");
 * assertThat(node.get("memberId").asText()).isEqualTo("avatar-7");
 * assertThat(node.has("occurredOn")).isTrue(); } }
 *
 * <p>// ── notifyRemovedFromGuild
 * ─────────────────────────────────────────────────── @Nested @DisplayName("notifyRemovedFromGuild")
 * class NotifyRemovedFromGuild { @Test @DisplayName("publishes a message to guild.member-removed")
 * void shouldPublishToMemberRemovedTopic() throws Exception { notifier.notifyRemovedFromGuild(new
 * RemovedFromGuild("guild-4", "avatar-9"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_MEMBER_REMOVED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_MEMBER_REMOVED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-4");
 * assertThat(node.get("memberId").asText()).isEqualTo("avatar-9");
 * assertThat(node.has("occurredOn")).isTrue(); } }
 *
 * <p>// ── notifyRoleAssigned
 * ─────────────────────────────────────────────────────── @Nested @DisplayName("notifyRoleAssigned")
 * class NotifyRoleAssigned { @Test @DisplayName("publishes a message to guild.role-assigned with
 * correct role name") void shouldPublishToRoleAssignedTopic() throws Exception {
 * notifier.notifyRoleAssigned( new RoleAssigned("guild-5", "avatar-11", new GuildRole("OFFICER")));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_ROLE_ASSIGNED);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_ROLE_ASSIGNED);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("guildId").asText()).isEqualTo("guild-5");
 * assertThat(node.get("memberId").asText()).isEqualTo("avatar-11");
 * assertThat(node.get("roleName").asText()).isEqualTo("OFFICER");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves the role name for a
 * LEADER role assignment") void shouldPreserveLeaderRole() throws Exception {
 * notifier.notifyRoleAssigned( new RoleAssigned("guild-6", "avatar-12", new GuildRole("LEADER")));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_ROLE_ASSIGNED).value());
 * assertThat(node.get("roleName").asText()).isEqualTo("LEADER"); } } }
 */
