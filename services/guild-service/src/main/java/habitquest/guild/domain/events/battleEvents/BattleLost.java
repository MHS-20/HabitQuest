package habitquest.guild.domain.events.battleEvents;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.guild.Guild;

public record BattleLost(Id<Battle> battleId, Id<Guild> guildId, Integer penalty)
    implements BattleEvent {}
