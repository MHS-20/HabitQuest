package habitquest.guild.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.guild.Guild;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface GuildRepository extends Repository {
  Guild save(Guild guild);

  Optional<Guild> findById(Id<Guild> id);

  void deleteById(Id<Guild> id);

  List<Guild> findAllSortedByRank();
}
