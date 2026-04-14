package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.notification.infrastructure.consumers.guild.GuildMessages.*;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuildEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String LEADER_1 = "leader-1";
  public static final String MEMBER_1 = "member-1";
  public static final String MEMBER_2 = "member-2";
  public static final String GUILD_1 = "guild-1";

  @BeforeEach
  void setUp() {
    resetGreenMail();
    userEmailRepository.save(LEADER_1, "leader@example.com");
    userEmailRepository.save(MEMBER_1, "member1@example.com");
    userEmailRepository.save(MEMBER_2, "member2@example.com");
  }

  // --- GuildCreated ---
  @Test
  void whenGuildCreated_thenLeaderReceivesEmailAndIsMemberOfGuild() throws Exception {
    publish("guild.created", new GuildCreatedMessage(GUILD_1, LEADER_1, "I Draghi", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("leader@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Guild created!");
    assertThat(bodyOf(mails[0])).contains("I Draghi");
    assertThat(guildMemberRepository.findMembersByGuildId(GUILD_1)).contains(LEADER_1);
  }

  // --- GuildJoined ---
  @Test
  void whenGuildJoined_thenMemberReceivesEmailAndIsAddedToGuild() throws Exception {
    publish("guild.joined", new GuildJoinedMessage(GUILD_1, MEMBER_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("member1@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Welcome to the guild!");

    assertThat(guildMemberRepository.findMembersByGuildId(GUILD_1)).contains(MEMBER_1);
  }

  // --- GuildLeft ---

  @Test
  void whenGuildLeft_thenMemberReceivesEmailAndIsRemovedFromGuild() throws Exception {
    guildMemberRepository.addMember(GUILD_1, MEMBER_1);

    publish("guild.left", new GuildLeftMessage(GUILD_1, MEMBER_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("You left the guild");
    assertThat(guildMemberRepository.findMembersByGuildId(GUILD_1)).doesNotContain(MEMBER_1);
  }

  // --- RemovedFromGuild ---

  @Test
  void whenRemovedFromGuild_thenMemberReceivesEmailAndIsRemovedFromGuild() throws Exception {
    guildMemberRepository.addMember(GUILD_1, MEMBER_1);

    publish("guild.removed", new RemovedFromGuildMessage(GUILD_1, MEMBER_1, Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("You have been removed from the guild");
    assertThat(guildMemberRepository.findMembersByGuildId(GUILD_1)).doesNotContain(MEMBER_1);
  }

  // --- GuildDeleted ---

  @Test
  void whenGuildDeleted_thenAllMembersReceiveEmailAndGuildIsRemoved() throws Exception {
    guildMemberRepository.addMember(GUILD_1, LEADER_1);
    guildMemberRepository.addMember(GUILD_1, MEMBER_1);

    publish("guild.deleted", new GuildDeletedMessage(GUILD_1, Instant.now()));

    // Two members → two emails
    MimeMessage[] mails = waitForEmails(2);

    assertThat(mails).hasSize(2);
    assertThat(mails).allSatisfy(mail -> assertThat(subjectOf(mail)).isEqualTo("Guild deleted"));

    // After deletion the guild should not exist in the repository
    assertThat(guildMemberRepository.findMembersByGuildId(GUILD_1)).isEmpty();
  }

  // --- RoleAssigned ---

  @Test
  void whenRoleAssigned_thenMemberReceivesEmailWithRoleName() throws Exception {
    publish(
        "guild.role-assigned",
        new RoleAssignedMessage(GUILD_1, MEMBER_1, "VICE_LEADER", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(subjectOf(mails[0])).isEqualTo("New role assigned!");
    assertThat(bodyOf(mails[0])).contains("VICE_LEADER");
  }

  // --- InviteSent ---

  @Test
  void whenInviteSent_thenTargetAvatarReceivesEmail() throws Exception {
    publish(
        "guild.invite-sent", new InviteSentMessage(GUILD_1, MEMBER_1, "invite-xyz", Instant.now()));

    MimeMessage[] mails = waitForEmails(1);

    assertThat(recipientOf(mails[0])).isEqualTo("member1@example.com");
    assertThat(subjectOf(mails[0])).isEqualTo("Guild invitation!");
    assertThat(bodyOf(mails[0])).contains(GUILD_1);
  }
}
