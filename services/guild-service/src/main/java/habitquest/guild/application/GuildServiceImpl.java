package habitquest.guild.application;

import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.factory.InviteFactory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.Invite;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GuildServiceImpl implements GuildService {
  private final GuildFactory guildFactory;
  private final GuildRepository guildRepository;
  private final GuildObserver guildObserver;
  private final BattleService battleService;
  private final InviteFactory inviteFactory;

  public GuildServiceImpl(
      GuildFactory guildFactory,
      GuildRepository guildRepository,
      GuildObserver guildObserver,
      BattleService battleService,
      InviteFactory inviteFactory) {
    this.guildFactory = guildFactory;
    this.guildRepository = guildRepository;
    this.guildObserver = guildObserver;
    this.battleService = battleService;
    this.inviteFactory = inviteFactory;
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
  public void sendInvite(String guildId, String requestorId, String targetAvatarId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    Invite invite = inviteFactory.create(guildId, targetAvatarId);
    guild.sendInvite(requestorId, invite);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new InviteSent(guildId, targetAvatarId, invite.inviteId()));
  }

  @Override
  public void acceptInvite(String guildId, String inviteId, String avatarId, String nickname)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.acceptInvite(inviteId, avatarId, nickname);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildJoined(guildId, avatarId)); // already exists!
    battleService
        .getBattleByGuild(guildId)
        .ifPresent(b -> battleService.increaseNumOfTurn(b.getId(), avatarId));
  }

  @Override
  public void leaveGuild(String guildId, String memberId) throws GuildNotFoundException {
    Guild guild =
        guildRepository.findById(guildId).orElseThrow(() -> new GuildNotFoundException(guildId));
    guild.leaveGuild(memberId);
    guildObserver.notifyGuildEvent(new GuildLeft(guild.getId(), memberId));

    battleService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleService.decreaseNumOfTurn(battle.getId(), memberId));

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
        .ifPresent(battle -> battleService.decreaseNumOfTurn(battle.getId(), memberId));
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
