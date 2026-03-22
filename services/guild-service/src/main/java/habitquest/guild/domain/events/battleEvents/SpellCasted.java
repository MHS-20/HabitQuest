package habitquest.guild.domain.events.battleEvents;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;

public record SpellCasted(Id<Battle> battleId, String spellId) implements BattleEvent {}
