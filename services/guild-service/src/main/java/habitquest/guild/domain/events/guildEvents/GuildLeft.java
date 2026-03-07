package habitquest.guild.domain.events.guildEvents;

public record GuildLeft(String guildId, String member) implements GuildEvent {}
