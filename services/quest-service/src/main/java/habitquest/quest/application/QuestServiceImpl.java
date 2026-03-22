package habitquest.quest.application;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestObserver;
import habitquest.quest.domain.factory.QuestFactory;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class QuestServiceImpl implements QuestService {

  private final QuestRepository questRepository;
  private final QuestObserver questObserver;
  private final QuestFactory questFactory;

  public QuestServiceImpl(
      QuestRepository questRepository, QuestObserver questObserver, QuestFactory questFactory) {
    this.questRepository = questRepository;
    this.questObserver = questObserver;
    this.questFactory = questFactory;
  }

  @Override
  public Quest createQuest(String name) {
    Quest quest = questFactory.createQuest(name);
    questRepository.save(quest);
    questObserver.notifyQuestEvent(new QuestCreated(quest));
    return quest;
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

  // endregion
}
