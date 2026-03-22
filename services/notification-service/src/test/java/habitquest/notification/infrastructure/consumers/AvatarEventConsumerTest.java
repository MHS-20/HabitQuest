package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.notification.infrastructure.notification.NotificationService;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AvatarEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String AVATAR_1 = "avatar-1";
  public static final String MAIL = "mario@example.com";
  public static final String AVATAR_DEAD = "avatar.dead";
  @Autowired private NotificationService notificationService;

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
  void whenLevelUpped_thenEmailSentWithNewLevel() throws Exception {
    publish(
        "avatar.level-upped",
        new AvatarEventConsumer.LevelUppedMessage(AVATAR_1, 10, Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Level up!");
    assertThat(bodyOf(mails[0])).contains("10");
  }

  @Test
  void whenDead_thenEmailSentWithAvatarId() throws Exception {
    assertThat(userEmailRepository.findEmailByUserId(AVATAR_1)).isPresent().hasValue(MAIL);

    publish(AVATAR_DEAD, new AvatarEventConsumer.DeadMessage(AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Your avatar has died!");
  }

  @Test
  void whenSkillPointAssigned_thenEmailSentWithStatDetails() throws Exception {
    publish(
        "avatar.skill-point-assigned",
        new AvatarEventConsumer.SkillPointAssignedMessage(AVATAR_1, "STRENGTH", 15, Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Skill point assigned!");
    assertThat(bodyOf(mails[0])).contains("STRENGTH").contains("15");
  }

  @Test
  void whenNewSpellLearned_thenEmailSentWithSpellDetails() throws Exception {
    publish(
        "avatar.new-spell-learned",
        new AvatarEventConsumer.NewSpellLearnedMessage(
            AVATAR_1, "Fireball", "A basic fire spell.", Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("New spell learned!");
    assertThat(bodyOf(mails[0])).contains("Fireball").contains("A basic fire spell.");
  }

  @Test
  void whenAvatarNotRegistered_thenNoEmailIsSent() {
    publish(AVATAR_DEAD, new AvatarEventConsumer.DeadMessage("avatar-unknown", Instant.now()));
    assertThat(greenMail.getReceivedMessages()).isEmpty();
  }
}
