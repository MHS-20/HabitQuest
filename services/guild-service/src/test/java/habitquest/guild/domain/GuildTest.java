package habitquest.guild.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.guild.domain.guild.*;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Guild")
class GuildTest {

  private static final String GUILD_ID = "guild-1";
  private static final String GUILD_NAME = "Guild of Heroes";
  private static final String LEADER_ID = "avatar-1";
  private static final String MEMBER_ID = "avatar-2";
  private static final String MEMBER_ID_2 = "avatar-3";
  private static final String MEMBER_NICK = "Nick2";
  private static final String MEMBER_NICK_2 = "Nick3";
  private static final String UNKNOWN_MEMBER_ID = "non-existent-id";
  private static final GuildRole LEADER_ROLE = GuildRole.LEADER;
  private static final GuildRole MEMBER_ROLE = GuildRole.MEMBER;
  private static final GuildRole OFFICER_ROLE = GuildRole.OFFICER;

  private GuildMember leader;
  private Guild guild;

  @BeforeEach
  void setUp() {
    leader = new GuildMember(LEADER_ID, "LeaderNick", LEADER_ROLE);
    guild = new Guild(GUILD_ID, GUILD_NAME, leader);
  }

  // ── Creation ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("creation")
  class Creation {

    @Test
    @DisplayName("should have the correct id")
    void shouldHaveCorrectId() {
      assertThat(guild.getId()).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should contain the leader as first member")
    void shouldContainLeaderAsMember() {
      assertThat(guild.getMembers()).hasSize(1);
      assertThat(guild.getMembers().get(0).getId()).isEqualTo(LEADER_ID);
    }

    @Test
    @DisplayName("should start with globalRank zero")
    void shouldStartWithRankZero() {
      assertThat(guild.getGlobalRank()).isEqualTo(0);
    }
  }

  // ── addMember ─────────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("addMember")
  class AddMember {

    @Test
    @DisplayName("should add a new member to the guild")
    void shouldAddNewMember() {
      guild.addMember(new GuildMember(MEMBER_ID, "NewGuy", MEMBER_ROLE));

      assertThat(guild.getMembers()).hasSize(2);
      assertThat(guild.getMembers()).extracting(GuildMember::getId).contains(MEMBER_ID);
    }

    @Test
    @DisplayName("should allow adding multiple members")
    void shouldAddMultipleMembers() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      guild.addMember(new GuildMember(MEMBER_ID_2, MEMBER_NICK_2, MEMBER_ROLE));

      assertThat(guild.getMembers()).hasSize(3);
    }
  }

  // ── sendInvite ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("sendInvite(invite)")
  class SendInvite {

    private static final String INVITE_ID = "invite-1";

    @Test
    @DisplayName("should add the invite to pending invites")
    void shouldAddInviteToPending() {
      guild.sendInvite(
          LEADER_ID, new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));

      assertThat(guild.getPendingInvites()).extracting(Invite::inviteId).containsExactly(INVITE_ID);
    }

    @Test
    @DisplayName("should throw when avatar is already a member")
    void shouldThrowWhenAlreadyMember() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      assertThatThrownBy(
              () ->
                  guild.sendInvite(
                      LEADER_ID,
                      new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400))))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("should not modify pending invites when avatar is already a member")
    void shouldNotAddInviteWhenAlreadyMember() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));

      assertThatThrownBy(
              () ->
                  guild.sendInvite(
                      LEADER_ID,
                      new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400))))
          .isInstanceOf(IllegalStateException.class);

      assertThat(guild.getPendingInvites()).isEmpty();
    }

    @Test
    @DisplayName("should allow multiple pending invites for different avatars")
    void shouldAllowMultiplePendingInvites() {
      guild.sendInvite(
          LEADER_ID, new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));
      guild.sendInvite(
          LEADER_ID,
          new Invite("invite-2", GUILD_ID, MEMBER_ID_2, Instant.now().plusSeconds(86400)));
      assertThat(guild.getPendingInvites()).hasSize(2);
    }
  }

  // ── acceptInvite ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("acceptInvite(inviteId, avatarId, nickname)")
  class AcceptInvite {

    private static final String INVITE_ID = "invite-1";

    @BeforeEach
    void seedInvite() {
      guild.sendInvite(
          LEADER_ID, new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));
    }

    @Test
    @DisplayName("should add the avatar as a member with MEMBER role")
    void shouldAddAvatarAsMember() {
      guild.acceptInvite(INVITE_ID, MEMBER_ID, MEMBER_NICK);

      assertThat(guild.getMembers()).extracting(GuildMember::getId).contains(MEMBER_ID);
    }

    @Test
    @DisplayName("should assign MEMBER role to the new member")
    void shouldAssignMemberRole() {
      guild.acceptInvite(INVITE_ID, MEMBER_ID, MEMBER_NICK);

      GuildMember joined =
          guild.getMembers().stream()
              .filter(m -> m.getId().equals(MEMBER_ID))
              .findFirst()
              .orElseThrow();
      assertThat(joined.getRole()).isEqualTo(MEMBER_ROLE);
    }

    @Test
    @DisplayName("should remove the invite from pending after acceptance")
    void shouldRemoveInviteFromPending() {
      guild.acceptInvite(INVITE_ID, MEMBER_ID, MEMBER_NICK);

      assertThat(guild.getPendingInvites()).extracting(Invite::inviteId).doesNotContain(INVITE_ID);
    }

    @Test
    @DisplayName("should throw when invite id does not exist")
    void shouldThrowWhenInviteNotFound() {
      assertThatThrownBy(() -> guild.acceptInvite("ghost-invite", MEMBER_ID, MEMBER_NICK))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw when invite belongs to a different avatar")
    void shouldThrowWhenInviteNotOwnedByAvatar() {
      assertThatThrownBy(() -> guild.acceptInvite(INVITE_ID, MEMBER_ID_2, MEMBER_NICK_2))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should not add member when invite ownership check fails")
    void shouldNotAddMemberWhenOwnershipFails() {
      assertThatThrownBy(() -> guild.acceptInvite(INVITE_ID, MEMBER_ID_2, MEMBER_NICK_2))
          .isInstanceOf(IllegalArgumentException.class);

      assertThat(guild.getMembers()).extracting(GuildMember::getId).doesNotContain(MEMBER_ID_2);
    }

    @Test
    @DisplayName("should not remove invite when acceptance fails")
    void shouldNotRemoveInviteWhenFails() {
      assertThatThrownBy(() -> guild.acceptInvite(INVITE_ID, MEMBER_ID_2, MEMBER_NICK_2))
          .isInstanceOf(IllegalArgumentException.class);

      assertThat(guild.getPendingInvites()).extracting(Invite::inviteId).containsExactly(INVITE_ID);
    }
  }

  // ── Leave guild ────────────────────────────────────────────────────

  @Nested
  @DisplayName("leaveGuild(memberId)")
  class RemoveMemberNoAuth {

    @Test
    @DisplayName("should remove an existing member by id")
    void shouldRemoveExistingMember() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      guild.leaveGuild(MEMBER_ID);
      assertThat(guild.getMembers()).extracting(GuildMember::getId).doesNotContain(MEMBER_ID);
    }

    @Test
    @DisplayName("should not affect other members when removing one")
    void shouldNotAffectOtherMembers() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      guild.addMember(new GuildMember(MEMBER_ID_2, MEMBER_NICK_2, MEMBER_ROLE));
      guild.leaveGuild(MEMBER_ID);
      assertThat(guild.getMembers()).hasSize(2);
      assertThat(guild.getMembers())
          .extracting(GuildMember::getId)
          .contains(LEADER_ID, MEMBER_ID_2);
    }

    @Test
    @DisplayName("should do nothing when member id does not exist")
    void shouldDoNothingForUnknownMemberId() {
      guild.leaveGuild(UNKNOWN_MEMBER_ID);
      assertThat(guild.getMembers()).hasSize(1);
    }
  }

  // ── removeMember (with auth) ──────────────────────────────────────────────────

  @Nested
  @DisplayName("removeMember(requestorId, memberId)")
  class RemoveMemberWithAuth {

    @Test
    @DisplayName("should remove the member when requestor is leader")
    void shouldRemoveMemberWhenLeader() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));

      guild.removeMember(LEADER_ID, MEMBER_ID);

      assertThat(guild.getMembers()).extracting(GuildMember::getId).doesNotContain(MEMBER_ID);
    }

    @Test
    @DisplayName("should throw when requestor is not a leader")
    void shouldThrowWhenRequestorIsNotLeader() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));

      assertThatThrownBy(() -> guild.removeMember(MEMBER_ID, MEMBER_ID_2))
          .isInstanceOf(UnauthorizedGuildOperationException.class);
    }

    @Test
    @DisplayName("should not modify the guild when requestor is not a leader")
    void shouldNotModifyGuildWhenUnauthorized() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      int sizeBefore = guild.getMembers().size();

      assertThatThrownBy(() -> guild.removeMember(MEMBER_ID, MEMBER_ID_2))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      assertThat(guild.getMembers()).hasSize(sizeBefore);
    }
  }

  // ── updateGlobalRank ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateGlobalRank")
  class UpdateGlobalRank {

    @Test
    @DisplayName("should update rank to the given value")
    void shouldUpdateRank() {
      guild.updateGlobalRank(42);

      assertThat(guild.getGlobalRank()).isEqualTo(42);
    }

    @Test
    @DisplayName("should allow updating rank multiple times")
    void shouldAllowMultipleUpdates() {
      guild.updateGlobalRank(10);
      guild.updateGlobalRank(5);

      assertThat(guild.getGlobalRank()).isEqualTo(5);
    }
  }

  // ── promoteMember (with auth) ─────────────────────────────────────────────────
  @Nested
  @DisplayName("promoteMember(requestorId, memberId, newRole)")
  class PromoteMemberWithAuth {

    @Test
    @DisplayName("should promote the member when requestor is leader")
    void shouldPromoteMemberWhenLeader() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));

      guild.promoteMember(LEADER_ID, MEMBER_ID, OFFICER_ROLE);

      GuildMember promoted =
          guild.getMembers().stream()
              .filter(m -> m.getId().equals(MEMBER_ID))
              .findFirst()
              .orElseThrow();
      assertThat(promoted.getRole()).isEqualTo(OFFICER_ROLE);
    }

    @Test
    @DisplayName("should throw when requestor is not a leader")
    void shouldThrowWhenRequestorIsNotLeader() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      assertThatThrownBy(() -> guild.promoteMember(MEMBER_ID, MEMBER_ID_2, OFFICER_ROLE))
          .isInstanceOf(UnauthorizedGuildOperationException.class);
    }

    @Test
    @DisplayName("should not change any role when requestor is not a leader")
    void shouldNotChangeRolesWhenUnauthorized() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      assertThatThrownBy(() -> guild.promoteMember(MEMBER_ID, MEMBER_ID_2, OFFICER_ROLE))
          .isInstanceOf(UnauthorizedGuildOperationException.class);
      assertThat(guild.getMembers())
          .extracting(GuildMember::getRole)
          .containsExactlyInAnyOrder(LEADER_ROLE, MEMBER_ROLE);
    }
  }
}
