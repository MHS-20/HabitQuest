package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HabitEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String AVATAR_1 = "avatar-1";
  public static final String MAIL = "mario@example.com";
  public static final String HABIT_ABC = "habit-abc";

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save(AVATAR_1, MAIL);
  }

  @Test
  void whenHabitDeleted_thenEmailSent() throws Exception {
    publish(
        "habit.deleted",
        new HabitEventConsumer.HabitDeletedMessage(HABIT_ABC, AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Habit removed");
    assertThat(bodyOf(mails[0])).contains(HABIT_ABC);
  }

  @Test
  void whenHabitAttended_thenPositiveEmailSent() throws Exception {
    publish(
        "habit.attended",
        new HabitEventConsumer.HabitAttendedMessage(HABIT_ABC, AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Habit completed!");
    assertThat(bodyOf(mails[0])).contains(HABIT_ABC);
  }

  @Test
  void whenHabitNotAttended_thenMotivationalEmailSent() throws Exception {
    publish(
        "habit.not-attended",
        new HabitEventConsumer.HabitNotAttendedMessage(HABIT_ABC, AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Habit missed");
    assertThat(bodyOf(mails[0])).contains("Don't give up");
  }
}
