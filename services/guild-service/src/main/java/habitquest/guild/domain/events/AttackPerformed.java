package habitquest.guild.domain.events;

public record AttackPerformed(String battleId, String attackerId, String receiverId)
    implements BattleEvent {}
