package habitquest.quest.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.quest.domain.Quest;
import java.time.Duration;

public class QuestFactory implements Factory {

  private final IdGenerator idGenerator;

  public QuestFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Quest createQuest(String name, Duration duration) {
    return new Quest(new Id<>(idGenerator.nextId()), name, duration);
  }
}
