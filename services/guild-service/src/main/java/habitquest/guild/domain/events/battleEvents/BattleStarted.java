package habitquest.guild.domain.events.battleEvents;

public record BattleStarted(String battleId, String guildId) implements BattleEvent {}
