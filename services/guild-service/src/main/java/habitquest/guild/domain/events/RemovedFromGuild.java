package habitquest.guild.domain.events;

public record RemovedFromGuild(String guildId, String memberId) implements GuildEvent {}
