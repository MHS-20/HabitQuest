package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.GuildLogger;
import habitquest.guild.application.GuildNotifier;
import habitquest.guild.domain.events.guildEvents.*;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class GuildNotifierImpl implements GuildNotifier {

  static final String GUILD_CREATED_BINDING = "guild.created";
  static final String GUILD_DELETED_BINDING = "guild.deleted";
  static final String GUILD_JOINED_BINDING = "guild.joined";
  static final String GUILD_LEFT_BINDING = "guild.left";
  static final String REMOVED_FROM_GUILD_BINDING = "guild.removed";
  static final String ROLE_ASSIGNED_BINDING = "guild.role-assigned";
  static final String INVITE_SENT_BINDING = "guild.invite-sent";

  private final StreamBridge streamBridge;
  private final GuildLogger log;

  public GuildNotifierImpl(StreamBridge streamBridge, GuildLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyGuildCreated(GuildCreated event) {
    GuildCreatedMessage message =
        new GuildCreatedMessage(
            event.guildId().value(), event.leaderId().value(), event.guildName(), Instant.now());
    log.info(message, "Publishing GuildCreated event");
    boolean sent = streamBridge.send(GUILD_CREATED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish GuildCreated event", null);
    }
  }

  @Override
  public void notifyGuildDeleted(GuildDeleted event) {
    GuildDeletedMessage message = new GuildDeletedMessage(event.guildId().value(), Instant.now());
    log.info(message, "Publishing GuildDeleted event");
    boolean sent = streamBridge.send(GUILD_DELETED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish GuildDeleted event", null);
    }
  }

  @Override
  public void notifyInviteSent(InviteSent event) {
    InviteSentMessage message =
        new InviteSentMessage(
            event.guildId().value(),
            event.targetAvatarId().value(),
            event.inviteId().value(),
            Instant.now());
    log.info(message, "Publishing InviteSent event");
    boolean sent = streamBridge.send(INVITE_SENT_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish InviteSent event", null);
    }
  }

  @Override
  public void notifyGuildJoined(GuildJoined event) {
    GuildJoinedMessage message =
        new GuildJoinedMessage(event.guildId().value(), event.memberId().value(), Instant.now());
    log.info(message, "Publishing GuildJoined event");
    boolean sent = streamBridge.send(GUILD_JOINED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish GuildJoined event", null);
    }
  }

  @Override
  public void notifyGuildLeft(GuildLeft event) {
    GuildLeftMessage message =
        new GuildLeftMessage(event.guildId().value(), event.memberId().value(), Instant.now());
    log.info(message, "Publishing GuildLeft event");
    boolean sent = streamBridge.send(GUILD_LEFT_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish GuildLeft event", null);
    }
  }

  @Override
  public void notifyRemovedFromGuild(RemovedFromGuild event) {
    RemovedFromGuildMessage message =
        new RemovedFromGuildMessage(
            event.guildId().value(), event.memberId().value(), Instant.now());
    log.info(message, "Publishing RemovedFromGuild event");
    boolean sent = streamBridge.send(REMOVED_FROM_GUILD_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish RemovedFromGuild event", null);
    }
  }

  @Override
  public void notifyRoleAssigned(RoleAssigned event) {
    RoleAssignedMessage message =
        new RoleAssignedMessage(
            event.guildId().value(),
            event.memberId().value(),
            event.newRole().name(),
            Instant.now());
    log.info(message, "Publishing RoleAssigned event");
    boolean sent = streamBridge.send(ROLE_ASSIGNED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish RoleAssigned event", null);
    }
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
