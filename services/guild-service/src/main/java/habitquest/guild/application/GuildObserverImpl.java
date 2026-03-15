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
      case GuildCreated e -> guildNotifier.notifyGuildCreated(e);
      case GuildDeleted e -> guildNotifier.notifyGuildDeleted(e);
      case GuildJoined e -> guildNotifier.notifyGuildJoined(e);
      case GuildLeft e -> guildNotifier.notifyGuildLeft(e);
      case RemovedFromGuild e -> guildNotifier.notifyRemovedFromGuild(e);
      case RoleAssigned e -> guildNotifier.notifyRoleAssigned(e);
      case InviteSent e -> guildNotifier.notifyInviteSent(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }
}
