package habitquest.guild.domain.events;

import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;

public record RoleAssigned(GuildMember member, GuildRole newRole) implements GuildEvent {}
