package habitquest.guild.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.guild.domain.guild.Guild;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface GuildRepository extends Repository {
  Guild save(Guild guild);

  Optional<Guild> findById(String id);

  void deleteById(String id);

  List<Guild> findAllSortedByRank();
}
