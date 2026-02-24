package habitquest.avatar_service.domain.stats;

import common.ddd.Aggregate;

public class AvatarStats implements Aggregate<String> {

  private String id;

  public AvatarStats(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
