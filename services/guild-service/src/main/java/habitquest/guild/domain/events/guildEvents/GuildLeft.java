package habitquest.guild.domain.events.guildEvents;

public record GuildLeft(String guildId, String memberId) implements GuildEvent {}
