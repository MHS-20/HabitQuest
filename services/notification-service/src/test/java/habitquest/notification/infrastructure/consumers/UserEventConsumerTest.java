package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.notification.infrastructure.consumers.users.UserMessages.*;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
  }

  @Test
  void whenUserRegistered_thenEmailIsMappedAndWelcomeMailIsSent() throws Exception {
    publish(
        "userRegistered-in-0",
        new UserRegisteredMessage("avatar-1", "mario@example.com", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(mails).hasSize(1);
    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Welcome to HabitQuest!");
    assertThat(bodyOf(mails[0])).contains("created successfully");
  }

  @Test
  void whenUserRegistered_thenAvatarIdIsMappedToEmail() {
    publish(
        "userRegistered-in-0",
        new UserRegisteredMessage("avatar-42", "luigi@example.com", Instant.now()));
    waitForEmails(1);
    assertThat(userEmailRepository.findEmailByUserId("avatar-42"))
        .isPresent()
        .hasValue("luigi@example.com");
  }
}
