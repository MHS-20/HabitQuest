package habitquest.guild.domain.events.battleEvents;

import habitquest.guild.domain.battle.Penalty;

public record BattleLost(String battleId, Penalty penalty) implements BattleEvent {}
