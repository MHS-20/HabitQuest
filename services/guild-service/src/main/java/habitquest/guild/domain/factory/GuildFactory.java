package habitquest.guild.domain.factory;

import common.ddd.Factory;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;

public class GuildFactory implements Factory {
  private final IdGenerator idGenerator;

  public GuildFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Guild create(String name, String creatorAvatarId, String creatorNickname) {
    return new Guild(
        name, new GuildMember(creatorAvatarId, creatorNickname, new GuildRole("Leader")));
  }
}
