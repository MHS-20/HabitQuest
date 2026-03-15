package habitquest.guild.domain.factory;

import common.ddd.Factory;
import habitquest.guild.domain.guild.Invite;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InviteFactory implements Factory {
  private final IdGenerator idGenerator;

  public InviteFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Invite create(String guildId, String targetAvatarId) {
    return new Invite(
        idGenerator.nextId(), guildId, targetAvatarId, Instant.now().plusSeconds(86400));
  }
}
