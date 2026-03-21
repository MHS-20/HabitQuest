package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HabitEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save("avatar-1", "mario@example.com");
  }

  @Test
  void whenHabitDeleted_thenEmailSent() throws Exception {
    publish(
        "habit.deleted",
        new HabitEventConsumer.HabitDeletedMessage("habit-abc", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Abitudine eliminata");
    assertThat(bodyOf(mails[0])).contains("habit-abc");
  }

  @Test
  void whenHabitAttended_thenPositiveEmailSent() throws Exception {
    publish(
        "habit.attended",
        new HabitEventConsumer.HabitAttendedMessage("habit-abc", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Abitudine completata!");
    assertThat(bodyOf(mails[0])).contains("habit-abc");
  }

  @Test
  void whenHabitNotAttended_thenMotivationalEmailSent() throws Exception {
    publish(
        "habit.not-attended",
        new HabitEventConsumer.HabitNotAttendedMessage("habit-abc", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Abitudine non completata");
    assertThat(bodyOf(mails[0])).contains("Non mollare");
  }
}
