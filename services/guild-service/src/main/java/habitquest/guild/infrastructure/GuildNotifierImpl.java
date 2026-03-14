package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.GuildNotifier;
import habitquest.guild.domain.events.guildEvents.GuildCreated;
import habitquest.guild.domain.events.guildEvents.GuildDeleted;
import habitquest.guild.domain.events.guildEvents.GuildJoined;
import habitquest.guild.domain.events.guildEvents.GuildLeft;
import habitquest.guild.domain.events.guildEvents.RemovedFromGuild;
import habitquest.guild.domain.events.guildEvents.RoleAssigned;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class GuildNotifierImpl implements GuildNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(GuildNotifierImpl.class);

  static final String GUILD_CREATED_BINDING = "guild-created-out-0";
  static final String GUILD_DELETED_BINDING = "guild-deleted-out-0";
  static final String GUILD_JOINED_BINDING = "guild-joined-out-0";
  static final String GUILD_LEFT_BINDING = "guild-left-out-0";
  static final String REMOVED_FROM_GUILD_BINDING = "guild-removed-out-0";
  static final String ROLE_ASSIGNED_BINDING = "guild-role-assigned-out-0";

  private final StreamBridge streamBridge;

  public GuildNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyGuildCreated(GuildCreated event) {
    GuildCreatedMessage message =
        new GuildCreatedMessage(event.guildId(), event.guildName(), Instant.now());

    LOG.info(
        "Publishing GuildCreated event: guildId={}, name={}", message.guildId(), message.name());
    boolean sent = streamBridge.send(GUILD_CREATED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish GuildCreated event for guildId {}", message.guildId());
    }
  }

  @Override
  public void notifyGuildDeleted(GuildDeleted event) {
    GuildDeletedMessage message = new GuildDeletedMessage(event.guildId(), Instant.now());

    LOG.info("Publishing GuildDeleted event: guildId={}", message.guildId());
    boolean sent = streamBridge.send(GUILD_DELETED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish GuildDeleted event for guildId {}", message.guildId());
    }
  }

  @Override
  public void notifyGuildJoined(GuildJoined event) {
    GuildJoinedMessage message =
        new GuildJoinedMessage(event.guildId(), event.memberId(), Instant.now());

    LOG.info(
        "Publishing GuildJoined event: guildId={}, memberId={}",
        message.guildId(),
        message.memberId());
    boolean sent = streamBridge.send(GUILD_JOINED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish GuildJoined event for guildId {}", message.guildId());
    }
  }

  @Override
  public void notifyGuildLeft(GuildLeft event) {
    GuildLeftMessage message =
        new GuildLeftMessage(event.guildId(), event.memberId(), Instant.now());

    LOG.info(
        "Publishing GuildLeft event: guildId={}, memberId={}",
        message.guildId(),
        message.memberId());
    boolean sent = streamBridge.send(GUILD_LEFT_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish GuildLeft event for guildId {}", message.guildId());
    }
  }

  @Override
  public void notifyRemovedFromGuild(RemovedFromGuild event) {
    RemovedFromGuildMessage message =
        new RemovedFromGuildMessage(event.guildId(), event.memberId(), Instant.now());

    LOG.info(
        "Publishing RemovedFromGuild event: guildId={}, memberId={}",
        message.guildId(),
        message.memberId());
    boolean sent = streamBridge.send(REMOVED_FROM_GUILD_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish RemovedFromGuild event for guildId {}", message.guildId());
    }
  }

  @Override
  public void notifyRoleAssigned(RoleAssigned event) {
    RoleAssignedMessage message =
        new RoleAssignedMessage(
            event.guildId(), event.memberId(), event.newRole().name(), Instant.now());

    LOG.info(
        "Publishing RoleAssigned event: guildId={}, memberId={}, role={}",
        message.guildId(),
        message.memberId(),
        message.roleName);
    boolean sent = streamBridge.send(ROLE_ASSIGNED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish RoleAssigned event for guildId {}", message.guildId());
    }
  }

  public record GuildCreatedMessage(String guildId, String name, Instant occurredOn) {}

  public record GuildDeletedMessage(String guildId, Instant occurredOn) {}

  public record GuildJoinedMessage(String guildId, String memberId, Instant occurredOn) {}

  public record GuildLeftMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RemovedFromGuildMessage(String guildId, String memberId, Instant occurredOn) {}

  public record RoleAssignedMessage(
      String guildId, String memberId, String roleName, Instant occurredOn) {}
}
