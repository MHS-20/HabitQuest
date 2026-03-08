package habitquest.guild.application;

import habitquest.guild.domain.events.battleEvents.*;
import org.springframework.stereotype.Component;

@Component
public class BattleObserverImpl implements BattleObserver {

  private final BattleNotifier battleNotifier;

  public BattleObserverImpl(BattleNotifier battleNotifier) {
    this.battleNotifier = battleNotifier;
  }

  @Override
  public void notifyBattleEvent(BattleEvent event) {
    switch (event) {
      case BattleStarted e -> battleNotifier.notifyBattleStarted(e);
      case BattleWon e -> battleNotifier.notifyBattleWon(e);
      case BattleLost e -> battleNotifier.notifyBattleLost(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }
}
