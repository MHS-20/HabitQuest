package habitquest.quest.application;

import java.util.List;

public record QuestProgressView(
    String questId,
    String questName,
    String status,
    int completionPercentage,
    List<HabitProgressView> habits) {

  public record HabitProgressView(
      String habitId,
      String title,
      int requiredOccurrences,
      int attendedOccurrences,
      int remainingOccurrences) {}
}
