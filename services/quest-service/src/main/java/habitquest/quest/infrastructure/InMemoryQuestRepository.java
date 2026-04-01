package habitquest.quest.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestRepository;
import habitquest.quest.domain.Quest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryQuestRepository implements QuestRepository {

  private final Map<Id<Quest>, Quest> store = new HashMap<>();

  @Override
  public Quest save(Quest quest) {
    store.put(quest.getId(), quest);
    return quest;
  }

  @Override
  public List<Quest> findAll() {
    return List.copyOf(store.values());
  }

  @Override
  public Quest findById(Id<Quest> id) throws QuestNotFoundException {
    Quest quest = store.get(id);
    if (quest == null) {
      throw new QuestNotFoundException(id.value());
    }
    return quest;
  }

  @Override
  public void deleteById(Id<Quest> id) throws QuestNotFoundException {
    if (!store.containsKey(id)) {
      throw new QuestNotFoundException(id.value());
    }
    store.remove(id);
  }
}
