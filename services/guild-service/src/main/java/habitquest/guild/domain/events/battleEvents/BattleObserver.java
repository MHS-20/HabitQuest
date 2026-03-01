package habitquest.guild.domain.events.battleEvents;

public interface BattleObserver {
  void notifyBattleEvent(BattleEvent event);
}
