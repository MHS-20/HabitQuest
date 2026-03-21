package habitquest.guild.domain.events.guildEvents;

public record GuildCreated(String guildId, String leaderId, String guildName)
    implements GuildEvent {}
