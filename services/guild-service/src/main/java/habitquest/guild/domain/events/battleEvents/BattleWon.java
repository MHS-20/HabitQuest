package habitquest.guild.domain.events.battleEvents;

public record BattleWon(
    String battleId, String guildId, Integer experienceReward, Integer moneyReward)
    implements BattleEvent {}
