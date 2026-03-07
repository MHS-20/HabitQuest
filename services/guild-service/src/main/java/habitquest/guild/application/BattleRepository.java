package habitquest.guild.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface BattleRepository extends Repository {
  Battle save(Battle battle);

  Optional<Battle> findById(String id);

  void deleteById(String id);

  Optional<Battle> findByGuildId(String guildId);
  List<Battle> findByStatus(BattleStatus status);
}
