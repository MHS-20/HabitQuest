package habitquest.guild.domain.events.guildEvents;

public record GuildJoined(String guildId, String memberId) implements GuildEvent {}
