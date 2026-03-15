package habitquest.guild.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GuildController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("GuildController")
public class GuildControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private GuildService guildService;

  // ── Fixtures ──────────────────────────────────────────────────────────────────

  private static final String GUILD_ID = "guild-1";
  private static final String GUILD_NAME = "Knights";
  private static final String CREATOR_AVATAR_ID = "avatar-1";
  private static final String CREATOR_NICKNAME = "Lancelot";
  private static final String MEMBER_ID = "avatar-2";
  private static final String MEMBER_NICKNAME = "Percival";
  private static final String UNKNOWN_ID = "ghost-99";

  private Guild stubGuild() {
    GuildMember leader = new GuildMember(CREATOR_AVATAR_ID, CREATOR_NICKNAME, GuildRole.LEADER);
    return new Guild(GUILD_ID, GUILD_NAME, leader);
  }

  private GuildMember stubMember() {
    return new GuildMember(MEMBER_ID, MEMBER_NICKNAME, GuildRole.MEMBER);
  }

  // ── POST /api/v1/guilds ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds")
  class CreateGuild {

    @Test
    @DisplayName("returns 201 with the new guild id")
    void shouldReturn201WithId() throws Exception {
      when(guildService.createGuild(GUILD_NAME, CREATOR_AVATAR_ID, CREATOR_NICKNAME))
          .thenReturn(GUILD_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name":"Knights","creatorAvatarId":"avatar-1","creatorNickname":"Lancelot"}
                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(GUILD_ID));
    }

    @Test
    @DisplayName("delegates all fields to the service")
    void shouldDelegateToService() throws Exception {
      when(guildService.createGuild(anyString(), anyString(), anyString())).thenReturn(GUILD_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name":"Knights","creatorAvatarId":"avatar-1","creatorNickname":"Lancelot"}
                      """))
          .andExpect(status().isCreated());

      verify(guildService).createGuild("Knights", "avatar-1", "Lancelot");
    }

    @Test
    @DisplayName("returns 400 when domain rejects the request")
    void shouldReturn400OnDomainError() throws Exception {
      when(guildService.createGuild(anyString(), anyString(), anyString()))
          .thenThrow(new IllegalArgumentException("Guild name cannot be blank"));

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"name":"","creatorAvatarId":"avatar-1","creatorNickname":"Lancelot"}
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Guild name cannot be blank"));
    }
  }

  // ── GET /api/v1/guilds/{id} ───────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/{id}")
  class GetGuild {

    @Test
    @DisplayName("returns 200 with guild data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(guildService.getGuild(GUILD_ID)).thenReturn(stubGuild());

      mockMvc
          .perform(get("/api/v1/guilds/{id}", GUILD_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value(GUILD_NAME));
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getGuild(UNKNOWN_ID)).thenThrow(new GuildNotFoundException(UNKNOWN_ID));

      mockMvc.perform(get("/api/v1/guilds/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  // ── DELETE /api/v1/guilds/{id} ────────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/guilds/{id}")
  class DeleteGuild {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(guildService).deleteGuild(GUILD_ID);

      mockMvc.perform(delete("/api/v1/guilds/{id}", GUILD_ID)).andExpect(status().isNoContent());

      verify(guildService).deleteGuild(GUILD_ID);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_ID)).when(guildService).deleteGuild(UNKNOWN_ID);

      mockMvc.perform(delete("/api/v1/guilds/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/guilds/{id}/members ──────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/{id}/members")
  class GetMembers {

    @Test
    @DisplayName("returns 200 with member list")
    void shouldReturn200WithMembers() throws Exception {
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(stubMember()));

      mockMvc.perform(get("/api/v1/guilds/{id}/members", GUILD_ID)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getMembers(UNKNOWN_ID)).thenThrow(new GuildNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/guilds/{id}/members", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/guilds/{id}/invites ─────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds/{id}/invites")
  class SendInvite {

    @Test
    @DisplayName("returns 204 when leader sends an invite")
    void shouldReturn204AndDelegate() throws Exception {
      doNothing().when(guildService).sendInvite(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"requestorId":"avatar-1","targetAvatarId":"avatar-2"}
                                      """))
          .andExpect(status().isNoContent());

      verify(guildService).sendInvite(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "sendInvite"))
          .when(guildService)
          .sendInvite(GUILD_ID, "not-a-leader", MEMBER_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"requestorId":"not-a-leader","targetAvatarId":"avatar-2"}
                                      """))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_ID))
          .when(guildService)
          .sendInvite(eq(UNKNOWN_ID), anyString(), anyString());

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", UNKNOWN_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"requestorId":"avatar-1","targetAvatarId":"avatar-2"}
                                      """))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when domain rejects the invite")
    void shouldReturn400OnDomainError() throws Exception {
      doThrow(new IllegalStateException("Avatar is already a member"))
          .when(guildService)
          .sendInvite(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"requestorId":"avatar-1","targetAvatarId":"avatar-2"}
                                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Avatar is already a member"));
    }
  }

  // ── POST /api/v1/guilds/{id}/invites/{inviteId}/accept ───────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds/{id}/invites/{inviteId}/accept")
  class AcceptInvite {

    private static final String INVITE_ID = "invite-1";

    @Test
    @DisplayName("returns 204 when avatar accepts a valid invite")
    void shouldReturn204AndDelegate() throws Exception {
      doNothing().when(guildService).acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICKNAME);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_ID, INVITE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"avatarId":"avatar-2","nickname":"Percival"}
                                      """))
          .andExpect(status().isNoContent());

      verify(guildService).acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICKNAME);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_ID))
          .when(guildService)
          .acceptInvite(eq(UNKNOWN_ID), eq(INVITE_ID), anyString(), anyString());

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", UNKNOWN_ID, INVITE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"avatarId":"avatar-2","nickname":"Percival"}
                                      """))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when invite is invalid or expired")
    void shouldReturn400WhenInviteInvalid() throws Exception {
      doThrow(new IllegalArgumentException("Invite not found or not yours"))
          .when(guildService)
          .acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICKNAME);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_ID, INVITE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"avatarId":"avatar-2","nickname":"Percival"}
                                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Invite not found or not yours"));
    }

    @Test
    @DisplayName("returns 400 when guild is full")
    void shouldReturn400WhenGuildFull() throws Exception {
      doThrow(new IllegalStateException("Guild has reached maximum capacity"))
          .when(guildService)
          .acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICKNAME);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_ID, INVITE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"avatarId":"avatar-2","nickname":"Percival"}
                                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Guild has reached maximum capacity"));
    }
  }

  // ── DELETE /api/v1/guilds/{id}/members/{memberId} ────────────────────────────
  @Nested
  @DisplayName("DELETE /api/v1/guilds/{id}/members/{memberId}")
  class RemoveMember {

    @Test
    @DisplayName("returns 204 when leader removes a member")
    void shouldReturn204() throws Exception {
      doNothing().when(guildService).removeMember(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID);

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", GUILD_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"requestorId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(guildService).removeMember(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "removeMember"))
          .when(guildService)
          .removeMember(GUILD_ID, "not-a-leader", MEMBER_ID);

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", GUILD_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"requestorId":"not-a-leader"}
                      """))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_ID))
          .when(guildService)
          .removeMember(eq(UNKNOWN_ID), anyString(), eq(MEMBER_ID));

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", UNKNOWN_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"requestorId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/guilds/{id}/members/{memberId}/leave ────────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds/{id}/members/{memberId}/leave")
  class LeaveGuild {

    @Test
    @DisplayName("returns 204 on successful leave")
    void shouldReturn204() throws Exception {
      doNothing().when(guildService).leaveGuild(GUILD_ID, MEMBER_ID);

      mockMvc
          .perform(post("/api/v1/guilds/{id}/members/{memberId}/leave", GUILD_ID, MEMBER_ID))
          .andExpect(status().isNoContent());

      verify(guildService).leaveGuild(GUILD_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("returns 400 when domain rejects the operation")
    void shouldReturn400WhenDomainRejects() throws Exception {
      doThrow(new IllegalStateException("Last owner cannot leave"))
          .when(guildService)
          .leaveGuild(GUILD_ID, MEMBER_ID);

      mockMvc
          .perform(post("/api/v1/guilds/{id}/members/{memberId}/leave", GUILD_ID, MEMBER_ID))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Last owner cannot leave"));
    }
  }

  // ── PATCH /api/v1/guilds/{id}/members/{memberId}/role ────────────────────────

  @Nested
  @DisplayName("PATCH /api/v1/guilds/{id}/members/{memberId}/role")
  class PromoteMember {

    @Test
    @DisplayName("returns 204 when leader promotes a member")
    void shouldReturn204AndDelegate() throws Exception {
      doNothing()
          .when(guildService)
          .promoteMember(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID, GuildRole.OFFICER);

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"roleName":"OFFICER","requestorId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(guildService).promoteMember(GUILD_ID, CREATOR_AVATAR_ID, MEMBER_ID, GuildRole.OFFICER);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "promoteMember"))
          .when(guildService)
          .promoteMember(GUILD_ID, "not-a-leader", MEMBER_ID, GuildRole.OFFICER);

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"roleName":"OFFICER","requestorId":"not-a-leader"}
                      """))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 400 when roleName is not a valid GuildRole")
    void shouldReturn400OnInvalidRole() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"roleName":"WIZARD","requestorId":"avatar-1"}
                      """))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(guildService);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_ID))
          .when(guildService)
          .promoteMember(eq(UNKNOWN_ID), anyString(), eq(MEMBER_ID), any(GuildRole.class));

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", UNKNOWN_ID, MEMBER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"roleName":"OFFICER","requestorId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/guilds/{id}/rank ──────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/{id}/rank")
  class GetGlobalRank {

    @Test
    @DisplayName("returns 200 with global rank")
    void shouldReturnRank() throws Exception {
      when(guildService.getGlobalRank(GUILD_ID)).thenReturn(3);

      mockMvc
          .perform(get("/api/v1/guilds/{id}/rank", GUILD_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.globalRank").value(3));
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getGlobalRank(UNKNOWN_ID))
          .thenThrow(new GuildNotFoundException(UNKNOWN_ID));

      mockMvc.perform(get("/api/v1/guilds/{id}/rank", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/guilds/leaderboard ───────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/leaderboard")
  class GetLeaderboard {

    @Test
    @DisplayName("returns 200 with ordered guild list")
    void shouldReturnLeaderboard() throws Exception {
      when(guildService.getGuildLeaderboard()).thenReturn(List.of(stubGuild()));

      mockMvc.perform(get("/api/v1/guilds/leaderboard")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when no guilds exist")
    void shouldReturnEmptyListWhenNoGuilds() throws Exception {
      when(guildService.getGuildLeaderboard()).thenReturn(List.of());

      mockMvc.perform(get("/api/v1/guilds/leaderboard")).andExpect(status().isOk());
    }
  }
}
