package habitquest.guild.domain.events.battleEvents;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.guild.GuildMember;

public record AttackPerformed(
    Id<Battle> battleId, Id<GuildMember> attackerId, Id<GuildMember> receiverId)
    implements BattleEvent {}
