package habitquest.quest.application.service;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.exceptions.AvatarRewardException;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.port.in.QuestQueryService;
import habitquest.quest.application.port.out.*;
import habitquest.quest.domain.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class QuestQueryServiceImpl implements QuestQueryService {

  private final QuestRepository questRepository;
  private final ActiveQuestsRepository activeQuestsRepository;
  private final AvatarClientPort avatarClient;
  private final QuestLogger log;

  public QuestQueryServiceImpl(
      QuestRepository questRepository,
      ActiveQuestsRepository activeQuestsRepository,
      AvatarClientPort avatarClient,
      QuestLogger log) {
    this.questRepository = questRepository;
    this.activeQuestsRepository = activeQuestsRepository;
    this.avatarClient = avatarClient;
    this.log = log;
  }

  @Override
  public List<Quest> getAllQuests() {
    return questRepository.findAll();
  }

  @Override
  public Quest getQuest(Id<Quest> id) throws QuestNotFoundException {
    return questRepository.findById(id);
  }

  @Override
  public String getName(Id<Quest> questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getName();
  }

  @Override
  public Duration getDuration(Id<Quest> questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getDuration();
  }

  @Override
  public Reward getReward(Id<Quest> questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getReward();
  }

  @Override
  public List<Habit> getHabits(Id<Quest> questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getHabits();
  }

  @Override
  public List<QuestProgressView> getActiveQuestProgressByAvatar(Id<Avatar> avatarId) {
    LocalDate today = LocalDate.now();

    return activeQuestsRepository.findByAvatarId(avatarId).stream()
        .map(
            active -> {
              ActiveQuests.Status before = active.getStatus();
              active.refreshStatus(today);
              ActiveQuests.Status after = active.getStatus();

              if (before != after) {
                activeQuestsRepository.save(active);
                if (after == ActiveQuests.Status.EXPIRED
                    && before != ActiveQuests.Status.COMPLETED) {
                  try {
                    avatarClient.applyDamage(avatarId, 10);
                  } catch (AvatarRewardException ex) {
                    log.error(active, "Failed to apply quest penalty damage", ex);
                  }
                }
              }

              Quest quest = questRepository.findById(active.getQuestId());
              Map<Id<Habit>, String> habitTitles =
                  quest.getHabits().stream()
                      .collect(Collectors.toMap(Habit::getId, Habit::getTitle));

              int totalRequiredOccurrences =
                  active.getRequiredOccurrences().values().stream()
                      .mapToInt(Integer::intValue)
                      .sum();
              int attendedOccurrences =
                  active.getAttendedOccurrences().values().stream()
                      .mapToInt(Integer::intValue)
                      .sum();
              int completion =
                  totalRequiredOccurrences == 0
                      ? 0
                      : (int) Math.round((attendedOccurrences * 100.0) / totalRequiredOccurrences);

              List<QuestProgressView.HabitProgressView> habitProgress =
                  active.getRequiredOccurrences().entrySet().stream()
                      .sorted(Comparator.comparing(entry -> entry.getKey().value()))
                      .map(
                          entry -> {
                            Id<Habit> habitId = entry.getKey();
                            int required = entry.getValue();
                            int attended = active.getAttendedOccurrences().getOrDefault(habitId, 0);
                            return new QuestProgressView.HabitProgressView(
                                habitId.value(),
                                habitTitles.getOrDefault(habitId, habitId.value()),
                                required,
                                attended,
                                active.remainingOccurrences(habitId));
                          })
                      .toList();

              return new QuestProgressView(
                  quest.getId().value(),
                  quest.getName(),
                  after.name(),
                  Math.clamp(completion, 0, 100),
                  habitProgress);
            })
        .sorted(Comparator.comparing(QuestProgressView::questName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }
}
