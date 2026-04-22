package habitquest.guild.application.service;

import common.ddd.Id;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.GuildQueryService;
import habitquest.guild.application.port.out.GuildRepository;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GuildQueryServiceImpl implements GuildQueryService {
  private final GuildRepository guildRepository;

  public GuildQueryServiceImpl(GuildRepository guildRepository) {
    this.guildRepository = guildRepository;
  }

  @Override
  public Guild getGuild(Id<Guild> guildId) throws GuildNotFoundException {
    return guildRepository
        .findById(guildId)
        .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
  }

  @Override
  public List<GuildMember> getMembers(Id<Guild> guildId) throws GuildNotFoundException {
    return guildRepository
        .findById(guildId)
        .map(Guild::getMembers)
        .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
  }

  @Override
  public boolean isLeader(Id<Guild> guildId, Id<GuildMember> memberId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    return guild.isLeader(memberId);
  }

  @Override
  public Integer getGlobalRank(Id<Guild> guildId) throws GuildNotFoundException {
    return guildRepository
        .findById(guildId)
        .map(Guild::getGlobalRank)
        .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
  }

  @Override
  public List<Guild> getGuildLeaderboard() {
    return guildRepository.findAllSortedByRank();
  }
}
