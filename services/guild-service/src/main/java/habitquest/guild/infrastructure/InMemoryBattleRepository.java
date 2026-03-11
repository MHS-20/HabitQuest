package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.BattleRepository;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
@Adapter
public class InMemoryBattleRepository implements BattleRepository {

  private final Map<String, Battle> store = new HashMap<>();

  @Override
  public Battle save(Battle battle) {
    store.put(battle.getId(), battle);
    return battle;
  }

  @Override
  public Optional<Battle> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(String id) {
    store.remove(id);
  }

  @Override
  public Optional<Battle> findByGuildId(String guildId) {
    return store.values().stream().filter(b -> b.getGuildId().equals(guildId)).findFirst();
  }

  @Override
  public List<Battle> findByStatus(BattleStatus status) {
    return store.values().stream().filter(b -> b.getBattleStatus() == status).toList();
  }
}
