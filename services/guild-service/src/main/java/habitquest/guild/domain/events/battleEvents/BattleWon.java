package habitquest.guild.domain.events.battleEvents;

import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;

public record BattleWon(
    String battleId, String guildId, Experience experienceReward, Money moneyReward)
    implements BattleEvent {}
