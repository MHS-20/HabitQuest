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

  @Mock private QuestRepository questRepository;
  @Mock private QuestObserver questObserver;
  @Mock private QuestFactory questFactory;
  private QuestServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new QuestServiceImpl(questRepository, questObserver, questFactory);
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
      Quest replacement = new Quest(new Id<>("quest-2"), "Evening Routine");
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
      Quest updated = service.updateName(QUEST_ID, "Evening Routine");
      assertThat(updated.getName()).isEqualTo("Evening Routine");
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
}
