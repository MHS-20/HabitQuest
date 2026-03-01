package habitquest.guild.domain.events.guildEvents;

import habitquest.guild.domain.guild.GuildRole;

public record RoleAssigned(String guildId, String memberId, GuildRole newRole)
    implements GuildEvent {}
