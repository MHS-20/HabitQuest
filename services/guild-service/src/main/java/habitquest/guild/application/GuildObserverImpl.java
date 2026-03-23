package habitquest.guild.application;

import habitquest.guild.domain.events.guildEvents.*;
import org.springframework.stereotype.Component;

@Component
public class GuildObserverImpl implements GuildObserver {

  private final GuildNotifier guildNotifier;

  public GuildObserverImpl(GuildNotifier guildNotifier) {
    this.guildNotifier = guildNotifier;
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
    guildNotifier.notifyGuildCreated(e);
  }

  public void handleGuildDeleted(GuildDeleted e) {
    guildNotifier.notifyGuildDeleted(e);
  }

  public void handleGuildJoined(GuildJoined e) {
    guildNotifier.notifyGuildJoined(e);
  }

  public void handleGuildLeft(GuildLeft e) {
    guildNotifier.notifyGuildLeft(e);
  }

  public void handleRemovedFromGuild(RemovedFromGuild e) {
    guildNotifier.notifyRemovedFromGuild(e);
  }

  public void handleRoleAssigned(RoleAssigned e) {
    guildNotifier.notifyRoleAssigned(e);
  }

  public void handleInviteSent(InviteSent e) {
    guildNotifier.notifyInviteSent(e);
  }
}
