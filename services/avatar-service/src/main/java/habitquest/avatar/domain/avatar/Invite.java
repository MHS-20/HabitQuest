package habitquest.avatar.domain.avatar;

import common.ddd.Id;
import java.time.Instant;

public record Invite(Id<Invite> inviteId, Id<Guild> guildId, String guildName, Instant expiresAt) {}
