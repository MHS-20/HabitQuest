package habitquest.guild.application;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.Invite;
import java.util.List;

@InBoundPort
public interface GuildService {
  // Primary API using typed Ids
  Id<Guild> createGuild(String name, Id<GuildMember> creatorAvatarId, String creatorNickname);

  Guild getGuild(Id<Guild> guildId) throws GuildNotFoundException;

  void deleteGuild(Id<Guild> guildId) throws GuildNotFoundException;

  List<GuildMember> getMembers(Id<Guild> guildId) throws GuildNotFoundException;

  boolean isLeader(Id<Guild> guildId, Id<GuildMember> memberId) throws GuildNotFoundException;

  Invite sendInvite(Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> targetAvatarId)
      throws GuildNotFoundException;

  void acceptInvite(
      Id<Guild> guildId, Id<Invite> inviteId, Id<GuildMember> avatarId, String nickname)
      throws GuildNotFoundException;

  void leaveGuild(Id<Guild> guildId, Id<GuildMember> memberId) throws GuildNotFoundException;

  void removeMember(Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId)
      throws GuildNotFoundException;

  void promoteMember(
      Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId, GuildRole newRole)
      throws GuildNotFoundException;

  Integer getGlobalRank(Id<Guild> guildId) throws GuildNotFoundException;

  List<Guild> getGuildLeaderboard();
}
