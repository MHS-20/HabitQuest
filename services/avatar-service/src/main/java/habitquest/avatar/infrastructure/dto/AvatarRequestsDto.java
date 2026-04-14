package habitquest.avatar.infrastructure.dto;

public class AvatarRequestsDto {
  public record CreateAvatarRequest(String id, String name) {}

  public record UpdateNameRequest(String name) {}

  public record AmountRequest(Integer amount) {}

  public record PotionRequest(String potionName) {}

  public record GuildInviteRequest(
      String inviteId, String guildId, String guildName, String expiresAt) {}

  public record ItemRequest(String type, String name, String description, Integer power) {}
}
