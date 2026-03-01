package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import java.util.List;
import java.util.Optional;

@InBoundPort
public interface GuildService {
  String createGuild(String name, String creatorAvatarId, String creatorNickname);

  Optional<Guild> getGuild(String guildId) throws GuildNotFoundException;

  void updateGuild(String guildId, Guild request) throws GuildNotFoundException;

  void deleteGuild(String guildId) throws GuildNotFoundException;

  List<GuildMember> getMembers(String guildId);

  void leaveGuild(String guildId, String memberId);

  void addMember(String guildId, GuildMember member);

  void removeMember(String guildId, String memberId);

  void promoteMember(String guildId, String memberId, GuildRole newRole);

  Integer getGlobalRank(String guildId);

  List<Guild> getGuildLeaderboard();
}
