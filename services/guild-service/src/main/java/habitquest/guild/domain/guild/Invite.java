package habitquest.guild.domain.guild;

import common.ddd.Id;
import java.time.Instant;

public record Invite(
    Id<Invite> inviteId, Id<Guild> guildId, Id<GuildMember> avatarId, Instant expiresAt) {}
