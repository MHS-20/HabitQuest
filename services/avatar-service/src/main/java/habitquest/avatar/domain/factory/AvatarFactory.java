package habitquest.avatar.domain.factory;

import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.spells.Blizzard;
import habitquest.avatar.domain.spells.FireBall;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.spells.Thunder;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
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

  public Optional<Spell> createSpellForLevel(Integer level) {
    switch (level) {
      case 5:
        return Optional.of(
            new FireBall(
                "Fireball", "A basic fire spell that deals moderate damage.", 10, new Mana(5)));
      case 10:
        return Optional.of(
            new Blizzard("Blizzard", "A chilling spell that deals damage.", 15, new Mana(7)));
      case 15:
        return Optional.of(new Thunder("Thunder", "A powerful lightning.", 20, new Mana(10)));
      default:
        return Optional.empty();
    }
  }
}
