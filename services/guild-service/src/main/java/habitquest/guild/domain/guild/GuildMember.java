package habitquest.guild.domain.guild;

import common.ddd.Entity;

public class GuildMember implements Entity<String> {
  private final String avatarId;
  private String nickname;
  private GuildRole role;

  public GuildMember(String avatarId) {
    this.avatarId = avatarId;
  }

  @Override
  public String getId() {
    return this.avatarId;
  }
}
