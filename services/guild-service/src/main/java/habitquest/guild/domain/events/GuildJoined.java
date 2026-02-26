package habitquest.guild.domain.events;

import habitquest.guild.domain.guild.GuildMember;

public record GuildJoined(String guildId, GuildMember member) implements GuildEvent {}
