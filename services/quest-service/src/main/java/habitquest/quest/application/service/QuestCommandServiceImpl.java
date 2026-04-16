package habitquest.quest.application.service;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.exceptions.AvatarRewardException;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.application.port.in.QuestCommandService;
import habitquest.quest.application.port.out.*;
import habitquest.quest.domain.*;
import habitquest.quest.domain.events.*;
import habitquest.quest.domain.factory.QuestFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class QuestCommandServiceImpl implements QuestCommandService {

  private final QuestRepository questRepository;
  private final ActiveQuestsRepository activeQuestsRepository;
  private final AvatarClientPort avatarClient;
  private final TrackingHabitsClientPort trackingHabitsClient;
  private final QuestObserver questObserver;
  private final QuestFactory questFactory;
  private final QuestLogger log;

  public QuestCommandServiceImpl(
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
  public Quest updateQuest(Id<Quest> id, Quest quest) throws QuestNotFoundException {
    questRepository.findById(id);
    return questRepository.save(quest);
  }

  @Override
  public void deleteQuest(Id<Quest> id) throws QuestNotFoundException {
    questRepository.deleteById(id);
  }

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
}
