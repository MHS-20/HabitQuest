package habitquest.guild.domain.events.guildEvents;

import common.ddd.Id;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.Invite;

public record InviteSent(Id<Guild> guildId, Id<GuildMember> targetAvatarId, Id<Invite> inviteId)
    implements GuildEvent {
  public InviteSent(String guildId, String targetAvatarId, String inviteId) {
    this(new Id<>(guildId), new Id<>(targetAvatarId), new Id<>(inviteId));
  }
}
