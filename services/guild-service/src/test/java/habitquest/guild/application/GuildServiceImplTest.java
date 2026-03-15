package habitquest.guild.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.factory.InviteFactory;
import habitquest.guild.domain.guild.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GuildServiceImpl")
@ExtendWith(MockitoExtension.class)
class GuildServiceImplTest {

  public static final String BATTLE_1 = "battle-1";
  private static final String GUILD_ID = "guild-1";
  private static final String GUILD_NAME = "MyGuild";
  private static final String LEADER_ID = "avatar-1";
  private static final String NICK = "Hero";
  private static final String MEMBER_ID = "avatar-2";
  private static final String MEMBER_NICK = "Newbie";
  private static final GuildRole LEADER_ROLE = GuildRole.LEADER;
  private static final GuildRole OFFICER_ROLE = GuildRole.OFFICER;
  private static final GuildRole MEMBER_ROLE = GuildRole.MEMBER;
  private static final String THROWS_WHEN_NOT_FOUND =
      "should throw GuildNotFoundException when guild does not exist";

  @Mock private GuildFactory guildFactory;
  @Mock private GuildRepository guildRepository;
  @Mock private GuildObserver guildObserver;
  @Mock private BattleService battleService;
  @Mock private InviteFactory inviteFactory;

  @InjectMocks private GuildServiceImpl guildService;

  private Guild guild;

  @BeforeEach
  void setUp() {
    guildService =
        new GuildServiceImpl(
            guildFactory, guildRepository, guildObserver, battleService, inviteFactory);

    guild = new Guild(GUILD_ID, GUILD_NAME, new GuildMember(LEADER_ID, NICK, LEADER_ROLE));
  }

  // ── createGuild ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createGuild")
  class CreateGuild {

    @Test
    @DisplayName("should delegate creation to the factory and save the result")
    void shouldCreateAndSave() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildService.createGuild(GUILD_NAME, LEADER_ID, NICK);

      verify(guildRepository).save(guild);
    }

    @Test
    @DisplayName("should return the id of the created guild")
    void shouldReturnGuildId() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      String result = guildService.createGuild(GUILD_NAME, LEADER_ID, NICK);

      assertThat(result).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should publish a GuildCreated event")
    void shouldPublishGuildCreatedEvent() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildService.createGuild(GUILD_NAME, LEADER_ID, NICK);

      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildCreated.class);
      assertThat(((GuildCreated) captor.getValue()).guildId()).isEqualTo(GUILD_ID);
    }
  }

  // ── getGuild ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getGuild")
  class GetGuild {

    @Test
    @DisplayName("should return the guild when it exists")
    void shouldReturnGuildWhenFound() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      Guild result = guildService.getGuild(GUILD_ID);

      assertThat(result).isSameAs(guild);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.getGuild(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── deleteGuild ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteGuild")
  class DeleteGuild {

    @Test
    @DisplayName("should delete from repository and publish GuildDeleted event")
    void shouldDeleteAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      guildService.deleteGuild(GUILD_ID);

      verify(guildRepository).deleteById(GUILD_ID);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildDeleted.class);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.deleteGuild(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── getMembers ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getMembers")
  class GetMembers {

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.getMembers(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── sendInvite ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("sendInvite")
  class SendInvite {

    private static final String INVITE_ID = "invite-1";

    @Test
    @DisplayName("should save guild and publish InviteSent event when leader sends invite")
    void shouldSaveAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(inviteFactory.create(GUILD_ID, MEMBER_ID))
          .thenReturn(new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));

      guildService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      InviteSent event = (InviteSent) captor.getValue();
      assertThat(event.guildId()).isEqualTo(GUILD_ID);
      assertThat(event.targetAvatarId()).isEqualTo(MEMBER_ID);
      assertThat(event.inviteId()).isEqualTo(INVITE_ID);
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(() -> guildService.sendInvite(GUILD_ID, MEMBER_ID, LEADER_ID))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName("should throw IllegalStateException when avatar is already a member")
    void shouldThrowWhenAlreadyMember() {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(inviteFactory.create(GUILD_ID, MEMBER_ID))
          .thenReturn(new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));

      assertThatThrownBy(() -> guildService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID))
          .isInstanceOf(IllegalStateException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> guildService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── acceptInvite ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("acceptInvite")
  class AcceptInvite {
    private static final String INVITE_ID = "invite-1";

    @BeforeEach
    void seedInvite() {
      guild.sendInvite(
          LEADER_ID, new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400)));
    }

    @Test
    @DisplayName("should add member, save and publish GuildJoined event")
    void shouldAddMemberAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      GuildJoined event = (GuildJoined) captor.getValue();
      assertThat(event.guildId()).isEqualTo(GUILD_ID);
      assertThat(event.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("should increase battle turns when a battle is ongoing")
    void shouldIncreaseTurnsWhenBattleOngoing() throws GuildNotFoundException {
      Battle ongoingBattle = mock(Battle.class);
      when(ongoingBattle.getId()).thenReturn(BATTLE_1);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(ongoingBattle));
      guildService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);
      verify(battleService).increaseNumOfTurn(BATTLE_1, MEMBER_ID);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when invite does not belong to the avatar")
    void shouldThrowWhenInviteNotOwnedByAvatar() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () -> guildService.acceptInvite(GUILD_ID, INVITE_ID, "wrong-avatar", MEMBER_NICK))
          .isInstanceOf(IllegalArgumentException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when invite id does not exist")
    void shouldThrowWhenInviteNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () -> guildService.acceptInvite(GUILD_ID, "ghost-invite", MEMBER_ID, MEMBER_NICK))
          .isInstanceOf(IllegalArgumentException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> guildService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── leaveGuild ────────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("leaveGuild")
  class LeaveGuild {

    @Test
    @DisplayName("should remove member, save and publish GuildLeft event")
    void shouldRemoveMemberAndPublishEvent() throws GuildNotFoundException {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.leaveGuild(GUILD_ID, LEADER_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildLeft.class);
    }

    @Test
    @DisplayName(
        "should publish GuildLeft and GuildDeleted, delete guild and battle when last member leaves")
    void shouldCleanUpWhenLastMemberLeaves() throws GuildNotFoundException {
      Battle ongoingBattle = mock(Battle.class);
      when(ongoingBattle.getId()).thenReturn(BATTLE_1);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(ongoingBattle));

      guildService.leaveGuild(GUILD_ID, LEADER_ID);

      verify(guildRepository).deleteById(GUILD_ID);
      verify(battleService).deleteBattle(BATTLE_1);

      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver, times(2)).notifyGuildEvent(captor.capture());
      assertThat(captor.getAllValues())
          .satisfiesExactly(
              first -> assertThat(first).isInstanceOf(GuildLeft.class),
              second -> assertThat(second).isInstanceOf(GuildDeleted.class));
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.leaveGuild(GUILD_ID, LEADER_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── removeMember ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("removeMember")
  class RemoveMember {

    @Test
    @DisplayName(
        "should remove member, save and publish RemovedFromGuild event when leader requests it")
    void shouldRemoveMemberAndPublishEvent() throws GuildNotFoundException {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.removeMember(GUILD_ID, LEADER_ID, MEMBER_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      RemovedFromGuild event = (RemovedFromGuild) captor.getValue();
      assertThat(event.memberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() throws GuildNotFoundException {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(() -> guildService.removeMember(GUILD_ID, MEMBER_ID, LEADER_ID))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.removeMember(GUILD_ID, LEADER_ID, MEMBER_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── promoteMember ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("promoteMember")
  class PromoteMember {

    @Test
    @DisplayName(
        "should promote member, save and publish RoleAssigned event when leader requests it")
    void shouldPromoteMemberAndPublishEvent() throws GuildNotFoundException {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      guildService.promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      RoleAssigned event = (RoleAssigned) captor.getValue();
      assertThat(event.memberId()).isEqualTo(MEMBER_ID);
      assertThat(event.newRole()).isEqualTo(OFFICER_ROLE);
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() throws GuildNotFoundException {
      guild.addMember(new GuildMember(MEMBER_ID, MEMBER_NICK, MEMBER_ROLE));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () -> guildService.promoteMember(GUILD_ID, MEMBER_ID, LEADER_ID, OFFICER_ROLE))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> guildService.promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── getGlobalRank ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getGlobalRank")
  class GetGlobalRank {

    @Test
    @DisplayName("should return the guild's current rank")
    void shouldReturnCurrentRank() throws GuildNotFoundException {
      guild.updateGlobalRank(7);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThat(guildService.getGlobalRank(GUILD_ID)).isEqualTo(7);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.getGlobalRank(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── getGuildLeaderboard ───────────────────────────────────────────────────────
  @Nested
  @DisplayName("getGuildLeaderboard")
  class GetGuildLeaderboard {

    @Test
    @DisplayName("should delegate to repository and return the sorted list")
    void shouldReturnLeaderboard() {
      when(guildRepository.findAllSortedByRank()).thenReturn(List.of(guild));

      assertThat(guildService.getGuildLeaderboard()).containsExactly(guild);
    }
  }
}
