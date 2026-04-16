package habitquest.guild.infrastructure.dto;

import common.cqrs.Command;
import common.cqrs.CommandResponse;

public class GuildCommands {
  // Requests (commands)
  public record CreateGuildCommand(String name, String creatorAvatarId, String creatorNickname)
      implements Command {}

  public record RemoveMemberCommand(String requestorId) implements Command {}

  public record SendInviteCommand(String requestorId, String targetAvatarId) implements Command {}

  public record AcceptInviteCommand(String avatarId, String nickname) implements Command {}

  public record PromoteMemberCommand(String roleName, String requestorId) implements Command {}

  // Responses produced by command endpoints
  public record GuildCreatedResponse(String id) implements CommandResponse {}

  public record ErrorResponse(String message) implements CommandResponse {}
}
