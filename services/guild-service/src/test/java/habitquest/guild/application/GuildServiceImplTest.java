package habitquest.guild.application;

import static habitquest.guild.GuildFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.BattleCommandService;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.out.AvatarClientPort;
import habitquest.guild.application.port.out.GuildRepository;
import habitquest.guild.application.service.GuildCommandServiceImpl;
import habitquest.guild.application.service.GuildQueryServiceImpl;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.factory.InviteFactory;
import habitquest.guild.domain.guild.*;
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

  private static final String THROWS_WHEN_NOT_FOUND =
      "should throw GuildNotFoundException when guild does not exist";

  @Mock private GuildFactory guildFactory;
  @Mock private GuildRepository guildRepository;
  @Mock private GuildObserver guildObserver;
  @Mock private BattleCommandService battleCommandService;
  @Mock private BattleQueryService battleQueryService;
  @Mock private InviteFactory inviteFactory;
  @Mock private AvatarClientPort avatarPort;

  @InjectMocks private GuildCommandServiceImpl guildCommandService;
  @InjectMocks private GuildQueryServiceImpl guildQueryService;

  private Guild guild;

  @BeforeEach
  void setUp() {
    guild = guild();
  }

  // ── createGuild ───────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createGuild")
  class CreateGuild {

    @Test
    @DisplayName("should delegate creation to the factory and save the result")
    void shouldCreateAndSave() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildCommandService.createGuild(GUILD_NAME, LEADER_ID, LEADER_NICK);

      verify(guildRepository).save(guild);
    }

    @Test
    @DisplayName("should return the id of the created guild")
    void shouldReturnGuildId() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      Id<Guild> result = guildCommandService.createGuild(GUILD_NAME, LEADER_ID, LEADER_NICK);

      assertThat(result).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should publish a GuildCreated event")
    void shouldPublishGuildCreatedEvent() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildCommandService.createGuild(GUILD_NAME, LEADER_ID, LEADER_NICK);

      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildCreated.class);
      assertThat(((GuildCreated) captor.getValue()).guildId().value()).isEqualTo(GUILD_1);
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

      Guild result = guildQueryService.getGuild(GUILD_ID);

      assertThat(result).isSameAs(guild);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildQueryService.getGuild(GUILD_ID))
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

      guildCommandService.deleteGuild(GUILD_ID);

      verify(guildRepository).deleteById(GUILD_ID);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildDeleted.class);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildCommandService.deleteGuild(GUILD_ID))
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

      assertThatThrownBy(() -> guildQueryService.getMembers(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── sendInvite ────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("sendInvite")
  class SendInvite {

    @Test
    @DisplayName(
        "should save guild, publish InviteSent event and notify avatar when leader sends invite")
    void shouldSaveAndPublishEvent() throws GuildNotFoundException {
      Invite invite = invite();
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(inviteFactory.create(GUILD_ID, MEMBER_ID)).thenReturn(invite);

      guildCommandService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      InviteSent event = (InviteSent) captor.getValue();
      assertThat(event.guildId().value()).isEqualTo(GUILD_1);
      assertThat(event.targetAvatarId().value()).isEqualTo(MEMBER_1);
      assertThat(event.inviteId().value()).isEqualTo(INVITE_1);
      verify(avatarPort)
          .sendInviteToAvatar(INVITE_1, MEMBER_1, GUILD_1, guild.getName(), invite.expiresAt());
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(() -> guildCommandService.sendInvite(GUILD_ID, MEMBER_ID, LEADER_ID))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName("should throw IllegalStateException when avatar is already a member")
    void shouldThrowWhenAlreadyMember() {
      addMember(guild); // Fixture helper
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(inviteFactory.create(GUILD_ID, MEMBER_ID)).thenReturn(invite());

      assertThatThrownBy(() -> guildCommandService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID))
          .isInstanceOf(IllegalStateException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> guildCommandService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // ── acceptInvite ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("acceptInvite")
  class AcceptInvite {

    @BeforeEach
    void seedInvite() {
      addLeaderInvite(guild); // Fixture helper
    }

    @Test
    @DisplayName("should add member, save and publish GuildJoined event")
    void shouldAddMemberAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildCommandService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      GuildJoined event = (GuildJoined) captor.getValue();
      assertThat(event.guildId().value()).isEqualTo(GUILD_1);
      assertThat(event.memberId().value()).isEqualTo(MEMBER_1);
    }

    @Test
    @DisplayName("should increase battle turns when a battle is ongoing")
    void shouldIncreaseTurnsWhenBattleOngoing() throws GuildNotFoundException {
      Battle ongoingBattle = mock(Battle.class);
      when(ongoingBattle.getId()).thenReturn(BATTLE_ID);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(ongoingBattle));

      guildCommandService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      verify(battleCommandService).increaseNumOfTurn(BATTLE_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when invite does not belong to the avatar")
    void shouldThrowWhenInviteNotOwnedByAvatar() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () ->
                  guildCommandService.acceptInvite(
                      GUILD_ID, INVITE_ID, new Id<>("wrong-avatar"), MEMBER_NICK))
          .isInstanceOf(IllegalArgumentException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when invite id does not exist")
    void shouldThrowWhenInviteNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () ->
                  guildCommandService.acceptInvite(
                      GUILD_ID, UNKNOWN_INVITE_ID, MEMBER_ID, MEMBER_NICK))
          .isInstanceOf(IllegalArgumentException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(
              () -> guildCommandService.acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK))
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
      addMember(guild);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildCommandService.leaveGuild(GUILD_ID, MEMBER_ID);

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
      when(ongoingBattle.getId()).thenReturn(BATTLE_ID);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(ongoingBattle));

      guildCommandService.leaveGuild(GUILD_ID, LEADER_ID);

      verify(guildRepository).deleteById(GUILD_ID);
      verify(battleCommandService).deleteBattle(BATTLE_ID);

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

      assertThatThrownBy(() -> guildCommandService.leaveGuild(GUILD_ID, LEADER_ID))
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
      addMember(guild);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildCommandService.removeMember(GUILD_ID, LEADER_ID, MEMBER_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      RemovedFromGuild event = (RemovedFromGuild) captor.getValue();
      assertThat(event.memberId().value()).isEqualTo(MEMBER_1);
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() throws GuildNotFoundException {
      addMember(guild);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(() -> guildCommandService.removeMember(GUILD_ID, MEMBER_ID, LEADER_ID))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildCommandService.removeMember(GUILD_ID, LEADER_ID, MEMBER_ID))
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
      addMember(guild);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      guildCommandService.promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      RoleAssigned event = (RoleAssigned) captor.getValue();
      assertThat(event.memberId().value()).isEqualTo(MEMBER_1);
      assertThat(event.newRole()).isEqualTo(OFFICER_ROLE);
    }

    @Test
    @DisplayName("should throw UnauthorizedGuildOperationException when requestor is not a leader")
    void shouldThrowWhenNotLeader() throws GuildNotFoundException {
      addMember(guild);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      assertThatThrownBy(
              () -> guildCommandService.promoteMember(GUILD_ID, MEMBER_ID, LEADER_ID, OFFICER_ROLE))
          .isInstanceOf(UnauthorizedGuildOperationException.class);

      verify(guildRepository, never()).save(any());
      verify(guildObserver, never()).notifyGuildEvent(any());
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> guildCommandService.promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE))
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

      assertThat(guildQueryService.getGlobalRank(GUILD_ID)).isEqualTo(7);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildQueryService.getGlobalRank(GUILD_ID))
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

      assertThat(guildQueryService.getGuildLeaderboard()).containsExactly(guild);
    }
  }
}
