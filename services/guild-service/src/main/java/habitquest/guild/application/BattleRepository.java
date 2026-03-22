package habitquest.guild.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.guild.Guild;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface BattleRepository extends Repository {
  Battle save(Battle battle);

  Optional<Battle> findById(Id<Battle> id);

  void deleteById(Id<Battle> id);

  Optional<Battle> findByGuildId(Id<Guild> guildId);

  List<Battle> findByStatus(BattleOutcome status);
}
