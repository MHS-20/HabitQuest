package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;

public record GuildDeleted(Id<Guild> guildId) implements GuildEvent {
  public GuildDeleted(String guildId) {
    this(new Id<>(guildId));
  }
}
