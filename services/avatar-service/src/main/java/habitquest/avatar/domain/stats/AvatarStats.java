package habitquest.avatar.domain.stats;

import common.ddd.Aggregate;

public class AvatarStats implements Aggregate<String> {

  private String id;
  private Strength strength;
  private Defense defense;
  private Intelligence intelligence;

  public AvatarStats(String id, Integer strength, Integer defense, Integer intelligence) {
    this.id = id;
    this.strength = new Strength(strength);
    this.defense = new Defense(defense);
    this.intelligence = new Intelligence(intelligence);
  }

  @Override
  public String getId() {
    return this.id;
  }

  public Strength getStrength() {
    return strength;
  }

  public Defense getDefense() {
    return defense;
  }

  public Intelligence getIntelligence() {
    return intelligence;
  }

  public void incrementStrength() {
    this.strength = this.strength.increment();
  }

  public void incrementDefense() {
    this.defense = this.defense.increment();
  }

  public void incrementIntelligence() {
    this.intelligence = this.intelligence.increment();
  }
}
