package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;

public record RemovedFromGuild(Id<Guild> guildId, Id<GuildMember> memberId) implements GuildEvent {}
