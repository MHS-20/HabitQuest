package habitquest.guild.domain.events;

public interface BattleObserver {
    void notifyBattleEvent(BattleEvent event);
}
