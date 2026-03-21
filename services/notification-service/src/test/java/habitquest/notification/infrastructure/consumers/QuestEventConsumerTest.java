package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save("avatar-1", "mario@example.com");
  }

  @Test
  void whenQuestCreated_thenEmailSentToCreator() throws Exception {
    publish(
        "quest.created",
        new QuestEventConsumer.QuestCreatedMessage(
            "quest-1", "avatar-1", "Uccidi il drago", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Nuova quest disponibile!");
    assertThat(bodyOf(mails[0])).contains("Uccidi il drago");
  }

  @Test
  void whenQuestCompleted_thenCongratulationsEmailSent() throws Exception {
    publish(
        "quest.completed",
        new QuestEventConsumer.QuestCompletedMessage("quest-1", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("mario@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Quest completata!");
  }

  @Test
  void whenQuestNotCompleted_thenEncouragementEmailSent() throws Exception {
    publish(
        "quest.not-completed",
        new QuestEventConsumer.QuestNotCompletedMessage("avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Quest non completata");
    assertThat(bodyOf(mails[0])).contains("Riprova");
  }

  @Test
  void whenQuestJoined_thenEmailSent() throws Exception {
    publish(
        "quest.joined",
        new QuestEventConsumer.QuestJoinedMessage("quest-1", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Sei entrato in una quest!");
    assertThat(bodyOf(mails[0])).contains("quest-1");
  }

  @Test
  void whenQuestLeft_thenEmailSent() throws Exception {
    publish(
        "quest.left",
        new QuestEventConsumer.QuestLeftMessage("quest-1", "avatar-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Hai abbandonato la quest");
  }
}
