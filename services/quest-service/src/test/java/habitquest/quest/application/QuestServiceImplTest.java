package habitquest.quest.application;

import static habitquest.quest.QuestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.quest.domain.*;
import habitquest.quest.domain.events.QuestCreated;
import habitquest.quest.domain.events.QuestEvent;
import habitquest.quest.domain.events.QuestObserver;
import habitquest.quest.domain.factory.QuestFactory;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestServiceImpl")
class QuestServiceImplTest {

  private static final String EVENING_ROUTINE = "Evening Routine";

  @Mock private QuestRepository questRepository;
  @Mock private ActiveQuestsRepository activeQuestsRepository;
  @Mock private QuestObserver questObserver;
  @Mock private QuestFactory questFactory;
  private QuestServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new QuestServiceImpl(questRepository, activeQuestsRepository, questObserver, questFactory);
  }

  @Nested
  @DisplayName("createQuest")
  class CreateQuest {

    @Test
    @DisplayName("saves and emits QuestCreated event")
    void createsAndEmitsEvent() {
      Quest quest = fullQuest();
      when(questFactory.createQuest(QUEST_NAME)).thenReturn(quest);
      when(questRepository.save(any(Quest.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      Quest created = service.createQuest(QUEST_NAME);

      assertThat(created.getId().value()).isNotBlank();
      assertThat(created.getName()).isEqualTo(QUEST_NAME);
      verify(questRepository).save(created);

      ArgumentCaptor<QuestEvent> eventCaptor = ArgumentCaptor.forClass(QuestEvent.class);
      verify(questObserver).notifyQuestEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue()).isInstanceOf(QuestCreated.class);
      assertThat(((QuestCreated) eventCaptor.getValue()).quest()).isSameAs(created);
    }
  }

  @Nested
  @DisplayName("getAllQuests")
  class GetAllQuests {

    @Test
    @DisplayName("returns all quests from repository")
    void returnsAll() {
      Quest first = fullQuest();
      Quest second = new Quest(new Id<>("quest-2"), EVENING_ROUTINE);
      when(questRepository.findAll()).thenReturn(List.of(first, second));

      List<Quest> quests = service.getAllQuests();

      assertThat(quests).hasSize(2);
      verify(questRepository).findAll();
    }
  }

  @Nested
  @DisplayName("getQuest")
  class GetQuest {

    @Test
    @DisplayName("returns quest when found")
    void found() {
      Quest quest = fullQuest();
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      assertThat(service.getQuest(QUEST_ID)).isSameAs(quest);
    }

    @Test
    @DisplayName("throws QuestNotFoundException when id is unknown")
    void notFound() {
      when(questRepository.findById(UNKNOWN_ID))
          .thenThrow(new QuestNotFoundException(UNKNOWN_ID.value()));
      assertThatThrownBy(() -> service.getQuest(UNKNOWN_ID))
          .isInstanceOf(QuestNotFoundException.class)
          .hasMessage(UNKNOWN_ID.value());
    }
  }

  @Nested
  @DisplayName("updateQuest")
  class UpdateQuest {

    @Test
    @DisplayName("checks existence and saves the provided quest")
    void updates() {
      Quest existing = fullQuest();
      Quest replacement = new Quest(new Id<>("quest-2"), EVENING_ROUTINE);
      when(questRepository.findById(QUEST_ID)).thenReturn(existing);
      when(questRepository.save(replacement)).thenReturn(replacement);
      Quest updated = service.updateQuest(QUEST_ID, replacement);
      assertThat(updated).isSameAs(replacement);
      verify(questRepository).findById(QUEST_ID);
      verify(questRepository).save(replacement);
      verify(questObserver, never()).notifyQuestEvent(any());
    }
  }

  @Nested
  @DisplayName("deleteQuest")
  class DeleteQuest {

    @Test
    @DisplayName("delegates to repository delete")
    void deletes() {
      service.deleteQuest(QUEST_ID);
      verify(questRepository).deleteById(QUEST_ID);
      verify(questObserver, never()).notifyQuestEvent(any());
    }
  }

  @Nested
  @DisplayName("getter delegates")
  class GetterDelegates {

    @Test
    @DisplayName("getName returns quest name")
    void getName() {
      when(questRepository.findById(QUEST_ID)).thenReturn(fullQuest());
      assertThat(service.getName(QUEST_ID)).isEqualTo(QUEST_NAME);
    }

    @Test
    @DisplayName("getDuration returns quest duration")
    void getDuration() {
      when(questRepository.findById(QUEST_ID)).thenReturn(fullQuest());
      assertThat(service.getDuration(QUEST_ID)).isEqualTo(DEFAULT_DURATION);
    }

    @Test
    @DisplayName("getReward returns quest reward")
    void getReward() {
      when(questRepository.findById(QUEST_ID)).thenReturn(fullQuest());
      assertThat(service.getReward(QUEST_ID)).isEqualTo(DEFAULT_MONEY_REWARD);
    }

    @Test
    @DisplayName("getHabits returns quest habits")
    void getHabits() {
      when(questRepository.findById(QUEST_ID)).thenReturn(fullQuest());
      assertThat(service.getHabits(QUEST_ID)).hasSize(1);
      assertThat(service.getHabits(QUEST_ID).getFirst().getId()).isEqualTo(HABIT_ID_1);
    }

    @Test
    @DisplayName("propagates QuestNotFoundException from repository")
    void notFound() {
      when(questRepository.findById(UNKNOWN_ID))
          .thenThrow(new QuestNotFoundException(UNKNOWN_ID.value()));
      assertThatThrownBy(() -> service.getName(UNKNOWN_ID))
          .isInstanceOf(QuestNotFoundException.class)
          .hasMessage(UNKNOWN_ID.value());
    }
  }

  @Nested
  @DisplayName("updateName")
  class UpdateName {

    @Test
    @DisplayName("updates name and saves")
    void updates() {
      Quest quest = fullQuest();
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(questRepository.save(quest)).thenReturn(quest);
      Quest updated = service.updateName(QUEST_ID, EVENING_ROUTINE);
      assertThat(updated.getName()).isEqualTo(EVENING_ROUTINE);
      verify(questRepository).save(quest);
    }
  }

  @Nested
  @DisplayName("updateDuration")
  class UpdateDuration {

    @Test
    @DisplayName("updates duration and saves")
    void updates() {
      Quest quest = fullQuest();
      Duration newDuration = Duration.ofMinutes(45);
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(questRepository.save(quest)).thenReturn(quest);
      Quest updated = service.updateDuration(QUEST_ID, newDuration);
      assertThat(updated.getDuration()).isEqualTo(newDuration);
      verify(questRepository).save(quest);
    }
  }

  @Nested
  @DisplayName("updateReward")
  class UpdateReward {

    @Test
    @DisplayName("updates reward and saves")
    void updates() {
      Quest quest = fullQuest();
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(questRepository.save(quest)).thenReturn(quest);
      Quest updated = service.updateReward(QUEST_ID, DEFAULT_MONEY_REWARD);
      assertThat(updated.getReward()).isEqualTo(DEFAULT_MONEY_REWARD);
      verify(questRepository).save(quest);
    }
  }

  @Nested
  @DisplayName("addHabit")
  class AddHabit {

    @Test
    @DisplayName("adds habit and saves")
    void adds() {
      Quest quest = fullQuest();
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(questRepository.save(quest)).thenReturn(quest);
      Quest updated = service.addHabit(QUEST_ID, meditationHabit());
      assertThat(updated.getHabits()).extracting(Habit::getId).contains(HABIT_ID_1, HABIT_ID_2);
      verify(questRepository).save(quest);
    }
  }

  @Nested
  @DisplayName("removeHabit")
  class RemoveHabit {

    @Test
    @DisplayName("removes habit and saves")
    void removes() {
      Quest quest = questWithMeditationHabit();
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(questRepository.save(quest)).thenReturn(quest);
      Quest updated = service.removeHabit(QUEST_ID, HABIT_ID_2);
      assertThat(updated.getHabits()).isEmpty();
      verify(questRepository).save(quest);
    }
  }

  @Nested
  @DisplayName("recordHabitAttendance")
  class RecordHabitAttendance {

    @Test
    @DisplayName("creates active quest progress when missing and stores attendance")
    void createsProgressAndStoresAttendance() {
      Quest quest = fullQuest();
      LocalDate attendedOn = LocalDate.of(2026, 4, 2);

      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(activeQuestsRepository.findByQuestIdAndAvatarId(QUEST_ID, AVATAR_ID_1))
          .thenReturn(Optional.empty());
      when(activeQuestsRepository.save(any(ActiveQuests.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      ActiveQuests updated =
          service.recordHabitAttendance(QUEST_ID, AVATAR_ID_1, HABIT_ID_1, attendedOn);

      assertThat(updated.getQuestId()).isEqualTo(QUEST_ID);
      assertThat(updated.getAvatarId()).isEqualTo(AVATAR_ID_1);
      assertThat(updated.getAttendedOccurrences().get(HABIT_ID_1)).isEqualTo(1);
      verify(activeQuestsRepository).save(any(ActiveQuests.class));
    }

    @Test
    @DisplayName("emits QuestCompleted when progress reaches completion")
    void emitsQuestCompletedOnCompletion() {
      Quest quest = fullQuest();
      LocalDate attendedOn = LocalDate.of(2026, 4, 3);
      ActiveQuests active =
          ActiveQuests.fromQuest(new Id<>("active-1"), AVATAR_ID_1, attendedOn, quest);

      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(activeQuestsRepository.findByQuestIdAndAvatarId(QUEST_ID, AVATAR_ID_1))
          .thenReturn(Optional.of(active));
      when(activeQuestsRepository.save(any(ActiveQuests.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      service.recordHabitAttendance(QUEST_ID, AVATAR_ID_1, HABIT_ID_1, attendedOn);

      verify(questObserver)
          .notifyQuestEvent(
              argThat(event -> event instanceof habitquest.quest.domain.events.QuestCompleted));
    }
  }

  @Nested
  @DisplayName("joinQuest")
  class JoinQuest {

    @Test
    @DisplayName("creates active quest and emits QuestJoined when avatar joins for first time")
    void createsActiveQuestOnFirstJoin() {
      Quest quest = fullQuest();
      LocalDate joinedOn = LocalDate.of(2026, 4, 3);

      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(activeQuestsRepository.findByQuestIdAndAvatarId(QUEST_ID, AVATAR_ID_1))
          .thenReturn(Optional.empty());
      when(activeQuestsRepository.save(any(ActiveQuests.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      ActiveQuests joined = service.joinQuest(QUEST_ID, AVATAR_ID_1, joinedOn);

      assertThat(joined.getQuestId()).isEqualTo(QUEST_ID);
      assertThat(joined.getAvatarId()).isEqualTo(AVATAR_ID_1);
      verify(activeQuestsRepository).save(any(ActiveQuests.class));
      verify(questObserver)
          .notifyQuestEvent(
              argThat(event -> event instanceof habitquest.quest.domain.events.QuestJoined));
    }

    @Test
    @DisplayName("returns existing active quest without emitting duplicate QuestJoined")
    void returnsExistingJoinState() {
      Quest quest = fullQuest();
      LocalDate joinedOn = LocalDate.of(2026, 4, 3);
      ActiveQuests existing =
          ActiveQuests.fromQuest(new Id<>("active-existing"), AVATAR_ID_1, joinedOn, quest);

      when(questRepository.findById(QUEST_ID)).thenReturn(quest);
      when(activeQuestsRepository.findByQuestIdAndAvatarId(QUEST_ID, AVATAR_ID_1))
          .thenReturn(Optional.of(existing));

      ActiveQuests joined = service.joinQuest(QUEST_ID, AVATAR_ID_1, joinedOn);

      assertThat(joined).isSameAs(existing);
      verify(activeQuestsRepository, never()).save(any(ActiveQuests.class));
      verify(questObserver, never())
          .notifyQuestEvent(
              argThat(event -> event instanceof habitquest.quest.domain.events.QuestJoined));
    }
  }

  @Nested
  @DisplayName("getActiveQuestProgressByAvatar")
  class GetActiveQuestProgressByAvatar {

    @Test
    @DisplayName("returns quest progress with completion and per-habit counters")
    void returnsQuestProgress() {
      Quest quest = fullQuest();
      LocalDate startedOn = LocalDate.of(2026, 4, 3);
      ActiveQuests active =
          ActiveQuests.fromQuest(new Id<>("active-2"), AVATAR_ID_1, startedOn, quest);
      active.recordAttendance(HABIT_ID_1, startedOn);

      when(activeQuestsRepository.findByAvatarId(AVATAR_ID_1)).thenReturn(List.of(active));
      when(questRepository.findById(QUEST_ID)).thenReturn(quest);

      List<QuestProgressView> result = service.getActiveQuestProgressByAvatar(AVATAR_ID_1);

      assertThat(result).hasSize(1);
      QuestProgressView first = result.getFirst();
      assertThat(first.questId()).isEqualTo(QUEST_ID.value());
      assertThat(first.completionPercentage()).isEqualTo(100);
      assertThat(first.habits()).hasSize(1);
      assertThat(first.habits().getFirst().remainingOccurrences()).isZero();
    }
  }
}
