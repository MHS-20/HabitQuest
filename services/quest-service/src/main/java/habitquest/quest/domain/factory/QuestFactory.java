package habitquest.quest.domain.factory;

import common.ddd.Factory;
import habitquest.quest.domain.Quest;

public class QuestFactory implements Factory {

  public static Quest createQuest(String name) {
    return new Quest(new UUIDGenerator().nextId(), name);
  }
}
