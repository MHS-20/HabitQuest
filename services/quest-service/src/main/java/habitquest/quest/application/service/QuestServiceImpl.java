package habitquest.quest.application.service;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.exceptions.AvatarRewardException;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.port.in.QuestService;
import habitquest.quest.application.port.out.*;
import habitquest.quest.domain.ActiveQuests;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.domain.events.QuestCompleted;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestJoined;
import habitquest.quest.domain.events.QuestObserver;
import habitquest.quest.domain.factory.QuestFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class QuestServiceImpl implements QuestService {

  private final QuestRepository questRepository;
  private final ActiveQuestsRepository activeQuestsRepository;
  private final AvatarClientPort avatarClient;
  private final TrackingHabitsClientPort trackingHabitsClient;
  private final QuestObserver questObserver;
  private final QuestFactory questFactory;
  private final QuestLogger log;

  public QuestServiceImpl(
      QuestRepository questRepository,
      ActiveQuestsRepository activeQuestsRepository,
      AvatarClientPort avatarClient,
      TrackingHabitsClientPort trackingHabitsClient,
      QuestObserver questObserver,
      QuestFactory questFactory,
      QuestLogger log) {
    this.questRepository = questRepository;
    this.activeQuestsRepository = activeQuestsRepository;
    this.avatarClient = avatarClient;
    this.trackingHabitsClient = trackingHabitsClient;
    this.questObserver = questObserver;
    this.questFactory = questFactory;
    this.log = log;
  }

  @Override
  public Quest createQuest(String name, Duration duration) {
    Quest quest = questFactory.createQuest(name, duration);
    questRepository.save(quest);
    questObserver.notifyQuestEvent(new QuestCreated(quest));
    return quest;
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
  public Quest updateQuest(Id<Quest> id, Quest quest) throws QuestNotFoundException {
    questRepository.findById(id);
    return questRepository.save(quest);
  }

  @Override
  public void deleteQuest(Id<Quest> id) throws QuestNotFoundException {
    questRepository.deleteById(id);
  }

  // region getters

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

  // endregion

  // region updaters

  @Override
  public Quest updateName(Id<Quest> questId, String name) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setName(name);
    return questRepository.save(quest);
  }

  @Override
  public Quest updateDuration(Id<Quest> questId, Duration duration) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setDuration(duration);
    return questRepository.save(quest);
  }

  @Override
  public Quest updateReward(Id<Quest> questId, MoneyReward reward) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setReward(reward);
    return questRepository.save(quest);
  }

  @Override
  public Quest addHabit(Id<Quest> questId, Habit habit) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.addHabit(habit);
    return questRepository.save(quest);
  }

  @Override
  public Quest removeHabit(Id<Quest> questId, Id<Habit> habit) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.removeHabit(habit);
    return questRepository.save(quest);
  }

  @Override
  public ActiveQuests recordHabitAttendance(
      Id<Quest> questId, Id<Avatar> avatarId, Id<Habit> habitId, LocalDate attendedOn)
      throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);

    ActiveQuests activeQuest =
        activeQuestsRepository
            .findByQuestIdAndAvatarId(questId, avatarId)
            .orElseGet(
                () ->
                    ActiveQuests.fromQuest(
                        new Id<>(UUID.randomUUID().toString()), avatarId, attendedOn, quest));

    ActiveQuests.Status previousStatus = activeQuest.getStatus();
    activeQuest.recordAttendance(habitId, attendedOn);
    activeQuest.refreshStatus(attendedOn);

    ActiveQuests saved = activeQuestsRepository.save(activeQuest);
    if (previousStatus != ActiveQuests.Status.COMPLETED
        && saved.getStatus() == ActiveQuests.Status.COMPLETED) {
      try {
        avatarClient.earnMoney(avatarId, quest.getReward().value());
      } catch (AvatarRewardException ex) {
        log.error(saved, "Failed to grant quest completion money reward", ex);
      }
      questObserver.notifyQuestEvent(new QuestCompleted(quest, avatarId));
    }

    return saved;
  }

  @Override
  public ActiveQuests joinQuest(Id<Quest> questId, Id<Avatar> avatarId, LocalDate joinedOn)
      throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    Optional<ActiveQuests> existing =
        activeQuestsRepository.findByQuestIdAndAvatarId(questId, avatarId);
    if (existing.isPresent()) {
      return existing.get();
    }

    ActiveQuests joined =
        ActiveQuests.fromQuest(new Id<>(UUID.randomUUID().toString()), avatarId, joinedOn, quest);
    trackingHabitsClient.createQuestHabitsForAvatar(avatarId, questId, quest.getHabits());
    ActiveQuests saved = activeQuestsRepository.save(joined);
    questObserver.notifyQuestEvent(new QuestJoined(quest, avatarId));
    return saved;
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
                // Apply damage if quest expired without being completed
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
                      .collect(java.util.stream.Collectors.toMap(Habit::getId, Habit::getTitle));

              int totalHabits = active.getRequiredOccurrences().size();
              long attendedHabits =
                  active.getAttendedOccurrences().values().stream()
                      .filter(attended -> attended > 0)
                      .count();
              int completion =
                  totalHabits == 0 ? 0 : (int) Math.round((attendedHabits * 100.0) / totalHabits);

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

  // endregion
}
