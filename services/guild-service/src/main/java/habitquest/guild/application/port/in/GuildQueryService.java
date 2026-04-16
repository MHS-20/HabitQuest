package habitquest.guild.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;

@InBoundPort
public interface GuildQueryService {
  Guild getGuild(Id<Guild> guildId) throws GuildNotFoundException;

  List<GuildMember> getMembers(Id<Guild> guildId) throws GuildNotFoundException;

  boolean isLeader(Id<Guild> guildId, Id<GuildMember> memberId) throws GuildNotFoundException;

  Integer getGlobalRank(Id<Guild> guildId) throws GuildNotFoundException;

  List<Guild> getGuildLeaderboard();
}
