package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;

public record RoleAssigned(Id<Guild> guildId, Id<GuildMember> memberId, GuildRole newRole)
    implements GuildEvent {}
