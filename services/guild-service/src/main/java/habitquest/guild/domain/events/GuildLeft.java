package habitquest.guild.domain.events;

public record GuildLeft(String guildId, String member) implements GuildEvent {}
