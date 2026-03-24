package habitquest.guild;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.Invite;
import java.time.Instant;
import java.util.UUID;

public final class GuildFixtures {

  // Guild IDs
  public static final String GUILD_1        = "guild-1";
  public static final String UNKNOWN_GUILD  = "ghost-99";
  public static final String LEADER_1       = "avatar-1";
  public static final String MEMBER_1       = "avatar-2";
  public static final String INVITE_1       = "invite-1";
  public static final String BATTLE_1       = "battle-1";
  public static final String UNKNOWN_INVITE = "ghost-invite";

  // Battle IDs
  public static final String LEADER_HTTP_ID   = "leader-1";
  public static final String BATTLE_MEMBER_1  = "member-1";
  public static final String BATTLE_MEMBER_2  = "member-2";
  public static final String UNKNOWN_BATTLE   = "ghost-99";

  // JSON field-name keys
  public static final String JSON_KEY_BATTLE_ID  = "battleId";
  public static final String JSON_KEY_GUILD_ID   = "guildId";

  // ── Typed domain IDs ─────────────────────────────────────────────────────────
  public static final Id<Guild>       GUILD_ID          = new Id<>(GUILD_1);
  public static final Id<Guild>       UNKNOWN_GUILD_ID  = new Id<>(UNKNOWN_GUILD);
  public static final Id<GuildMember> LEADER_ID         = new Id<>(LEADER_1);
  public static final Id<GuildMember> MEMBER_ID         = new Id<>(MEMBER_1);
  public static final Id<Invite>      INVITE_ID         = new Id<>(INVITE_1);
  public static final Id<Invite>      UNKNOWN_INVITE_ID = new Id<>(UNKNOWN_INVITE);
  public static final Id<Battle>      BATTLE_ID         = new Id<>(BATTLE_1);
  public static final Id<Battle>      UNKNOWN_BATTLE_ID = new Id<>(UNKNOWN_BATTLE);

  public static final Id<GuildMember> LEADER_HTTP = new Id<>(LEADER_HTTP_ID);
  public static final Id<GuildMember> BATTLE_MEMBER_ID_1 = new Id<>(BATTLE_MEMBER_1);
  public static final Id<GuildMember> BATTLE_MEMBER_ID_2 = new Id<>(BATTLE_MEMBER_2);

  // Guild metadata
  public static final String    GUILD_NAME   = "MyGuild";
  public static final String    LEADER_NICK  = "Hero";
  public static final String    MEMBER_NICK  = "Newbie";
  public static final GuildRole LEADER_ROLE  = GuildRole.LEADER;
  public static final GuildRole OFFICER_ROLE = GuildRole.OFFICER;
  public static final GuildRole MEMBER_ROLE  = GuildRole.MEMBER;

  // Boss defaults
  public static final BossType BOSS_TYPE    = BossType.MINOTAUR;
  public static final int      BOSS_HEALTH  = BOSS_TYPE.stats().health().value();
  public static final int      EXP_REWARD   = BOSS_TYPE.experienceReward().amount();
  public static final int      MONEY_REWARD = BOSS_TYPE.moneyReward().amount();
  public static final int      PENALTY      = BOSS_TYPE.penalty().amount();

  // Guild factories
  public static Guild guild() {
    return new Guild(GUILD_ID, GUILD_NAME, leader());
  }

  public static Guild guildWithMembers(int n) {
    Guild guild = guild();
    for (int i = 0; i < n; i++) {
      guild.addMember(new GuildMember(new Id<>("member-" + i), "nick-" + i, MEMBER_ROLE));
    }
    return guild;
  }

  public static Guild guildWithMember() {
    Guild guild = guild();
    guild.addMember(member());
    return guild;
  }

  // Member factories
  public static GuildMember leader() {
    return new GuildMember(LEADER_ID, LEADER_NICK, LEADER_ROLE);
  }

  public static GuildMember member() {
    return new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE);
  }

  public static GuildMember officer() {
    return new GuildMember(MEMBER_ID, MEMBER_NICK, OFFICER_ROLE);
  }

  public static GuildMember avatarMember() {
    return new GuildMember(LEADER_ID, LEADER_NICK, MEMBER_ROLE);
  }

  // Battle factories
  public static Battle battle() {
    Battle b = new Battle(BATTLE_ID, GUILD_ID, BOSS_TYPE, 0);
    b.increaseNumOfTurns(LEADER_ID);
    return b;
  }

  public static Battle battleWithTwoMembers() {
    Battle b = new Battle(BATTLE_ID, GUILD_ID, BOSS_TYPE, 0);
    b.increaseNumOfTurns(BATTLE_MEMBER_ID_1);
    b.increaseNumOfTurns(BATTLE_MEMBER_ID_2);
    return b;
  }

  // Invite factories
  public static Invite invite() {
    return new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400));
  }

  public static Invite inviteFor(Id<GuildMember> target) {
    return new Invite(
            new Id<>(UUID.randomUUID().toString()), GUILD_ID, target, Instant.now().plusSeconds(86400));
  }

  public static Invite expiredInvite() {
    return new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().minusSeconds(10));
  }

  // Utility helpers
  public static void addMember(Guild guild) {
    guild.addMember(member());
  }

  public static void addLeaderInvite(Guild guild) {
    guild.sendInvite(LEADER_ID, invite());
  }

  public static void addInvite(Guild guild, Invite invite) {
    guild.sendInvite(LEADER_ID, invite);
  }

  // Prevent instantiation
  private GuildFixtures() {
    throw new UnsupportedOperationException("utility class");
  }
}