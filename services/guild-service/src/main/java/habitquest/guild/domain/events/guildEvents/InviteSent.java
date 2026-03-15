package habitquest.guild.domain.events.guildEvents;

public record InviteSent(String guildId, String targetAvatarId, String inviteId)
    implements GuildEvent {}
