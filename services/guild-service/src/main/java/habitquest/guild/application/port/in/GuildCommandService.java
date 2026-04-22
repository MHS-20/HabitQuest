package habitquest.guild.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.Invite;

@InBoundPort
public interface GuildCommandService {
  Id<Guild> createGuild(String name, Id<GuildMember> creatorAvatarId, String creatorNickname);

  void deleteGuild(Id<Guild> guildId) throws GuildNotFoundException;

  Invite sendInvite(Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> targetAvatarId)
      throws GuildNotFoundException;

  void acceptInvite(
      Id<Guild> guildId,
      Id<habitquest.guild.domain.guild.Invite> inviteId,
      Id<GuildMember> avatarId,
      String nickname)
      throws GuildNotFoundException;

  void leaveGuild(Id<Guild> guildId, Id<GuildMember> memberId) throws GuildNotFoundException;

  void removeMember(Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId)
      throws GuildNotFoundException;

  void promoteMember(
      Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId, GuildRole newRole)
      throws GuildNotFoundException;
}
