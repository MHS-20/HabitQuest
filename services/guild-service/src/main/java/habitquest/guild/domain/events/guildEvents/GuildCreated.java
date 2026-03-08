package habitquest.guild.domain.events.guildEvents;

public record GuildCreated(String guildId, String guildName) implements GuildEvent {}
