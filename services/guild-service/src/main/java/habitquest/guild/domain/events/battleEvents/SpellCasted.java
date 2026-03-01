package habitquest.guild.domain.events.battleEvents;

public record SpellCasted(String battleId, String spellId) implements BattleEvent {}
