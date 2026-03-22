package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;

public record GuildCreated(Id<Guild> guildId, Id<GuildMember> leaderId, String guildName)
    implements GuildEvent {
  public GuildCreated(String guildId, String leaderId, String guildName) {
    this(new Id<>(guildId), new Id<>(leaderId), guildName);
  }
}
