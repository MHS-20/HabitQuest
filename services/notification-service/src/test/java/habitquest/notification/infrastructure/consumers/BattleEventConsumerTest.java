package habitquest.notification.infrastructure.consumers;

import static org.assertj.core.api.Assertions.assertThat;

import habitquest.notification.infrastructure.consumers.guild.BattleEventConsumer;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BattleEventConsumerTest extends BaseConsumerIntegrationTest {

  public static final String BATTLE_1 = "battle-1";
  public static final String GUILD_1 = "guild-1";

  @BeforeEach
  void setUp() {
    resetGreenMail();
    // Three registered members with email addresses
    userEmailRepository.save("avatar-1", "guerriero@example.com");
    userEmailRepository.save("avatar-2", "mago@example.com");
    userEmailRepository.save("avatar-3", "ladro@example.com");
    // All three in the same guild
    guildMemberRepository.addMember(GUILD_1, "avatar-1");
    guildMemberRepository.addMember(GUILD_1, "avatar-2");
    guildMemberRepository.addMember(GUILD_1, "avatar-3");
  }

  @Test
  void whenBattleStarted_thenAllGuildMembersReceiveEmail() throws Exception {
    publish(
        "guild.battle-started",
        new BattleEventConsumer.BattleStartedMessage(BATTLE_1, GUILD_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(3);
    assertThat(mails).hasSize(3);
    assertThat(mails)
        .allSatisfy(mail -> assertThat(subjectOf(mail)).isEqualTo("Your guild is in battle!"));
    assertThat(Stream.of(mails).map(this::recipientOf))
        .containsExactlyInAnyOrder(
            "guerriero@example.com", "mago@example.com", "ladro@example.com");
  }

  @Test
  void whenBattleWon_thenAllGuildMembersReceiveCongratulations() throws Exception {
    publish(
        "guild.battle-won",
        new BattleEventConsumer.BattleWonMessage(BATTLE_1, GUILD_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(3);
    assertThat(mails).hasSize(3);
    assertThat(mails)
        .allSatisfy(mail -> assertThat(subjectOf(mail)).isEqualTo("Victory! Your guild won!"));
    assertThat(mails).allSatisfy(mail -> assertThat(bodyOf(mail)).contains(BATTLE_1));
  }

  @Test
  void whenBattleLost_thenAllGuildMembersReceiveEncouragement() throws Exception {
    publish(
        "guild.battle-lost",
        new BattleEventConsumer.BattleLostMessage(BATTLE_1, GUILD_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(3);
    assertThat(mails).hasSize(3);
    assertThat(mails)
        .allSatisfy(mail -> assertThat(subjectOf(mail)).isEqualTo("Your guild lost the battle"));
    assertThat(mails)
        .allSatisfy(mail -> assertThat(bodyOf(mail)).contains("We'll get them next time"));
  }

  @Test
  void whenBattleStarted_butGuildHasNoMembers_thenNoEmailIsSent() {
    publish(
        "guild.battle-started",
        new BattleEventConsumer.BattleStartedMessage("battle-2", "guild-vuota", Instant.now()));
    assertThat(greenMail.getReceivedMessages()).isEmpty();
  }

  @Test
  void whenBattleWon_andOneMemberHasNoEmail_thenOnlyRegisteredMembersReceiveEmail()
      throws Exception {
    guildMemberRepository.addMember(GUILD_1, "avatar-4-senza-email");
    publish(
        "guild.battle-won",
        new BattleEventConsumer.BattleWonMessage(BATTLE_1, GUILD_1, Instant.now()));
    MimeMessage[] mails = waitForEmails(3);
    assertThat(mails).hasSize(3);
  }
}
