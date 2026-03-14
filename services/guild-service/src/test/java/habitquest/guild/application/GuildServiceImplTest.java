package habitquest.guild.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
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

  private static final String GUILD_ID = "guild-1";
  private static final String GUILD_NAME = "MyGuild";
  private static final String AVATAR_ID = "avatar-1";
  private static final String NICK = "Hero";
  private static final String MEMBER_AVATAR_ID = "avatar-2";
  private static final String MEMBER_NICK = "Newbie";
  private static final String MEMBER_ROLE_NAME = "Member";
  private static final GuildRole LEADER_ROLE = new GuildRole("Leader");
  private static final GuildRole OFFICER_ROLE = new GuildRole("Officer");
  private static final String THROWS_WHEN_NOT_FOUND =
      "should throw GuildNotFoundException when guild does not exist";

  @Mock private GuildFactory guildFactory;
  @Mock private GuildRepository guildRepository;
  @Mock private GuildObserver guildObserver;

  @Mock
  private BattleService
      battleService; // was missing — caused NPE in addMember/leaveGuild/removeMember

  @InjectMocks private GuildServiceImpl guildService;

  private Guild guild;
  private GuildMember leader;

  @BeforeEach
  void setUp() {
    leader = new GuildMember(AVATAR_ID, NICK, LEADER_ROLE);
    guild = new Guild(GUILD_ID, GUILD_NAME, leader);
  }

  // -------------------------------------------------------------------------
  // createGuild
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("createGuild")
  class CreateGuild {

    @Test
    @DisplayName("should delegate creation to the factory and save the result")
    void shouldCreateAndSave() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildService.createGuild(GUILD_NAME, AVATAR_ID, NICK);

      verify(guildRepository).save(guild);
    }

    @Test
    @DisplayName("should return the id of the created guild")
    void shouldReturnGuildId() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      String result = guildService.createGuild(GUILD_NAME, AVATAR_ID, NICK);

      assertThat(result).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should publish a GuildCreated event")
    void shouldPublishGuildCreatedEvent() {
      when(guildFactory.create(any(), any(), any())).thenReturn(guild);

      guildService.createGuild(GUILD_NAME, AVATAR_ID, NICK);

      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildCreated.class);
      assertThat(((GuildCreated) captor.getValue()).guildId()).isEqualTo(GUILD_ID);
    }
  }

  // -------------------------------------------------------------------------
  // getGuild
  // -------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------
  // updateGuild
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("updateGuild")
  class UpdateGuild {

    @Test
    @DisplayName("should update the global rank and save")
    void shouldUpdateRankAndSave() throws GuildNotFoundException {
      Guild request = new Guild(GUILD_ID, GUILD_NAME, leader);
      request.updateGlobalRank(10);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      guildService.updateGuild(GUILD_ID, request);

      assertThat(guild.getGlobalRank()).isEqualTo(10);
      verify(guildRepository).save(guild);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.updateGuild(GUILD_ID, guild))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // deleteGuild
  // -------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------
  // getMembers
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("getMembers")
  class GetMembers {

    @Test
    @DisplayName("should return the member list of the guild")
    void shouldReturnMemberList() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      List<GuildMember> members = guildService.getMembers(GUILD_ID);

      assertThat(members).containsExactly(leader);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.getMembers(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // addMember
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("addMember")
  class AddMember {

    @Test
    @DisplayName("should add member, save and publish GuildJoined event")
    void shouldAddMemberAndPublishEvent() throws GuildNotFoundException {
      GuildMember newMember =
          new GuildMember(MEMBER_AVATAR_ID, MEMBER_NICK, new GuildRole(MEMBER_ROLE_NAME));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      // Simulate no ongoing battle so the turn-increase branch is skipped cleanly.
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.addMember(GUILD_ID, newMember);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildJoined.class);
      assertThat(((GuildJoined) captor.getValue()).memberId()).isEqualTo(MEMBER_AVATAR_ID);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      GuildMember newMember =
          new GuildMember(MEMBER_AVATAR_ID, MEMBER_NICK, new GuildRole(MEMBER_ROLE_NAME));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.addMember(GUILD_ID, newMember))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // leaveGuild
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("leaveGuild")
  class LeaveGuild {

    @Test
    @DisplayName("should remove member, save and publish GuildLeft event")
    void shouldRemoveMemberAndPublishEvent() throws GuildNotFoundException {
      guild.addMember(
          new GuildMember(MEMBER_AVATAR_ID, MEMBER_NICK, new GuildRole(MEMBER_ROLE_NAME)));
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.leaveGuild(GUILD_ID, AVATAR_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(GuildLeft.class);
    }

    @Test
    @DisplayName(
        "should publish GuildLeft and GuildDeleted and delete guild and battle when last member leaves")
    void shouldCleanUpWhenLastMemberLeaves() throws GuildNotFoundException {
      Battle ongoingBattle = mock(Battle.class);
      when(ongoingBattle.getId()).thenReturn("battle-1");
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(ongoingBattle));

      guildService.leaveGuild(GUILD_ID, AVATAR_ID);

      verify(guildRepository).deleteById(GUILD_ID);
      verify(battleService).deleteBattle("battle-1");

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

      assertThatThrownBy(() -> guildService.leaveGuild(GUILD_ID, AVATAR_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // removeMember
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("removeMember")
  class RemoveMember {

    @Test
    @DisplayName("should remove member, save and publish RemovedFromGuild event")
    void shouldRemoveMemberAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));
      // Simulate no ongoing battle so the turn-decrease branch is skipped cleanly.
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.empty());

      guildService.removeMember(GUILD_ID, AVATAR_ID);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(RemovedFromGuild.class);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.removeMember(GUILD_ID, AVATAR_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // promoteMember
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("promoteMember")
  class PromoteMember {

    @Test
    @DisplayName("should promote member, save and publish RoleAssigned event")
    void shouldPromoteMemberAndPublishEvent() throws GuildNotFoundException {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      guildService.promoteMember(GUILD_ID, AVATAR_ID, OFFICER_ROLE);

      verify(guildRepository).save(guild);
      ArgumentCaptor<GuildEvent> captor = ArgumentCaptor.forClass(GuildEvent.class);
      verify(guildObserver).notifyGuildEvent(captor.capture());
      RoleAssigned event = (RoleAssigned) captor.getValue();
      assertThat(event.memberId()).isEqualTo(AVATAR_ID);
      assertThat(event.newRole()).isEqualTo(OFFICER_ROLE);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.promoteMember(GUILD_ID, AVATAR_ID, OFFICER_ROLE))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // getGlobalRank
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("getGlobalRank")
  class GetGlobalRank {

    @Test
    @DisplayName("should return the guild's current rank")
    void shouldReturnCurrentRank() throws GuildNotFoundException {
      guild.updateGlobalRank(7);
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.of(guild));

      Integer rank = guildService.getGlobalRank(GUILD_ID);

      assertThat(rank).isEqualTo(7);
    }

    @Test
    @DisplayName(THROWS_WHEN_NOT_FOUND)
    void shouldThrowWhenNotFound() {
      when(guildRepository.findById(GUILD_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> guildService.getGlobalRank(GUILD_ID))
          .isInstanceOf(GuildNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // getGuildLeaderboard
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("getGuildLeaderboard")
  class GetGuildLeaderboard {

    @Test
    @DisplayName("should delegate to repository and return the sorted list")
    void shouldReturnLeaderboard() {
      when(guildRepository.findAllSortedByRank()).thenReturn(List.of(guild));

      List<Guild> result = guildService.getGuildLeaderboard();

      assertThat(result).containsExactly(guild);
    }
  }
}
