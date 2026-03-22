package habitquest.guild.domain.guild;

import common.ddd.Entity;
import common.ddd.Id;

public class GuildMember implements Entity<Id<GuildMember>> {
  private final Id<GuildMember> avatarId;
  private String nickname;
  private GuildRole role;

  public GuildMember(Id<GuildMember> avatarId, String nickname, GuildRole role) {
    this.avatarId = avatarId;
    this.nickname = nickname;
    this.role = role;
  }

  @Override
  public Id<GuildMember> getId() {
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
