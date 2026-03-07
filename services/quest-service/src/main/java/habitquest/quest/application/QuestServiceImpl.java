package habitquest.quest.application;

import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestObserver;
import java.time.Duration;
import java.util.List;

public class QuestServiceImpl implements QuestService {

  private final QuestRepository questRepository;
  private final QuestObserver questObserver;

  public QuestServiceImpl(QuestRepository questRepository, QuestObserver questObserver) {
    this.questRepository = questRepository;
    this.questObserver = questObserver;
  }

  @Override
  public Quest createQuest(String name) {
    Quest quest = new Quest(name);
    questRepository.save(quest);
    questObserver.notifyQuestEvent(new QuestCreated(quest));
    return quest;
  }

  @Override
  public Quest getQuest(String id) throws QuestNotFoundException {
    return questRepository.findById(id);
  }

  @Override
  public Quest updateQuest(String id, Quest quest) throws QuestNotFoundException {
    questRepository.findById(id);
    return questRepository.save(quest);
  }

  @Override
  public void deleteQuest(String id) throws QuestNotFoundException {
    questRepository.deleteById(id);
  }

  // region getters

  @Override
  public String getName(String questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getName();
  }

  @Override
  public Duration getDuration(String questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getDuration();
  }

  @Override
  public Reward getReward(String questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getReward();
  }

  @Override
  public List<Habit> getHabits(String questId) throws QuestNotFoundException {
    return questRepository.findById(questId).getHabits();
  }

  // endregion

  // region updaters

  @Override
  public Quest updateName(String questId, String name) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setName(name);
    return questRepository.save(quest);
  }

  @Override
  public Quest updateDuration(String questId, Duration duration) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setDuration(duration);
    return questRepository.save(quest);
  }

  @Override
  public Quest updateReward(String questId, Reward reward) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.setReward(reward);
    return questRepository.save(quest);
  }

  @Override
  public Quest addHabit(String questId, Habit habit) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.addHabit(habit);
    return questRepository.save(quest);
  }

  @Override
  public Quest removeHabit(String questId, Habit habit) throws QuestNotFoundException {
    Quest quest = questRepository.findById(questId);
    quest.removeHabit(habit);
    return questRepository.save(quest);
  }

  // endregion
}
