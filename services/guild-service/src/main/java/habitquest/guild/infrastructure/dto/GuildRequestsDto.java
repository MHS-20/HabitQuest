package habitquest.guild.infrastructure.dto;

public class GuildRequestsDto {
  public record CreateGuildRequest(String name, String creatorAvatarId, String creatorNickname) {}

  public record RemoveMemberRequest(String requestorId) {}

  public record SendInviteRequest(String requestorId, String targetAvatarId) {}

  public record AcceptInviteRequest(String avatarId, String nickname) {}

  public record PromoteMemberRequest(String roleName, String requestorId) {}
}
