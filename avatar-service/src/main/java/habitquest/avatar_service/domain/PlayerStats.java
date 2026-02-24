package habitquest.avatar_service.domain;

import common.ddd.Aggregate;

public class PlayerStats implements Aggregate<String> {

  private String id;

  public PlayerStats(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
