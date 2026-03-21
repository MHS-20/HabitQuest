package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import habitquest.notification.infrastructure.repository.GuildMemberRepository;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@TestPropertySource(
    properties = {
      "spring.mail.host=localhost",
      "spring.mail.port=3025",
      "spring.mail.username=",
      "spring.mail.password=",
      "spring.mail.properties.mail.smtp.auth=false",
      "spring.mail.properties.mail.smtp.starttls.enable=false",
      "spring.mail.properties.mail.smtp.starttls.required=false"
    })
public abstract class BaseConsumerIntegrationTest {

  private static final int SMTP_PORT = 3025;
  private static final Duration EMAIL_TIMEOUT = Duration.ofSeconds(5);

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

  protected static GreenMail greenMail;

  @Autowired protected StreamBridge streamBridge;
  @Autowired protected UserEmailRepository userEmailRepository;
  @Autowired protected GuildMemberRepository guildMemberRepository;

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
    registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
  }

  @BeforeAll
  static void setupMail() {
    if (greenMail == null) {
      greenMail = new GreenMail(new ServerSetup(SMTP_PORT, "localhost", ServerSetup.PROTOCOL_SMTP));
      // greenMail.setUser("test@habitquest.it", "test");
      greenMail.start();
    }
  }

  @AfterAll
  static void tearDownMail() {
    if (greenMail != null) {
      greenMail.stop();
    }
  }

  protected void publish(String bindingName, Object payload) {
    streamBridge.send(bindingName, payload);
  }

  protected MimeMessage[] waitForEmails(int count) {
    Awaitility.await()
        .atMost(EMAIL_TIMEOUT)
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () ->
                assertThat(greenMail.getReceivedMessages())
                    .as(
                        "Expected %d emails but found %d",
                        count, greenMail.getReceivedMessages().length)
                    .hasSizeGreaterThanOrEqualTo(count));
    return greenMail.getReceivedMessages();
  }

  protected void resetGreenMail() {
    greenMail.reset();
  }

  protected String subjectOf(MimeMessage msg) {
    try {
      return msg.getSubject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String recipientOf(MimeMessage msg) {
    try {
      return msg.getAllRecipients()[0].toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String bodyOf(MimeMessage msg) {
    try {
      return msg.getContent().toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
