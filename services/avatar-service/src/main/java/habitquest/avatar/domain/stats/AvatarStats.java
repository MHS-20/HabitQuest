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
}
