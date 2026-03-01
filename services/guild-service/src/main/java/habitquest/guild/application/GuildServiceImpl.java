package habitquest.guild.application;

import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import java.util.List;
import java.util.Optional;

public class GuildServiceImpl implements GuildService {
  private final GuildFactory guildFactory;
  private final GuildRepository guildRepository;
  private final GuildObserver guildObserver;

  public GuildServiceImpl(
      GuildFactory guildFactory, GuildRepository guildRepository, GuildObserver guildObserver) {
    this.guildFactory = guildFactory;
    this.guildRepository = guildRepository;
    this.guildObserver = guildObserver;
  }

  @Override
  public String createGuild(String name, String creatorAvatarId, String creatorNickname) {
    var guild = guildFactory.create(name, creatorAvatarId, creatorNickname);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildCreated(guild.getId()));
    return guild.getId();
  }

  @Override
  public Optional<Guild> getGuild(String guildId) throws GuildNotFoundException {
    Optional<Guild> guild = guildRepository.findById(guildId);
    if (guild.isEmpty()) {
      throw new GuildNotFoundException(guildId);
    }
    return guild;
  }

  @Override
  public void updateGuild(String guildId, Guild request) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.updateGlobalRank(request.getGlobalRank());
    guildRepository.save(guild);
  }

  @Override
  public void deleteGuild(String guildId) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guildRepository.deleteById(guildId);
    guildObserver.notifyGuildEvent(new GuildDeleted(guild.getId()));
  }

  // --- Membership management ---
  @Override
  public List<GuildMember> getMembers(String guildId) {
    return guildRepository
        .findById(guildId)
        .map(Guild::getMembers)
        .orElseThrow(() -> new GuildNotFoundException(guildId));
  }

  @Override
  public void addMember(String guildId, GuildMember member) {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.addMember(member);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildJoined(guild.getId(), member.getId()));
  }

  @Override
  public void leaveGuild(String guildId, String memberId) {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.removeMember(memberId);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildLeft(guild.getId(), memberId));
  }

  @Override
  public void removeMember(String guildId, String memberId) {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.removeMember(memberId);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RemovedFromGuild(guild.getId(), memberId));
  }

  @Override
  public void promoteMember(String guildId, String memberId, GuildRole newRole) {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.promoteMember(memberId, newRole);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RoleAssigned(guild.getId(), memberId, newRole));
  }

  // --- Ranking ---
  @Override
  public Integer getGlobalRank(String guildId) {
    return guildRepository
        .findById(guildId)
        .map(Guild::getGlobalRank)
        .orElseThrow(() -> new GuildNotFoundException(guildId));
  }

  @Override
  public List<Guild> getGuildLeaderboard() {
    return guildRepository.findAllSortedByRank();
  }
}
