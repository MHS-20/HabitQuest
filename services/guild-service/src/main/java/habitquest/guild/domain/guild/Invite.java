package habitquest.guild.domain.guild;

import java.time.Instant;

public record Invite(String inviteId, String guildId, String avatarId, Instant expiresAt) {}
