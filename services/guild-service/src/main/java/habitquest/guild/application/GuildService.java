package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import java.util.List;

@InBoundPort
public interface GuildService {
  String createGuild(String name, String creatorAvatarId, String creatorNickname);

  Guild getGuild(String guildId) throws GuildNotFoundException;

  void deleteGuild(String guildId) throws GuildNotFoundException;

  List<GuildMember> getMembers(String guildId) throws GuildNotFoundException;

  boolean isLeader(String guildId, String memberId) throws GuildNotFoundException;

  void leaveGuild(String guildId, String memberId) throws GuildNotFoundException;

  void addMember(String guildId, GuildMember member) throws GuildNotFoundException;

  void removeMember(String guildId, String memberId) throws GuildNotFoundException;

  void promoteMember(String guildId, String memberId, GuildRole newRole)
      throws GuildNotFoundException;

  Integer getGlobalRank(String guildId) throws GuildNotFoundException;

  List<Guild> getGuildLeaderboard();
}
