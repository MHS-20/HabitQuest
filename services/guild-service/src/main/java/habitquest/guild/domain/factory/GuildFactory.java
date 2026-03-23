package habitquest.guild.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;

public class GuildFactory implements Factory {
  private final IdGenerator idGenerator;

  public GuildFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Guild create(String name, Id<GuildMember> creatorAvatarId, String creatorNickname) {
    return new Guild(
        new Id<>(idGenerator.nextId()),
        name,
        new GuildMember(creatorAvatarId, creatorNickname, GuildRole.LEADER));
  }
}
