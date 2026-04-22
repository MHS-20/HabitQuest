package habitquest.avatar.infrastructure.dto;

import common.cqrs.Command;
import common.cqrs.CommandResponse;

public class AvatarCommands {
  // --- Command Requests ---
  public record CreateAvatarCommand(String id, String name) implements Command {}

  public record UpdateNameCommand(String name) implements Command {}

  public record MoneyCommand(int amount)  implements Command {}

  public record SpendManaCommand(int amount) implements Command {}

  public record GrantExperienceCommand(int amount)  implements Command {}

  public record ApplyDamageCommand(int amount) implements Command {}

  public record UsePotionCommand(String name, String description, Integer power)
      implements Command {}

  public record GuildInviteCommand(
      String inviteId, String guildId, String guildName, String expiresAt) implements Command {}

  public record ItemCommand(String type, String name, String description, Integer power)
      implements Command {}

  // --- Command Responses ---
  public record AvatarCreatedResponse(String id) implements CommandResponse {}

  public record DamageResponse(boolean died) implements CommandResponse {}

  public record ErrorResponse(String message) implements CommandResponse {}
}
