package habitquest.guild.domain.events.guildEvents;

public record RemovedFromGuild(String guildId, String memberId) implements GuildEvent {}
