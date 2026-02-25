package habitquest.guild.domain.events;

import habitquest.guild.domain.battle.Penalty;

public record BattleLost(String battleId, Penalty penalty) implements BattleEvent {}
