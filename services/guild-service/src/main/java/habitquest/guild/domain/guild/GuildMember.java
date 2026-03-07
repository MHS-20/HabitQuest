package habitquest.guild.domain.guild;

import common.ddd.Entity;

public class GuildMember implements Entity<String> {
  private final String avatarId;
  private String nickname;
  private GuildRole role;

  public GuildMember(String avatarId, String nickname, GuildRole role) {
    this.avatarId = avatarId;
    this.nickname = nickname;
    this.role = role;
  }

  @Override
  public String getId() {
    return this.avatarId;
  }

  public String getNickname() {
    return this.nickname;
  }

  public GuildRole getRole() {
    return this.role;
  }

  public void promoteTo(GuildRole newRole) {
    this.role = newRole;
  }
}
