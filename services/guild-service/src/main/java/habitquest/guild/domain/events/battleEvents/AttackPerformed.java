package habitquest.guild.domain.events.battleEvents;

public record AttackPerformed(String battleId, String attackerId, String receiverId)
    implements BattleEvent {}
