/**
 * package habitquest.marketplace.infrastructure;
 *
 * <p>import static org.assertj.core.api.Assertions.assertThat;
 *
 * <p>import com.fasterxml.jackson.databind.ObjectMapper; import
 * habitquest.marketplace.application.MarketplaceRepository; import
 * habitquest.marketplace.domain.events.ItemBought; import
 * habitquest.marketplace.domain.events.ItemSold; import java.time.Duration; import
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
 * org.testcontainers.utility.DockerImageName; @SpringBootTest @Testcontainers @DisplayName("MarketplaceNotifierImpl")
 * public class MarketplaceNotifierImplIT { @Container static final KafkaContainer KAFKA = new
 * KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0")); @DynamicPropertySource static void
 * kafkaProperties(DynamicPropertyRegistry registry) {
 * registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
 * } @Autowired private MarketplaceNotifierImpl notifier; @MockitoBean private MarketplaceRepository
 * marketplaceRepository;
 *
 * <p>// ── Topic names (must match application.yml destinations) ───────────────────
 *
 * <p>private static final String TOPIC_ITEM_BOUGHT = "marketplace.item-bought"; private static
 * final String TOPIC_ITEM_SOLD = "marketplace.item-sold";
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
 * <p>// Polls until at least one record arrives on the given topic, or times out. private
 * ConsumerRecord<String, String> pollOne(String topic) {
 * consumer.subscribe(Collections.singletonList(topic)); long deadline = System.currentTimeMillis()
 * + POLL_TIMEOUT.toMillis(); while (System.currentTimeMillis() < deadline) { for
 * (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) { return record;
 * } } throw new AssertionError("No message received on topic '" + topic + "' within timeout"); }
 *
 * <p>// ── notifyItemBought
 * ───────────────────────────────────────────────────────── @Nested @DisplayName("notifyItemBought")
 * class NotifyItemBought { @Test @DisplayName("publishes a message to marketplace.item-bought")
 * void shouldPublishToItemBoughtTopic() throws Exception { notifier.notifyItemBought(new
 * ItemBought("market-1", "Iron Sword", "avatar-42"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_ITEM_BOUGHT);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_ITEM_BOUGHT);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("marketplaceId").asText()).isEqualTo("market-1");
 * assertThat(node.get("itemName").asText()).isEqualTo("Iron Sword");
 * assertThat(node.get("avatarId").asText()).isEqualTo("avatar-42");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves all fields in the
 * item-bought payload") void shouldPreserveAllFields() throws Exception {
 * notifier.notifyItemBought(new ItemBought("market-99", "Dragon Shield", "hero-7"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_ITEM_BOUGHT).value());
 * assertThat(node.get("marketplaceId").asText()).isEqualTo("market-99");
 * assertThat(node.get("itemName").asText()).isEqualTo("Dragon Shield");
 * assertThat(node.get("avatarId").asText()).isEqualTo("hero-7"); } @Test @DisplayName("each
 * purchase event has its own occurredOn timestamp") void shouldHaveTimestamp() throws Exception {
 * notifier.notifyItemBought(new ItemBought("market-1", "Mana Potion", "avatar-1"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_ITEM_BOUGHT).value());
 * assertThat(node.get("occurredOn").asText()).isNotBlank(); } }
 *
 * <p>// ── notifyItemSold
 * ─────────────────────────────────────────────────────────── @Nested @DisplayName("notifyItemSold")
 * class NotifyItemSold { @Test @DisplayName("publishes a message to marketplace.item-sold") void
 * shouldPublishToItemSoldTopic() throws Exception { notifier.notifyItemSold(new
 * ItemSold("market-2", "Old Armor", "avatar-10"));
 *
 * <p>ConsumerRecord<String, String> record = pollOne(TOPIC_ITEM_SOLD);
 *
 * <p>assertThat(record.topic()).isEqualTo(TOPIC_ITEM_SOLD);
 *
 * <p>var node = objectMapper.readTree(record.value());
 * assertThat(node.get("marketplaceId").asText()).isEqualTo("market-2");
 * assertThat(node.get("itemName").asText()).isEqualTo("Old Armor");
 * assertThat(node.get("avatarId").asText()).isEqualTo("avatar-10");
 * assertThat(node.has("occurredOn")).isTrue(); } @Test @DisplayName("preserves all fields in the
 * item-sold payload") void shouldPreserveAllFields() throws Exception { notifier.notifyItemSold(new
 * ItemSold("market-5", "Rusty Sword", "warrior-3"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_ITEM_SOLD).value());
 * assertThat(node.get("marketplaceId").asText()).isEqualTo("market-5");
 * assertThat(node.get("itemName").asText()).isEqualTo("Rusty Sword");
 * assertThat(node.get("avatarId").asText()).isEqualTo("warrior-3"); } @Test @DisplayName("each sale
 * event has its own occurredOn timestamp") void shouldHaveTimestamp() throws Exception {
 * notifier.notifyItemSold(new ItemSold("market-1", "Health Potion", "avatar-2"));
 *
 * <p>var node = objectMapper.readTree(pollOne(TOPIC_ITEM_SOLD).value());
 * assertThat(node.get("occurredOn").asText()).isNotBlank(); } } }
 */
