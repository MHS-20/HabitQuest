package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.EventConsumer;
import habitquest.notification.infrastructure.NotificationService;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class GuildEventConsumer implements EventConsumer {

  private final NotificationService notificationService;

  public GuildEventConsumer(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Bean
  public Consumer<GuildCreatedMessage> guildCreated() {
    return message -> {
      logger()
          .info("Received GuildCreated: guildId={}, name={}", message.guildId(), message.name());
      notificationService.send("La guild \"" + message.name() + "\" è stata creata!");
    };
  }

  @Bean
  public Consumer<GuildDeletedMessage> guildDeleted() {
    return message -> {
      logger().info("Received GuildDeleted: guildId={}", message.guildId());
      notificationService.send("La guild " + message.guildId() + " è stata eliminata.");
    };
  }

  @Bean
  public Consumer<GuildJoinedMessage> guildJoined() {
    return message -> {
      logger()
          .info(
              "Received GuildJoined: guildId={}, memberId={}",
              message.guildId(),
              message.memberId());
      notificationService.send(
          "Il membro " + message.memberId() + " si è unito alla guild " + message.guildId() + "!");
    };
  }

  @Bean
  public Consumer<GuildLeftMessage> guildLeft() {
    return message -> {
      logger()
          .info(
              "Received GuildLeft: guildId={}, memberId={}", message.guildId(), message.memberId());
      notificationService.send(
          "Il membro " + message.memberId() + " ha lasciato la guild " + message.guildId() + ".");
    };
  }

  @Bean
  public Consumer<RemovedFromGuildMessage> guildRemoved() {
    return message -> {
      logger()
          .info(
              "Received RemovedFromGuild: guildId={}, memberId={}",
              message.guildId(),
              message.memberId());
      notificationService.send(
          "Il membro "
              + message.memberId()
              + " è stato rimosso dalla guild "
              + message.guildId()
              + ".");
    };
  }

  @Bean
  public Consumer<RoleAssignedMessage> guildRoleAssigned() {
    return message -> {
      logger()
          .info(
              "Received RoleAssigned: guildId={}, memberId={}, role={}",
              message.guildId(),
              message.memberId(),
              message.roleName());
      notificationService.send(
          "Al membro "
              + message.memberId()
              + " è stato assegnato il ruolo "
              + message.roleName()
              + " nella guild "
              + message.guildId()
              + ".");
    };
  }

  @Bean
  public Consumer<InviteSentMessage> guildInviteSent() {
    return message -> {
      logger()
          .info(
              "Received InviteSent: guildId={}, targetAvatarId={}",
              message.guildId(),
              message.targetAvatarId());
      notificationService.send("Hai ricevuto un invito dalla guild " + message.guildId() + "!");
    };
  }

  public record GuildCreatedMessage(String guildId, String name, Instant occurredOn) {}

  public record GuildDeletedMessage(String guildId, Instant occurredOn) {}

  public record GuildJoinedMessage(String guildId, String memberId, Instant occurredOn) {}

  public record GuildLeftMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RemovedFromGuildMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RoleAssignedMessage(
      String guildId, String memberId, String roleName, Instant occurredOn) {}

  public record InviteSentMessage(
      String guildId, String targetAvatarId, String inviteId, Instant occurredOn) {}
}
