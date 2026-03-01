package habitquest.guild.domain.events;

public record GuildJoined(String guildId, String memberId) implements GuildEvent {}
