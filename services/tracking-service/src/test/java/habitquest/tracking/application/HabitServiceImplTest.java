package habitquest.tracking.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitEvent;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.Recurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
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

  private static final String HABIT_ID = "habit-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String TITLE = "Hydrate";
  private static final String DESCRIPTION = "Drink 2L of water";
  private static final String UNKNOWN_ID = "missing";

  @Mock private HabitRepository habitRepository;
  @Mock private HabitFactory habitFactory;
  @Mock private HabitObserver habitObserver;

  private HabitServiceImpl service;

  private Habit stubHabit() {
    Habit habit =
        new Habit(HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.empty());
    habit.setTitle(TITLE);
    habit.setDescription(DESCRIPTION);
    habit.setTags(List.of(new Tag("health"), new Tag("wellness")));
    habit.setRecurrence(new DailyRecurrence());
    habit.attendHabit(LocalDateTime.of(2026, 3, 16, 10, 0));
    return habit;
  }

  @BeforeEach
  void setUp() {
    service = new HabitServiceImpl(habitRepository, habitFactory, habitObserver);
  }

  @Nested
  @DisplayName("creation methods")
  class CreateHabit {

    @Test
    @DisplayName("createDailyHabit delegates to factory, saves, and returns habit")
    void createsDaily() {
      Habit habit = stubHabit();
      when(habitFactory.createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION)).thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      Habit created = service.createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION);
      verify(habitRepository).save(habit);
    }

    @Test
    @DisplayName("createWeeklyHabit delegates to factory, saves, and returns habit")
    void createsWeekly() {
      Habit habit = stubHabit();
      when(habitFactory.createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DayOfWeek.MONDAY))
          .thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      Habit created = service.createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DayOfWeek.MONDAY);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DayOfWeek.MONDAY);
      verify(habitRepository).save(habit);
    }

    @Test
    @DisplayName("createMonthlyHabit delegates to factory, saves, and returns habit")
    void createsMonthly() {
      Habit habit = stubHabit();
      when(habitFactory.createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, 15)).thenReturn(habit);
      when(habitRepository.save(habit)).thenReturn(habit);

      Habit created = service.createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, 15);

      assertThat(created).isSameAs(habit);
      verify(habitFactory).createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, 15);
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("getHabitById")
  class GetHabitById {

    @Test
    @DisplayName("returns the habit when found")
    void found() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getHabitById(HABIT_ID)).isSameAs(habit);
    }

    @Test
    @DisplayName("throws HabitNotFoundException when id is unknown")
    void notFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getHabitById(UNKNOWN_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID);
    }
  }

  @Nested
  @DisplayName("deleteHabitById")
  class DeleteHabitById {

    @Test
    @DisplayName("deletes and emits HabitDeleted event")
    void deletesAndEmitsEvent() {
      service.deleteHabitById(HABIT_ID);

      verify(habitRepository).deleteById(HABIT_ID);
      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitDeleted.class);
      assertThat(((HabitDeleted) captor.getValue()).habitId()).isEqualTo(HABIT_ID);
    }
  }

  @Nested
  @DisplayName("query delegates")
  class QueryDelegates {

    @Test
    @DisplayName("getTitle delegates to habit.getTitle()")
    void getTitle() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getTitle(HABIT_ID)).isEqualTo(TITLE);
    }

    @Test
    @DisplayName("getDescription delegates to habit.getDescription()")
    void getDescription() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getDescription(HABIT_ID)).isEqualTo(DESCRIPTION);
    }

    @Test
    @DisplayName("getTags delegates to habit.getTags()")
    void getTags() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getTags(HABIT_ID)).containsExactly(new Tag("health"), new Tag("wellness"));
    }

    @Test
    @DisplayName("getRecurrence delegates to habit.getRecurrence()")
    void getRecurrence() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getRecurrence(HABIT_ID)).isEqualTo(new DailyRecurrence());
    }

    @Test
    @DisplayName("getLastAttendedDate delegates to habit.getLastAttendedDate()")
    void getLastAttendedDate() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      assertThat(service.getLastAttendedDate(HABIT_ID))
          .isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 0));
    }

    @Test
    @DisplayName("throws HabitNotFoundException when querying unknown habit")
    void queryNotFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getTitle(UNKNOWN_ID))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID);
    }
  }

  @Nested
  @DisplayName("updateTitle")
  class UpdateTitle {

    @Test
    @DisplayName("updates title and saves")
    void updates() {
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      Habit updated = service.updateTitle(HABIT_ID, "Read");

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
      Habit habit = stubHabit();
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      Habit updated = service.updateDescription(HABIT_ID, "Read 20 pages");

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
      Habit habit = stubHabit();
      List<Tag> tags = List.of(new Tag("mindset"));
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      Habit updated = service.updateTags(HABIT_ID, tags);

      assertThat(updated.getTags()).containsExactly(new Tag("mindset"));
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("updateRecurrence")
  class UpdateRecurrence {

    @Test
    @DisplayName("updates recurrence and saves")
    void updates() {
      Habit habit = stubHabit();
      Recurrence recurrence = new WeeklyRecurrence(DayOfWeek.MONDAY);
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      Habit updated = service.updateRecurrence(HABIT_ID, recurrence);

      assertThat(updated.getRecurrence()).isEqualTo(new WeeklyRecurrence(DayOfWeek.MONDAY));
      verify(habitRepository).save(habit);
    }
  }

  @Nested
  @DisplayName("attendHabit")
  class AttendHabit {

    @Test
    @DisplayName("updates last attended date, saves, and emits HabitAttended")
    void attendsAndEmitsEvent() {
      Habit habit = stubHabit();
      LocalDateTime attendedAt = LocalDateTime.of(2026, 3, 17, 9, 30);
      when(habitRepository.findById(HABIT_ID)).thenReturn(Optional.of(habit));

      Habit attended = service.attendHabit(HABIT_ID, attendedAt);

      assertThat(attended.getLastAttendedDate()).isEqualTo(attendedAt);
      verify(habitRepository).save(habit);

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitAttended.class);
      assertThat(((HabitAttended) captor.getValue()).habit()).isSameAs(habit);
    }

    @Test
    @DisplayName("throws HabitNotFoundException for unknown habit")
    void notFound() {
      when(habitRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.attendHabit(UNKNOWN_ID, LocalDateTime.now()))
          .isInstanceOf(HabitNotFoundException.class)
          .hasMessage(UNKNOWN_ID);
    }
  }

  @Nested
  @DisplayName("detectOverdueHabits")
  class DetectOverdueHabits {

    @Test
    @DisplayName("emits HabitNotAttended when last attended is null")
    void emitsForNeverAttended() {
      Habit neverAttended =
          new Habit(
              "habit-null", AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.empty());
      neverAttended.setTitle(TITLE);
      neverAttended.setDescription(DESCRIPTION);
      neverAttended.setRecurrence(new DailyRecurrence());

      when(habitRepository.findAll()).thenReturn(List.of(neverAttended));

      service.detectOverdueHabits();

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitNotAttended.class);
      assertThat(((HabitNotAttended) captor.getValue()).habit()).isSameAs(neverAttended);
    }

    @Test
    @DisplayName("emits HabitNotAttended when next expected date is in the past")
    void emitsForOverdueHabit() {
      Habit overdue =
          new Habit(
              "habit-overdue",
              AVATAR_ID,
              TITLE,
              DESCRIPTION,
              new DailyRecurrence(),
              Optional.empty());
      overdue.setTitle(TITLE);
      overdue.setDescription(DESCRIPTION);
      overdue.setRecurrence(new DailyRecurrence());
      overdue.attendHabit(LocalDateTime.now().minusDays(2));

      when(habitRepository.findAll()).thenReturn(List.of(overdue));

      service.detectOverdueHabits();

      ArgumentCaptor<HabitEvent> captor = ArgumentCaptor.forClass(HabitEvent.class);
      verify(habitObserver).notifyHabitEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(HabitNotAttended.class);
      assertThat(((HabitNotAttended) captor.getValue()).habit()).isSameAs(overdue);
    }

    @Test
    @DisplayName("does not emit event when habit is not overdue")
    void doesNotEmitForUpToDateHabit() {
      Habit upToDate =
          new Habit(
              "habit-ok",
              AVATAR_ID,
              TITLE,
              DESCRIPTION,
              new MonthlyRecurrence(20),
              Optional.empty());
      upToDate.setTitle(TITLE);
      upToDate.setDescription(DESCRIPTION);
      upToDate.setRecurrence(new MonthlyRecurrence(20));
      upToDate.attendHabit(LocalDateTime.now());

      when(habitRepository.findAll()).thenReturn(List.of(upToDate));

      service.detectOverdueHabits();

      verify(habitObserver, never()).notifyHabitEvent(any(HabitNotAttended.class));
    }
  }
}
