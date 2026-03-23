package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;

public record RankUpdated(Id<Guild> guildId) implements GuildEvent {}
