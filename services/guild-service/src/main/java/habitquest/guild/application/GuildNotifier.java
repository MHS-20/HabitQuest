package habitquest.guild.application;

import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.events.guildEvents.*;

@OutBoundPort
public interface GuildNotifier {

  void notifyGuildCreated(GuildCreated e);

  void notifyGuildDeleted(GuildDeleted e);

  void notifyInviteSent(InviteSent e);

  void notifyGuildJoined(GuildJoined e);

  void notifyGuildLeft(GuildLeft e);

  void notifyRemovedFromGuild(RemovedFromGuild e);

  void notifyRoleAssigned(RoleAssigned e);
}
