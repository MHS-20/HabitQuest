package habitquest.guild.domain.events;

public record SpellCasted(String battleId, String spellId) implements BattleEvent {}
