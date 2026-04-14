package habitquest.notification.infrastructure.consumers.guild;

import java.time.Instant;

public class GuildMessages {

  public record GuildCreatedMessage(
      String guildId, String leaderId, String name, Instant occurredOn) {}

  public record GuildDeletedMessage(String guildId, Instant occurredOn) {}

  public record GuildJoinedMessage(String guildId, String memberId, Instant occurredOn) {}

  public record GuildLeftMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RemovedFromGuildMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RoleAssignedMessage(
      String guildId, String memberId, String roleName, Instant occurredOn) {}

  public record InviteSentMessage(
      String guildId, String targetAvatarId, String inviteId, Instant occurredOn) {}
}
