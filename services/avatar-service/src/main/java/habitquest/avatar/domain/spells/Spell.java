package habitquest.avatar.domain.spells;

import habitquest.avatar.domain.avatar.Level;
import habitquest.avatar.domain.avatar.Mana;
import java.util.Arrays;
import java.util.Optional;

public enum Spell {
  FIREBALL("Fireball", "A basic fire spell.", 10, 5, 5),
  BLIZZARD("Blizzard", "A chilling spell.", 15, 7, 10),
  THUNDER("Thunder", "A powerful lightning.", 20, 10, 15);

  private final String name;
  private final String description;
  private final int power;
  private final int requiredMana;
  private final int requiredLevel;

  Spell(String name, String description, int power, int requiredMana, int requiredLevel) {
    this.name = name;
    this.description = description;
    this.power = power;
    this.requiredMana = requiredMana;
    this.requiredLevel = requiredLevel;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getPower() {
    return power;
  }

  public Mana getRequiredMana() {
    return new Mana(requiredMana);
  }

  public int getRequiredLevel() {
    return requiredLevel;
  }

  public static Optional<Spell> unlockedAtLevel(Level level) {
    return Arrays.stream(values()).filter(s -> s.requiredLevel == level.levelNumber()).findFirst();
  }
}
