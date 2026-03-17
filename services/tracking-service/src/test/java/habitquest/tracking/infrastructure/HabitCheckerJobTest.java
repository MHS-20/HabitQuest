package habitquest.tracking.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import habitquest.tracking.application.HabitRepository;
import habitquest.tracking.application.HabitService;
import habitquest.tracking.application.HabitServiceImpl;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.events.HabitEvent;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HabitCheckerJob")
class HabitCheckerJobTest {

  @Mock private HabitService habitService;
  @Mock private HabitRepository habitRepository;
  @Mock private HabitFactory habitFactory;
  @Mock private HabitObserver habitObserver;

  private HabitCheckerJob job;

  @BeforeEach
  void setUp() {
    job = new HabitCheckerJob(habitService);
  }

  @Test
  @DisplayName("run delegates overdue detection to HabitService")
  void runDelegatesToHabitService() {
    job.run();

    verify(habitService, times(1)).detectOverdueHabits();
    verifyNoMoreInteractions(habitService);
  }

  @Test
  @DisplayName("run emits HabitNotAttended for a never-attended habit")
  void runEmitsNotAttendedForNeverAttendedHabit() {
    Habit neverAttendedHabit =
        new Habit(
            "habit-1",
            "avatar-1",
            "Hydrate",
            "Drink water",
            new DailyRecurrence(),
            Optional.empty());
    when(habitRepository.findAll()).thenReturn(List.of(neverAttendedHabit));

    HabitService realService = new HabitServiceImpl(habitRepository, habitFactory, habitObserver);
    HabitCheckerJob realJob = new HabitCheckerJob(realService);

    realJob.run();

    ArgumentCaptor<HabitEvent> eventCaptor = ArgumentCaptor.forClass(HabitEvent.class);
    verify(habitObserver).notifyHabitEvent(eventCaptor.capture());
    HabitEvent event = eventCaptor.getValue();
    verify(habitRepository).findAll();
    verifyNoMoreInteractions(habitObserver);
    assertThat(event).isInstanceOf(HabitNotAttended.class);
    assertThat(((HabitNotAttended) event).habit().getId()).isEqualTo("habit-1");
  }
}
