package habitquest.avatar.domain.factory;

import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.spells.Blizzard;
import habitquest.avatar.domain.spells.FireBall;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.spells.Thunder;

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

  public Spell createSpellForLevel(Integer level) {
    switch (level) {
      case 5:
        return new FireBall(
            "Fireball", "A basic fire spell that deals moderate damage.", 10, new Mana(5));
      case 10:
        return new Blizzard("Blizzard", "A chilling spell that deals damage.", 15, new Mana(7));
      case 15:
        return new Thunder("Thunder", "A powerful lightning.", 20, new Mana(10));
      default:
        return null;
    }
  }
}
