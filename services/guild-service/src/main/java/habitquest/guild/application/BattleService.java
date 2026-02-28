package habitquest.guild.application;

import common.hexagonal.InBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;

@InBoundPort
public interface BattleService {

    Battle getBattle(String battleId);
    String getGuildId(String battleId);

    BossEnemy getBoss(String battleId);

    Integer getNumOfTurns(String battleId);
    void updateNumOfTurns(String battleId, Integer numOfTurns);

    Integer getCurrentTurn(String battleId);
    void updateCurrentTurn(String battleId, Integer currentTurn);

    BossStatus getBossRemainingHealth(String battleId);
    void updateBossRemainingHealth(String battleId, BossStatus bossStatus);
}
