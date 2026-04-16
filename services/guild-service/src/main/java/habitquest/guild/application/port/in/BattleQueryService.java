package habitquest.guild.application.port.in;

import common.ddd.Id;
import common.hexagonal.InBoundPort;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;
import java.util.Optional;

@InBoundPort
public interface BattleQueryService {

  Battle getBattleById(Id<Battle> battleId) throws BattleNotFoundException;

  Optional<Battle> getBattleByGuild(Id<Guild> guildId) throws BattleNotFoundException;

  boolean hasBattleInProgress(Id<Guild> guildId) throws BattleNotFoundException;

  // --- Boss info (queries) ---
  Id<Guild> getGuildId(Id<Battle> battleId) throws BattleNotFoundException;

  BossEnemy getBoss(Id<Battle> battleId) throws BattleNotFoundException;

  List<BossEnemy> getAllBossTypes();

  BossStatus getBossRemainingHealth(Id<Battle> battleId) throws BattleNotFoundException;

  // --- Turn management (queries) ---
  Integer getCurrentTurn(Id<Battle> battleId) throws BattleNotFoundException;

  Integer getNumOfTurns(Id<Battle> battleId) throws BattleNotFoundException;

  // --- Combat helpers (queries) ---
  boolean isAttackerTurn(Id<Battle> battleId, Id<GuildMember> attackerId)
      throws BattleNotFoundException;

  BattleOutcome getBattleStatus(Id<Battle> battleId) throws BattleNotFoundException;

  boolean isBattleOver(Id<Battle> battleId) throws BattleNotFoundException;

  boolean isBattleWon(Id<Battle> battleId) throws BattleNotFoundException;
}
