package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuildEventConsumerTest extends BaseConsumerIntegrationTest {

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save("leader-1", "leader@example.com");
    userEmailRepository.save("member-1", "member1@example.com");
    userEmailRepository.save("member-2", "member2@example.com");
  }

  // --- GuildCreated ---

  @Test
  void whenGuildCreated_thenLeaderReceivesEmailAndIsMemberOfGuild() throws Exception {
    publish(
        "guild.created",
        new GuildEventConsumer.GuildCreatedMessage(
            "guild-1", "leader-1", "I Draghi", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("leader@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Guild creata!");
    assertThat(bodyOf(mails[0])).contains("I Draghi");

    // Il leader deve essere aggiunto come membro
    assertThat(guildMemberRepository.findMembersByGuildId("guild-1")).contains("leader-1");
  }

  // --- GuildJoined ---

  @Test
  void whenGuildJoined_thenMemberReceivesEmailAndIsAddedToGuild() throws Exception {
    publish(
        "guild.joined",
        new GuildEventConsumer.GuildJoinedMessage("guild-1", "member-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("member1@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Benvenuto nella guild!");

    assertThat(guildMemberRepository.findMembersByGuildId("guild-1")).contains("member-1");
  }

  // --- GuildLeft ---

  @Test
  void whenGuildLeft_thenMemberReceivesEmailAndIsRemovedFromGuild() throws Exception {
    guildMemberRepository.addMember("guild-1", "member-1");

    publish(
        "guild.left",
        new GuildEventConsumer.GuildLeftMessage("guild-1", "member-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Hai lasciato la guild");
    assertThat(guildMemberRepository.findMembersByGuildId("guild-1")).doesNotContain("member-1");
  }

  // --- RemovedFromGuild ---

  @Test
  void whenRemovedFromGuild_thenMemberReceivesEmailAndIsRemovedFromGuild() throws Exception {
    guildMemberRepository.addMember("guild-1", "member-1");

    publish(
        "guild.removed",
        new GuildEventConsumer.RemovedFromGuildMessage("guild-1", "member-1", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Sei stato rimosso dalla guild");
    assertThat(guildMemberRepository.findMembersByGuildId("guild-1")).doesNotContain("member-1");
  }

  // --- GuildDeleted ---

  @Test
  void whenGuildDeleted_thenAllMembersReceiveEmailAndGuildIsRemoved() throws Exception {
    guildMemberRepository.addMember("guild-1", "leader-1");
    guildMemberRepository.addMember("guild-1", "member-1");

    publish("guild.deleted", new GuildEventConsumer.GuildDeletedMessage("guild-1", Instant.now()));

    // Due membri → due mail
    MimeMessage[] mails = waitForEmails(2);

    assertThat(mails).hasSize(2);
    assertThat(mails).allSatisfy(mail -> assertThat(subjectOf(mail)).isEqualTo("Guild eliminata"));

    // Dopo la cancellazione la guild non deve più esistere nella repository
    assertThat(guildMemberRepository.findMembersByGuildId("guild-1")).isEmpty();
  }

  // --- RoleAssigned ---

  @Test
  void whenRoleAssigned_thenMemberReceivesEmailWithRoleName() throws Exception {
    publish(
        "guild.role-assigned",
        new GuildEventConsumer.RoleAssignedMessage(
            "guild-1", "member-1", "VICE_LEADER", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("Nuovo ruolo assegnato!");
    assertThat(bodyOf(mails[0])).contains("VICE_LEADER");
  }

  // --- InviteSent ---

  @Test
  void whenInviteSent_thenTargetAvatarReceivesEmail() throws Exception {
    publish(
        "guild.invite-sent",
        new GuildEventConsumer.InviteSentMessage(
            "guild-1", "member-1", "invite-xyz", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("member1@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Invito nella guild!");
    assertThat(bodyOf(mails[0])).contains("guild-1");
  }
}
