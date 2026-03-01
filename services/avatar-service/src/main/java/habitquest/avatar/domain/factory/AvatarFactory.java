package habitquest.avatar.domain.factory;

import habitquest.avatar.domain.avatar.*;

public class AvatarFactory {
  private final IdGenerator idGenerator;

  public AvatarFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Avatar create(String name) {
    return new Avatar(
        name,
        idGenerator.nextId(),
        idGenerator.nextId(),
        idGenerator.nextId(),
        idGenerator.nextId());
  }
}
