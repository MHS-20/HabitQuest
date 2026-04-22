package habitquest.quest.infrastructure.outbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.port.out.ActiveQuestsRepository;
import habitquest.quest.domain.ActiveQuests;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Quest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryActiveQuestsRepository implements ActiveQuestsRepository {

  private final Map<Id<ActiveQuests>, ActiveQuests> store = new ConcurrentHashMap<>();

  @Override
  public ActiveQuests save(ActiveQuests activeQuests) {
    store.put(activeQuests.getId(), activeQuests);
    return activeQuests;
  }

  @Override
  public Optional<ActiveQuests> findByQuestIdAndAvatarId(Id<Quest> questId, Id<Avatar> avatarId) {
    return store.values().stream()
        .filter(
            active -> active.getQuestId().equals(questId) && active.getAvatarId().equals(avatarId))
        .findFirst();
  }

  @Override
  public List<ActiveQuests> findByAvatarId(Id<Avatar> avatarId) {
    return store.values().stream().filter(active -> active.getAvatarId().equals(avatarId)).toList();
  }
}
