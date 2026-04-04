package habitquest.guild.infrastructure;

import static habitquest.guild.GuildFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.guild.application.GuildLogger;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.guild.*;
import java.time.Instant;
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
  @MockitoBean private GuildLogger log;
  @MockitoBean private AvatarClient avatarClient;

  // ── POST /api/v1/guilds ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds")
  class CreateGuild {

    @Test
    @DisplayName("returns 201 with the new guild id")
    void shouldReturn201WithId() throws Exception {
      when(guildService.createGuild(GUILD_NAME, LEADER_ID, LEADER_NICK)).thenReturn(GUILD_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"name\":\"%s\",\"creatorAvatarId\":\"%s\",\"creatorNickname\":\"%s\"}",
                          GUILD_NAME, LEADER_1, LEADER_NICK)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(GUILD_1));
    }

    @Test
    @DisplayName("delegates all fields to the service")
    void shouldDelegateToService() throws Exception {
      when(guildService.createGuild(anyString(), any(Id.class), anyString())).thenReturn(GUILD_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"name\":\"%s\",\"creatorAvatarId\":\"%s\",\"creatorNickname\":\"%s\"}",
                          GUILD_NAME, LEADER_1, LEADER_NICK)))
          .andExpect(status().isCreated());

      verify(guildService).createGuild(GUILD_NAME, LEADER_ID, LEADER_NICK);
    }

    @Test
    @DisplayName("returns 400 when domain rejects the request")
    void shouldReturn400OnDomainError() throws Exception {
      when(guildService.createGuild(anyString(), any(Id.class), anyString()))
          .thenThrow(new IllegalArgumentException("Guild name cannot be blank"));

      mockMvc
          .perform(
              post("/api/v1/guilds")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"name\":\"\",\"creatorAvatarId\":\"%s\",\"creatorNickname\":\"%s\"}",
                          LEADER_1, LEADER_NICK)))
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
      when(guildService.getGuild(GUILD_ID)).thenReturn(guild());

      mockMvc
          .perform(get("/api/v1/guilds/{id}", GUILD_1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value(GUILD_NAME));
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getGuild(UNKNOWN_GUILD_ID))
          .thenThrow(new GuildNotFoundException(UNKNOWN_GUILD));

      mockMvc.perform(get("/api/v1/guilds/{id}", UNKNOWN_GUILD)).andExpect(status().isNotFound());
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

      mockMvc.perform(delete("/api/v1/guilds/{id}", GUILD_1)).andExpect(status().isNoContent());

      verify(guildService).deleteGuild(GUILD_ID);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_GUILD))
          .when(guildService)
          .deleteGuild(UNKNOWN_GUILD_ID);

      mockMvc
          .perform(delete("/api/v1/guilds/{id}", UNKNOWN_GUILD))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/guilds/{id}/members ──────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/{id}/members")
  class GetMembers {

    @Test
    @DisplayName("returns 200 with member list")
    void shouldReturn200WithMembers() throws Exception {
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(member()));

      mockMvc.perform(get("/api/v1/guilds/{id}/members", GUILD_1)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getMembers(UNKNOWN_GUILD_ID))
          .thenThrow(new GuildNotFoundException(UNKNOWN_GUILD));

      mockMvc
          .perform(get("/api/v1/guilds/{id}/members", UNKNOWN_GUILD))
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
      Invite mockInvite =
          new Invite(INVITE_ID, GUILD_ID, MEMBER_ID, Instant.now().plusSeconds(86400));
      when(guildService.sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID)).thenReturn(mockInvite);
      // when(guildService.getGuild(GUILD_ID)).thenReturn(GUILD_1);
      doReturn(guild()).when(guildService).getGuild(GUILD_ID);
      doNothing().when(avatarClient).sendInviteToAvatar(any(), any(), any(), any(), any());

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"requestorId\":\"%s\",\"targetAvatarId\":\"%s\"}",
                          LEADER_1, MEMBER_1)))
          .andExpect(status().isNoContent());

      verify(guildService).sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID);
      verify(avatarClient).sendInviteToAvatar(any(), eq(MEMBER_1), eq(GUILD_1), any(), any());
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<GuildMember> notLeaderId = new Id<>("not-a-leader");
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "sendInvite"))
          .when(guildService)
          .sendInvite(eq(GUILD_ID), eq(notLeaderId), eq(MEMBER_ID));

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"requestorId\":\"%s\",\"targetAvatarId\":\"%s\"}",
                          "not-a-leader", MEMBER_1)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_GUILD))
          .when(guildService)
          .sendInvite(eq(UNKNOWN_GUILD_ID), any(Id.class), any(Id.class));

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", UNKNOWN_GUILD)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"requestorId\":\"%s\",\"targetAvatarId\":\"%s\"}",
                          LEADER_1, MEMBER_1)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when domain rejects the invite")
    void shouldReturn400OnDomainError() throws Exception {
      doThrow(new IllegalStateException("Avatar is already a member"))
          .when(guildService)
          .sendInvite(GUILD_ID, LEADER_ID, MEMBER_ID);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites", GUILD_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"requestorId\":\"%s\",\"targetAvatarId\":\"%s\"}",
                          LEADER_1, MEMBER_1)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Avatar is already a member"));
    }
  }

  // ── POST /api/v1/guilds/{id}/invites/{inviteId}/accept ───────────────────────

  @Nested
  @DisplayName("POST /api/v1/guilds/{id}/invites/{inviteId}/accept")
  class AcceptInvite {

    @Test
    @DisplayName("returns 204 when avatar accepts a valid invite")
    void shouldReturn204AndDelegate() throws Exception {
      doNothing().when(guildService).acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_1, INVITE_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"avatarId\":\"%s\",\"nickname\":\"%s\"}", MEMBER_1, MEMBER_NICK)))
          .andExpect(status().isNoContent());

      verify(guildService).acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_GUILD))
          .when(guildService)
          .acceptInvite(eq(UNKNOWN_GUILD_ID), eq(INVITE_ID), any(Id.class), anyString());

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", UNKNOWN_GUILD, INVITE_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"avatarId\":\"%s\",\"nickname\":\"%s\"}", MEMBER_1, MEMBER_NICK)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when invite is invalid or expired")
    void shouldReturn400WhenInviteInvalid() throws Exception {
      doThrow(new IllegalArgumentException("Invite not found or not yours"))
          .when(guildService)
          .acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_1, INVITE_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"avatarId\":\"%s\",\"nickname\":\"%s\"}", MEMBER_1, MEMBER_NICK)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Invite not found or not yours"));
    }

    @Test
    @DisplayName("returns 400 when guild is full")
    void shouldReturn400WhenGuildFull() throws Exception {
      doThrow(new IllegalStateException("Guild has reached maximum capacity"))
          .when(guildService)
          .acceptInvite(GUILD_ID, INVITE_ID, MEMBER_ID, MEMBER_NICK);

      mockMvc
          .perform(
              post("/api/v1/guilds/{id}/invites/{inviteId}/accept", GUILD_1, INVITE_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format(
                          "{\"avatarId\":\"%s\",\"nickname\":\"%s\"}", MEMBER_1, MEMBER_NICK)))
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
      doNothing().when(guildService).removeMember(GUILD_ID, LEADER_ID, MEMBER_ID);

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", GUILD_1, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(String.format("{\"requestorId\":\"%s\"}", LEADER_1)))
          .andExpect(status().isNoContent());

      verify(guildService).removeMember(GUILD_ID, LEADER_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<GuildMember> notLeaderId = new Id<>("not-a-leader");
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "removeMember"))
          .when(guildService)
          .removeMember(eq(GUILD_ID), eq(notLeaderId), eq(MEMBER_ID));

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", GUILD_1, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"requestorId\":\"not-a-leader\"}"))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_GUILD))
          .when(guildService)
          .removeMember(eq(UNKNOWN_GUILD_ID), any(Id.class), eq(MEMBER_ID));

      mockMvc
          .perform(
              delete("/api/v1/guilds/{id}/members/{memberId}", UNKNOWN_GUILD, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(String.format("{\"requestorId\":\"%s\"}", LEADER_1)))
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
          .perform(post("/api/v1/guilds/{id}/members/{memberId}/leave", GUILD_1, MEMBER_1))
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
          .perform(post("/api/v1/guilds/{id}/members/{memberId}/leave", GUILD_1, MEMBER_1))
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
      doNothing().when(guildService).promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE);

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_1, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format("{\"roleName\":\"OFFICER\",\"requestorId\":\"%s\"}", LEADER_1)))
          .andExpect(status().isNoContent());

      verify(guildService).promoteMember(GUILD_ID, LEADER_ID, MEMBER_ID, OFFICER_ROLE);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<GuildMember> notLeaderId = new Id<>("not-a-leader");
      doThrow(new UnauthorizedGuildOperationException("not-a-leader", "promoteMember"))
          .when(guildService)
          .promoteMember(eq(GUILD_ID), eq(notLeaderId), eq(MEMBER_ID), eq(OFFICER_ROLE));

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_1, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"roleName\":\"OFFICER\",\"requestorId\":\"not-a-leader\"}"))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 400 when roleName is not a valid GuildRole")
    void shouldReturn400OnInvalidRole() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", GUILD_1, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format("{\"roleName\":\"WIZARD\",\"requestorId\":\"%s\"}", LEADER_1)))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(guildService);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new GuildNotFoundException(UNKNOWN_GUILD))
          .when(guildService)
          .promoteMember(eq(UNKNOWN_GUILD_ID), any(Id.class), eq(MEMBER_ID), any(GuildRole.class));

      mockMvc
          .perform(
              patch("/api/v1/guilds/{id}/members/{memberId}/role", UNKNOWN_GUILD, MEMBER_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      String.format("{\"roleName\":\"OFFICER\",\"requestorId\":\"%s\"}", LEADER_1)))
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
          .perform(get("/api/v1/guilds/{id}/rank", GUILD_1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.globalRank").value(3));
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(guildService.getGlobalRank(UNKNOWN_GUILD_ID))
          .thenThrow(new GuildNotFoundException(UNKNOWN_GUILD));

      mockMvc
          .perform(get("/api/v1/guilds/{id}/rank", UNKNOWN_GUILD))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/guilds/leaderboard ───────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/guilds/leaderboard")
  class GetLeaderboard {

    @Test
    @DisplayName("returns 200 with ordered guild list")
    void shouldReturnLeaderboard() throws Exception {
      when(guildService.getGuildLeaderboard()).thenReturn(List.of(guild()));

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
