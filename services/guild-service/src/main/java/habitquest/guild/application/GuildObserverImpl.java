package habitquest.guild.application;

import habitquest.guild.domain.events.guildEvents.*;
import org.springframework.stereotype.Component;

@Component
public class GuildObserverImpl implements GuildObserver {

  private final GuildNotifier guildNotifier;
  private final GuildLogger log;

  public GuildObserverImpl(GuildNotifier guildNotifier, GuildLogger log) {
    this.guildNotifier = guildNotifier;
    this.log = log;
  }

  @Override
  public void notifyGuildEvent(GuildEvent event) {
    switch (event) {
      case GuildCreated e -> handleGuildCreated(e);
      case GuildDeleted e -> handleGuildDeleted(e);
      case GuildJoined e -> handleGuildJoined(e);
      case GuildLeft e -> handleGuildLeft(e);
      case RemovedFromGuild e -> handleRemovedFromGuild(e);
      case RoleAssigned e -> handleRoleAssigned(e);
      case InviteSent e -> handleInviteSent(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }

  public void handleGuildCreated(GuildCreated e) {
    log.info(e, "Handling GuildCreated event");
    guildNotifier.notifyGuildCreated(e);
  }

  public void handleGuildDeleted(GuildDeleted e) {
    log.info(e, "Handling GuildDeleted event");
    guildNotifier.notifyGuildDeleted(e);
  }

  public void handleGuildJoined(GuildJoined e) {
    log.info(e, "Handling GuildJoined event");
    guildNotifier.notifyGuildJoined(e);
  }

  public void handleGuildLeft(GuildLeft e) {
    log.info(e, "Handling GuildLeft event");
    guildNotifier.notifyGuildLeft(e);
  }

  public void handleRemovedFromGuild(RemovedFromGuild e) {
    log.info(e, "Handling RemovedFromGuild event");
    guildNotifier.notifyRemovedFromGuild(e);
  }

  public void handleRoleAssigned(RoleAssigned e) {
    log.info(e, "Handling RoleAssigned event");
    guildNotifier.notifyRoleAssigned(e);
  }

  public void handleInviteSent(InviteSent e) {
    log.info(e, "Handling InviteSent event");
    guildNotifier.notifyInviteSent(e);
  }
}
