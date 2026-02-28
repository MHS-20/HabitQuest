package habitquest.guild.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.battle.Battle;

@OutBoundPort
public interface BattleRepository extends Repository {
    Battle save(Battle battle);
    Battle findById(String id) throws BattleNotFoundException;
    void deleteById(String id) throws BattleNotFoundException;
}
