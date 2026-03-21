package habitquest.notification.infrastructure.consumers;

import common.hexagonal.Adapter;
import habitquest.notification.infrastructure.notification.NotificationService;
import habitquest.notification.infrastructure.repository.GuildMemberRepository;
import habitquest.notification.infrastructure.repository.UserEmailRepository;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class GuildEventConsumer extends GuildAwareEventConsumer {

  public GuildEventConsumer(
      UserEmailRepository userEmailRepository,
      GuildMemberRepository guildMemberRepository,
      NotificationService notificationService) {
    super(userEmailRepository, guildMemberRepository, notificationService);
  }

  @Bean
  public Consumer<GuildCreatedMessage> guildCreated() {
    return message -> {
      logger()
          .info("Received GuildCreated: guildId={}, name={}", message.guildId(), message.name());
      sendToAvatar(
          message.leaderId(),
          "Guild creata!",
          "La guild \"" + message.name() + "\" è stata creata con successo!");

      guildMemberRepository.addMember(message.guildId(), message.leaderId());
    };
  }

  @Bean
  public Consumer<GuildDeletedMessage> guildDeleted() {
    return message -> {
      logger().info("Received GuildDeleted: guildId={}", message.guildId());
      sendToGuild(
          message.guildId(),
          "Guild eliminata",
          "La guild \"" + message.guildId() + "\" è stata eliminata.");
      guildMemberRepository.removeGuild(message.guildId());
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
      guildMemberRepository.addMember(message.guildId(), message.memberId());
      sendToAvatar(
          message.memberId(),
          "Benvenuto nella guild!",
          "Ti sei unito alla guild \"" + message.guildId() + "\"! Buona avventura insieme.");
    };
  }

  @Bean
  public Consumer<GuildLeftMessage> guildLeft() {
    return message -> {
      logger()
          .info(
              "Received GuildLeft: guildId={}, memberId={}", message.guildId(), message.memberId());
      guildMemberRepository.removeMember(message.guildId(), message.memberId());
      sendToAvatar(
          message.memberId(),
          "Hai lasciato la guild",
          "Hai lasciato la guild \"" + message.guildId() + "\".");
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
      guildMemberRepository.removeMember(message.guildId(), message.memberId());
      sendToAvatar(
          message.memberId(),
          "Sei stato rimosso dalla guild",
          "Sei stato rimosso dalla guild \"" + message.guildId() + "\".");
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
      sendToAvatar(
          message.memberId(),
          "Nuovo ruolo assegnato!",
          "Ti è stato assegnato il ruolo \""
              + message.roleName()
              + "\" nella guild \""
              + message.guildId()
              + "\".");
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
      sendToAvatar(
          message.targetAvatarId(),
          "Invito nella guild!",
          "Hai ricevuto un invito a unirti alla guild \"" + message.guildId() + "\".");
    };
  }

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
