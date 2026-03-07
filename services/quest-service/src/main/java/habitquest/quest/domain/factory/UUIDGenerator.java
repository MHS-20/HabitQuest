package habitquest.quest.domain.factory;

import java.util.UUID;

public class UUIDGenerator implements IdGenerator {
  @Override
  public String nextId() {
    return UUID.randomUUID().toString();
  }
}
