package habitquest.guild.domain.events.battleEvents;

public record BattleLost(String battleId, String guildId, Integer penalty) implements BattleEvent {}
