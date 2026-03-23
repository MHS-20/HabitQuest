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
      case BattleStarted e -> handleBattleStarted(e);
      case BattleWon e -> handleBattleWon(e);
      case BattleLost e -> handleBattleLost(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }

  public void handleBattleStarted(BattleStarted e) {
    battleNotifier.notifyBattleStarted(e);
  }

  public void handleBattleWon(BattleWon e) {
    battleNotifier.notifyBattleWon(e);
  }

  public void handleBattleLost(BattleLost e) {
    battleNotifier.notifyBattleLost(e);
  }
}
