package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvatarEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save("avatar-1", "mario@example.com");
  }

  @Test
  void whenLevelUpped_thenEmailSentWithNewLevel() throws Exception {
    publish(
        "avatar.level-upped",
        new AvatarEventConsumer.LevelUppedMessage("avatar-1", 10, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Livello aumentato!");
    assertThat(bodyOf(mails[0])).contains("10");
  }

  @Test
  void whenDead_thenEmailSentWithAvatarId() throws Exception {
    assertThat(userEmailRepository.findEmailByUserId("avatar-1"))
        .isPresent()
        .hasValue("mario@example.com");

    publish("avatar.dead", new AvatarEventConsumer.DeadMessage("avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Il tuo avatar è morto!");
  }

  @Test
  void whenSkillPointAssigned_thenEmailSentWithStatDetails() throws Exception {
    publish(
        "avatar.skill-point-assigned",
        new AvatarEventConsumer.SkillPointAssignedMessage(
            "avatar-1", "STRENGTH", 15, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Punto abilità assegnato!");
    assertThat(bodyOf(mails[0])).contains("STRENGTH").contains("15");
  }

  @Test
  void whenAvatarNotRegistered_thenNoEmailIsSent() {
    publish("avatar.dead", new AvatarEventConsumer.DeadMessage("avatar-unknown", Instant.now()));
    assertThat(greenMail.getReceivedMessages()).isEmpty();
  }
}
