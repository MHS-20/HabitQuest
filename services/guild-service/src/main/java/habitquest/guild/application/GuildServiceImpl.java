package habitquest.guild.application;

import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GuildServiceImpl implements GuildService {
  private final GuildFactory guildFactory;
  private final GuildRepository guildRepository;
  private final GuildObserver guildObserver;
  private final BattleService battleService;

  public GuildServiceImpl(
      GuildFactory guildFactory,
      GuildRepository guildRepository,
      GuildObserver guildObserver,
      BattleService battleService) {
    this.guildFactory = guildFactory;
    this.guildRepository = guildRepository;
    this.guildObserver = guildObserver;
    this.battleService = battleService;
  }

  @Override
  public String createGuild(String name, String creatorAvatarId, String creatorNickname) {
    var guild = guildFactory.create(name, creatorAvatarId, creatorNickname);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildCreated(guild.getId(), name));
    return guild.getId();
  }

  @Override
  public Guild getGuild(String guildId) throws GuildNotFoundException {
    return guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
  }

  @Override
  public void deleteGuild(String guildId) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guildRepository.deleteById(guildId);
    guildObserver.notifyGuildEvent(new GuildDeleted(guild.getId()));
  }

  @Override
  public boolean isLeader(String guildId, String memberId) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    return guild.isLeader(memberId);
  }

  // --- Membership management ---
  @Override
  public List<GuildMember> getMembers(String guildId) throws GuildNotFoundException {
    return guildRepository
        .findById(guildId)
        .map(Guild::getMembers)
        .orElseThrow(() -> new GuildNotFoundException(guildId));
  }

  @Override
  public void addMember(String guildId, GuildMember member) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.addMember(member);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildJoined(guild.getId(), member.getId()));
    battleService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleService.increaseNumOfTurn(battle.getId()));
  }

  @Override
  public void leaveGuild(String guildId, String memberId) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.leaveGuild(memberId);
    guildObserver.notifyGuildEvent(new GuildLeft(guild.getId(), memberId));

    battleService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleService.decreaseNumOfTurn(battle.getId()));

    if (guild.getMembers().isEmpty()) {
      battleService
          .getBattleByGuild(guildId)
          .ifPresent(battle -> battleService.deleteBattle(battle.getId()));

      guildRepository.deleteById(guildId);
      guildObserver.notifyGuildEvent(new GuildDeleted(guild.getId()));
    } else {
      guildRepository.save(guild);
    }
  }

  @Override
  public void removeMember(String guildId, String requestorId, String memberId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.removeMember(requestorId, memberId);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RemovedFromGuild(guild.getId(), memberId));
    battleService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleService.decreaseNumOfTurn(battle.getId()));
  }

  @Override
  public void promoteMember(String guildId, String requestorId, String memberId, GuildRole newRole)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.promoteMember(requestorId, memberId, newRole);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RoleAssigned(guild.getId(), memberId, newRole));
  }

  // --- Ranking ---
  @Override
  public Integer getGlobalRank(String guildId) throws GuildNotFoundException {
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
