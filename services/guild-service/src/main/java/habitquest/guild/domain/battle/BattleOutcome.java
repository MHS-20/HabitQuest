package habitquest.guild.domain.battle;

public sealed interface BattleOutcome
    permits BattleOutcome.Ongoing, BattleOutcome.Won, BattleOutcome.Lost {

  record Ongoing() implements BattleOutcome {}

  record Won(int experienceReward, int moneyReward) implements BattleOutcome {}

  record Lost(int penalty) implements BattleOutcome {}

  default boolean isOver() {
    return this instanceof Won || this instanceof Lost;
  }

  default boolean isWon() {
    return this instanceof Won;
  }

  default boolean isLost() {
    return this instanceof Lost;
  }
}
