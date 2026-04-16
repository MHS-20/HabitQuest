package habitquest.guild.application.service;

import common.ddd.Id;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.BattleCommandService;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.in.GuildCommandService;
import habitquest.guild.application.port.out.AvatarClientPort;
import habitquest.guild.application.port.out.GuildRepository;
import habitquest.guild.domain.events.guildEvents.*;
import habitquest.guild.domain.factory.GuildFactory;
import habitquest.guild.domain.factory.InviteFactory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import habitquest.guild.domain.guild.Invite;
import org.springframework.stereotype.Service;

@Service
public class GuildCommandServiceImpl implements GuildCommandService {
  private final GuildFactory guildFactory;
  private final GuildRepository guildRepository;
  private final GuildObserver guildObserver;
  private final BattleCommandService battleCommandService;
  private final BattleQueryService battleQueryService;
  private final InviteFactory inviteFactory;
  private final AvatarClientPort avatarPort;

  public GuildCommandServiceImpl(
      GuildFactory guildFactory,
      GuildRepository guildRepository,
      GuildObserver guildObserver,
      BattleCommandService battleCommandService,
      BattleQueryService battleQueryService,
      InviteFactory inviteFactory,
      AvatarClientPort avatarClientPort) {
    this.guildFactory = guildFactory;
    this.guildRepository = guildRepository;
    this.guildObserver = guildObserver;
    this.battleCommandService = battleCommandService;
    this.battleQueryService = battleQueryService;
    this.inviteFactory = inviteFactory;
    this.avatarPort = avatarClientPort;
  }

  @Override
  public Id<Guild> createGuild(
      String name, Id<GuildMember> creatorAvatarId, String creatorNickname) {
    var guild = guildFactory.create(name, creatorAvatarId, creatorNickname);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildCreated(guild.getId(), creatorAvatarId, name));
    return guild.getId();
  }

  @Override
  public void deleteGuild(Id<Guild> guildId) throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    guildRepository.deleteById(guildId);
    guildObserver.notifyGuildEvent(new GuildDeleted(guild.getId()));
  }

  @Override
  public Invite sendInvite(
      Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> targetAvatarId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    Invite invite = inviteFactory.create(guildId, targetAvatarId);
    guild.sendInvite(requestorId, invite);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new InviteSent(guildId, targetAvatarId, invite.inviteId()));
    avatarPort.sendInviteToAvatar(
        invite.inviteId().value(),
        targetAvatarId.value(),
        guildId.value(),
        guild.getName(),
        invite.expiresAt());
    return invite;
  }

  @Override
  public void acceptInvite(
      Id<Guild> guildId,
      Id<habitquest.guild.domain.guild.Invite> inviteId,
      Id<GuildMember> avatarId,
      String nickname)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    guild.acceptInvite(inviteId, avatarId, nickname);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new GuildJoined(guildId, avatarId));
    battleQueryService
        .getBattleByGuild(guildId)
        .ifPresent(b -> battleCommandService.increaseNumOfTurn(b.getId(), avatarId));
  }

  @Override
  public void leaveGuild(Id<Guild> guildId, Id<GuildMember> memberId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    guild.leaveGuild(memberId);
    guildObserver.notifyGuildEvent(new GuildLeft(guild.getId(), memberId));

    battleQueryService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleCommandService.decreaseNumOfTurn(battle.getId(), memberId));

    if (guild.getMembers().isEmpty()) {
      battleQueryService
          .getBattleByGuild(guildId)
          .ifPresent(battle -> battleCommandService.deleteBattle(battle.getId()));

      guildRepository.deleteById(guildId);
      guildObserver.notifyGuildEvent(new GuildDeleted(guild.getId()));
    } else {
      guildRepository.save(guild);
    }
  }

  @Override
  public void removeMember(Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    guild.removeMember(requestorId, memberId);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RemovedFromGuild(guild.getId(), memberId));
    battleQueryService
        .getBattleByGuild(guildId)
        .ifPresent(battle -> battleCommandService.decreaseNumOfTurn(battle.getId(), memberId));
  }

  @Override
  public void promoteMember(
      Id<Guild> guildId, Id<GuildMember> requestorId, Id<GuildMember> memberId, GuildRole newRole)
      throws GuildNotFoundException {
    Guild guild =
        guildRepository
            .findById(guildId)
            .orElseThrow(() -> new GuildNotFoundException(guildId.value()));
    guild.promoteMember(requestorId, memberId, newRole);
    guildRepository.save(guild);
    guildObserver.notifyGuildEvent(new RoleAssigned(guild.getId(), memberId, newRole));
  }
}
