package habitquest.tracking.application;

import static habitquest.tracking.HabitFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitEvent;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import java.time.LocalDateTime;
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
@DisplayName("HabitServiceImpl")
class HabitServiceImplTest {

  @Mock private HabitRepository habitRepository;
  @Mock private HabitHistoryRepository historyRepository;
  @Mock private HabitFactory habitFactory;
  @Mock private HabitObserver habitObserver;

  private HabitServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new HabitServiceImpl(habitRepository, historyRepository, habitFactory, habitObserver);
  }

  @Nested
  @DisplayName("creation methods")
  class CreateHabit {

    @Test
    @DisplayName("createDailyHabit delegates to factory, saves, and returns habit")
    void createsDaily() {
      var habit = hydrateHabit();
      when(habitFactory.createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION)).thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      var created = service.createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION);
      verify(habitRepository).save(habit);
      verify(historyRepository).append(any(HabitHistoryEvent.class));
    }

    @Test
    @DisplayName("createWeeklyHabit delegates to factory, saves, and returns habit")
    void createsWeekly() {
      var habit = hydrateHabit();
      when(habitFactory.createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_WEEK))
          .thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      var created = service.createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_WEEK);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_WEEK);
      verify(habitRepository).save(habit);
      verify(historyRepository).append(any(HabitHistoryEvent.class));
    }

    @Test
    @DisplayName("createMonthlyHabit delegates to factory, saves, and returns habit")
    void createsMonthly() {
      var habit = hydrateHabit();
      when(habitFactory.createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_MONTH))
          .thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      var created = service.createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_MONTH);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, DEFAULT_DAY_OF_MONTH);
      verify(habitRepository).save(habit);
      verify(historyRepository).append(any(HabitHistoryEvent.class));
    }
  }

  @Nested
  @DisplayName("getHabitById")
  class GetHabitById {

    @Test
    @DisplayName("returns the habit when found")
    void found() {
      var habit = hydrateHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getHabitById(HABIT_ID)).isSameAs(habit);
    }

    @Test
    @DisplayName("throws HabitNotFoundException when id is unknown")
    void notFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getHabitById(UNKNOWN_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID.value());
    }
  }

  @Nested
  @DisplayName("deleteHabitById")
  class DeleteHabitById {

    @Test
    @DisplayName("deletes and emits HabitDeleted event")
    void deletesAndEmitsEvent() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      service.deleteHabitById(HABIT_ID);

      verify(habitRepository).deleteById(HABIT_ID);
      verify(historyRepository).append(any(HabitHistoryEvent.class));
      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitDeleted.class);
      assertThat(((HabitDeleted) captor.getValue()).habitId()).isEqualTo(HABIT_ID);
      assertThat(((HabitDeleted) captor.getValue()).avatarId()).isEqualTo(AVATAR_ID);
    }
  }

  @Nested
  @DisplayName("query delegates")
  class QueryDelegates {

    @Test
    @DisplayName("getTitle delegates to habit.getTitle()")
    void getTitle() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      assertThat(service.getTitle(HABIT_ID)).isEqualTo(TITLE);
    }

    @Test
    @DisplayName("getDescription delegates to habit.getDescription()")
    void getDescription() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      assertThat(service.getDescription(HABIT_ID)).isEqualTo(DESCRIPTION);
    }

    @Test
    @DisplayName("getTags delegates to habit.getTags()")
    void getTags() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      assertThat(service.getTags(HABIT_ID))
          .containsExactly(new Tag(TAG_HEALTH), new Tag(TAG_WELLNESS));
    }

    @Test
    @DisplayName("getRecurrence delegates to habit.getRecurrence()")
    void getRecurrence() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      assertThat(service.getRecurrence(HABIT_ID)).isEqualTo(DAILY_RECURRENCE);
    }

    @Test
    @DisplayName("getLastAttendedDate delegates to habit.getLastAttendedDate()")
    void getLastAttendedDate() {
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(hydrateHabit()));

      assertThat(service.getLastAttendedDate(HABIT_ID)).isEqualTo(ATTENDED_AT);
    }

    @Test
    @DisplayName("throws HabitNotFoundException when querying unknown habit")
    void queryNotFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getTitle(UNKNOWN_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID.value().toString());
    }
  }

  @Nested
  @DisplayName("updateTitle")
  class UpdateTitle {

    @Test
    @DisplayName("updates title and saves")
    void updates() {
      var habit = hydrateHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      var updated = service.updateTitle(HABIT_ID, "Read");

      assertThat(updated.getTitle()).isEqualTo("Read");
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("updateDescription")
  class UpdateDescription {

    @Test
    @DisplayName("updates description and saves")
    void updates() {
      var habit = hydrateHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      var updated = service.updateDescription(HABIT_ID, "Read 20 pages");

      assertThat(updated.getDescription()).isEqualTo("Read 20 pages");
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("updateTags")
  class UpdateTags {

    @Test
    @DisplayName("updates tags and saves")
    void updates() {
      var habit = hydrateHabit();
      var tags = List.of(new Tag(TAG_MINDSET));
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      var updated = service.updateTags(HABIT_ID, tags);

      assertThat(updated.getTags()).containsExactly(new Tag(TAG_MINDSET));
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("updateRecurrence")
  class UpdateRecurrence {

    @Test
    @DisplayName("updates recurrence and saves")
    void updates() {
      var habit = hydrateHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      var updated = service.updateRecurrence(HABIT_ID, WEEKLY_RECURRENCE);

      assertThat(updated.getRecurrence()).isEqualTo(WEEKLY_RECURRENCE);
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("attendHabit")
  class AttendHabit {

    @Test
    @DisplayName("updates last attended date, saves, and emits HabitAttended")
    void attendsAndEmitsEvent() {
      var habit = hydrateHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      var attended = service.attendHabit(HABIT_ID, NEXT_ATTENDED_AT);

      assertThat(attended.getLastAttendedDate()).isEqualTo(NEXT_ATTENDED_AT);
      verify(habitRepository).save(habit);

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitAttended.class);
      assertThat(((HabitAttended) captor.getValue()).habit()).isSameAs(habit);
      assertThat(((HabitAttended) captor.getValue()).avatarId()).isEqualTo(AVATAR_ID);
      verify(historyRepository).append(any(HabitHistoryEvent.class));
    }

    @Test
    @DisplayName("throws HabitNotFoundException for unknown habit")
    void notFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.attendHabit(UNKNOWN_ID, LocalDateTime.now()))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID.value().toString());
    }
  }

  @Nested
  @DisplayName("detectOverdueHabits")
  class DetectOverdueHabits {

    @Test
    @DisplayName("emits HabitNotAttended when last attended is null")
    void emitsForNeverAttended() {
      var neverAttended = neverAttendedHabit();
      when(habitRepository.findAll()).thenReturn(List.of(neverAttended));
      when(historyRepository.findByHabitId(neverAttended.getId())).thenReturn(List.of());

      service.detectOverdueHabits();

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitNotAttended.class);
      assertThat(((HabitNotAttended) captor.getValue()).habit()).isSameAs(neverAttended);
      assertThat(((HabitNotAttended) captor.getValue()).avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    @DisplayName("emits HabitNotAttended when next expected date is in the past")
    void emitsForOverdueHabit() {
      var overdue = overdueHabit();
      when(habitRepository.findAll()).thenReturn(List.of(overdue));
      when(historyRepository.findByHabitId(OVERDUE_HABIT_ID)).thenReturn(List.of());

      service.detectOverdueHabits();

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitNotAttended.class);
      assertThat(((HabitNotAttended) captor.getValue()).habit()).isSameAs(overdue);
      assertThat(((HabitNotAttended) captor.getValue()).avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    @DisplayName("does not emit event when habit is not overdue")
    void doesNotEmitForUpToDateHabit() {
      var upToDate = upToDateMonthlyHabit();
      when(habitRepository.findAll()).thenReturn(List.of(upToDate));

      service.detectOverdueHabits();

      verify(habitObserver, never()).notifyHabitEvent(any(HabitNotAttended.class));
    }

    @Test
    @DisplayName("does not append duplicate NOT_ATTENDED marker for same overdue slot")
    void deduplicatesNotAttendedHistory() {
      var overdue = overdueHabit();
      String expectedMarker =
          "expectedAt=" + overdue.getRecurrence().nextAfter(overdue.getLastAttendedDate());

      when(habitRepository.findAll()).thenReturn(List.of(overdue));
      when(historyRepository.findByHabitId(OVERDUE_HABIT_ID))
          .thenReturn(
              List.of(
                  new HabitHistoryEvent(
                      new HabitNotAttended(overdue, overdue.getAvatarId()),
                      LocalDateTime.now().minusMinutes(1),
                      expectedMarker)));

      service.detectOverdueHabits();

      verify(historyRepository, never())
          .append(argThat(e -> e.event() instanceof HabitNotAttended));
    }
  }

  @Nested
  @DisplayName("getHistory")
  class GetHistory {

    @Test
    @DisplayName("returns stored history for an existing habit")
    void returnsHistory() {
      var habit = hydrateHabit();
      var history =
          List.of(
              new HabitHistoryEvent(
                  new HabitAttended(habit, habit.getAvatarId()), LocalDateTime.now(), "attended"));
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));
      when(historyRepository.findByHabitId(HABIT_ID)).thenReturn(history);

      assertThat(service.getHistory(HABIT_ID)).isEqualTo(history);
    }
  }
}
