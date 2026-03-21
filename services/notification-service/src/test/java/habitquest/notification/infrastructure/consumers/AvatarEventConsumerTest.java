package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.notification.infrastructure.notification.NotificationService;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

class AvatarEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String AVATAR_1 = "avatar-1";
  public static final String MAIL = "mario@example.com";
  public static final String AVATAR_DEAD = "avatar.dead";
  @Autowired private NotificationService notificationService;
  @Autowired private KafkaTemplate<String, String> kafkaTemplate;

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save(AVATAR_1, MAIL);
  }

  @Test
  void greenMailIsReachable() throws Exception {
    notificationService.send(MAIL, "test", "test body");
    MimeMessage[] mails = waitForEmails(1);
    assertThat(mails).hasSize(1);
  }

  @Test
  void kafkaBrokerIsReachable() throws Exception {
    System.out.println(
        "### KafkaTemplate bootstrap servers: "
            + kafkaTemplate
                .getProducerFactory()
                .getConfigurationProperties()
                .get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    System.out.println(
        "### Container bootstrap servers: " + SharedKafkaContainer.INSTANCE.getBootstrapServers());

    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        SharedKafkaContainer.INSTANCE.getBootstrapServers());
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    KafkaTemplate<String, String> template =
        new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

    template.send(AVATAR_DEAD, "{\"avatarId\":\"test\",\"occurredOn\":\"2024-01-01T00:00:00Z\"}");
    template.flush();

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
        SharedKafkaContainer.INSTANCE.getBootstrapServers());
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-verify-" + System.nanoTime());
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
      consumer.subscribe(List.of(AVATAR_DEAD));

      Awaitility.await()
          .atMost(Duration.ofSeconds(10))
          .untilAsserted(
              () -> {
                var records = consumer.poll(Duration.ofMillis(500));
                assertThat(records.count()).isGreaterThan(0);
              });
    }
  }

  @Test
  void whenLevelUpped_thenEmailSentWithNewLevel() throws Exception {
    publish(
        "avatar.level-upped",
        new AvatarEventConsumer.LevelUppedMessage(AVATAR_1, 10, Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Livello aumentato!");
    assertThat(bodyOf(mails[0])).contains("10");
  }

  @Test
  void whenDead_thenEmailSentWithAvatarId() throws Exception {
    assertThat(userEmailRepository.findEmailByUserId(AVATAR_1)).isPresent().hasValue(MAIL);

    publish(AVATAR_DEAD, new AvatarEventConsumer.DeadMessage(AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Il tuo avatar è morto!");
  }

  @Test
  void whenSkillPointAssigned_thenEmailSentWithStatDetails() throws Exception {
    publish(
        "avatar.skill-point-assigned",
        new AvatarEventConsumer.SkillPointAssignedMessage(AVATAR_1, "STRENGTH", 15, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Punto abilità assegnato!");
    assertThat(bodyOf(mails[0])).contains("STRENGTH").contains("15");
  }

  @Test
  void whenAvatarNotRegistered_thenNoEmailIsSent() {
    publish(AVATAR_DEAD, new AvatarEventConsumer.DeadMessage("avatar-unknown", Instant.now()));
    assertThat(greenMail.getReceivedMessages()).isEmpty();
  }
}
