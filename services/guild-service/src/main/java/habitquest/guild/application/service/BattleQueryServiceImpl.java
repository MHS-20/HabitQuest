package habitquest.guild.application.service;

import common.ddd.Id;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.out.BattleRepository;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BattleQueryServiceImpl implements BattleQueryService {
  private final BattleRepository battleRepository;

  public BattleQueryServiceImpl(BattleRepository battleRepository) {
    this.battleRepository = battleRepository;
  }

  @Override
  public Battle getBattleById(Id<Battle> battleId) throws BattleNotFoundException {
    return battleRepository
        .findById(battleId)
        .orElseThrow(() -> new BattleNotFoundException(battleId.value()));
  }

  @Override
  public Optional<Battle> getBattleByGuild(Id<Guild> guildId) throws BattleNotFoundException {
    return battleRepository.findByGuildId(guildId);
  }

  @Override
  public boolean hasBattleInProgress(Id<Guild> guildId) throws BattleNotFoundException {
    return !getBattleByGuild(guildId)
        .orElseThrow(() -> new BattleNotFoundException("No battle found for guild: " + guildId))
        .getBattleStatus()
        .isOver();
  }

  @Override
  public Id<Guild> getGuildId(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getGuildId();
  }

  @Override
  public BossEnemy getBoss(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBoss();
  }

  @Override
  public List<BossEnemy> getAllBossTypes() {
    return BossType.allBossTypes();
  }

  @Override
  public BossStatus getBossRemainingHealth(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBossRemainingHealth();
  }

  @Override
  public Integer getCurrentTurn(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getCurrentTurn();
  }

  @Override
  public Integer getNumOfTurns(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getNumOfTurns();
  }

  @Override
  public boolean isAttackerTurn(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException {
    Battle battle = getBattleById(battleId);
    return battle.isAttackerTurn(attackerId);
  }

  @Override
  public BattleOutcome getBattleStatus(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus();
  }

  @Override
  public boolean isBattleOver(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isOver();
  }

  @Override
  public boolean isBattleWon(Id<Battle> battleId) throws BattleNotFoundException {
    return getBattleById(battleId).getBattleStatus().isWon();
  }
}
