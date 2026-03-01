package habitquest.guild.domain.events;

import habitquest.guild.domain.guild.GuildRole;

public record RoleAssigned(String guildId, String memberId, GuildRole newRole)
    implements GuildEvent {}
