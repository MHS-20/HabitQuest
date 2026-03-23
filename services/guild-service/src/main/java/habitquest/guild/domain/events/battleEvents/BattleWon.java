package habitquest.guild.domain.events.battleEvents;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.guild.Guild;

public record BattleWon(
    Id<Battle> battleId, Id<Guild> guildId, Integer experienceReward, Integer moneyReward)
    implements BattleEvent {}
