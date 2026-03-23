package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String AVATAR_1 = "avatar-1";
  public static final String MAIL = "mario@example.com";
  public static final String QUEST_1 = "quest-1";

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save(AVATAR_1, MAIL);
  }

  //  @Test
  //  void whenQuestCreated_thenEmailSentToCreator() throws Exception {
  //    publish(
  //        "quest.created",
  //        new QuestEventConsumer.QuestCreatedMessage(
  //            QUEST_1, AVATAR_1, "Kill the dragon", Instant.now()));
  //    MimeMessage[] mails = waitForEmails(1);
  //    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
  //    assertThat(subjectOf(mails[0])).isEqualTo("New quest available!");
  //    assertThat(bodyOf(mails[0])).contains("Kill the dragon");
  //  }

  @Test
  void whenQuestCompleted_thenCongratulationsEmailSent() throws Exception {
    publish(
        "quest.completed",
        new QuestEventConsumer.QuestCompletedMessage(QUEST_1, AVATAR_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo(MAIL);
    assertThat(subjectOf(mails[0])).isEqualTo("Quest completed!");
  }

  //  @Test
  //  void whenQuestNotCompleted_thenEncouragementEmailSent() throws Exception {
  //    publish(
  //        "quest.not-completed",
  //        new QuestEventConsumer.QuestNotCompletedMessage(AVATAR_1, Instant.now()));
  //    MimeMessage[] mails = waitForEmails(1);
  //    assertThat(subjectOf(mails[0])).isEqualTo("Quest not completed");
  //    assertThat(bodyOf(mails[0])).contains("Try again");
  //  }

  @Test
  void whenQuestJoined_thenEmailSent() throws Exception {
    publish(
        "quest.joined",
        new QuestEventConsumer.QuestJoinedMessage(QUEST_1, AVATAR_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(subjectOf(mails[0])).isEqualTo("You joined a quest!");
    assertThat(bodyOf(mails[0])).contains(QUEST_1);
  }

  @Test
  void whenQuestLeft_thenEmailSent() throws Exception {
    publish(
        "quest.left", new QuestEventConsumer.QuestLeftMessage(QUEST_1, AVATAR_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(1);
    assertThat(subjectOf(mails[0])).isEqualTo("You left the quest");
  }
}
