package habitquest.guild.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.guild.application.GuildRepository;
import habitquest.guild.domain.guild.Guild;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryGuildRepository implements GuildRepository {
  private final Map<Id<Guild>, Guild> store = new ConcurrentHashMap<>();

  @Override
  public Guild save(Guild guild) {
    store.put(guild.getId(), guild);
    return guild;
  }

  @Override
  public Optional<Guild> findById(Id<Guild> id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Id<Guild> id) {
    store.remove(id);
  }

  @Override
  public List<Guild> findAllSortedByRank() {
    return store.values().stream().sorted(Comparator.comparingInt(Guild::getGlobalRank)).toList();
  }
}
