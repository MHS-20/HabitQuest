package habitquest.guild.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.Invite;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InviteFactory implements Factory {
  private final IdGenerator idGenerator;

  public InviteFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Invite create(Id<Guild> guildId, Id<GuildMember> targetAvatarId) {
    return new Invite(
        new Id<>(idGenerator.nextId()), guildId, targetAvatarId, Instant.now().plusSeconds(86400));
  }
}
