package habitquest.tracking.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import common.ddd.Id;
import habitquest.tracking.application.port.in.HabitCommandService;
import habitquest.tracking.application.port.out.AvatarClientPort;
import habitquest.tracking.application.port.out.HabitHistoryRepository;
import habitquest.tracking.application.port.out.HabitRepository;
import habitquest.tracking.application.port.out.QuestClientPort;
import habitquest.tracking.application.service.HabitCommandServiceImpl;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.events.HabitEvent;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.infrastructure.inbound.HabitCheckerJob;
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

  @Mock private HabitCommandService habitService;
  @Mock private HabitRepository habitRepository;
  @Mock private HabitHistoryRepository historyRepository;
  @Mock private HabitFactory habitFactory;
  @Mock private HabitObserver habitObserver;
  @Mock private AvatarClientPort avatarClient;
  @Mock private QuestClientPort questClient;

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
            new Id<>("habit-1"),
            new Id<>("avatar-1"),
            "Hydrate",
            "Drink water",
            new DailyRecurrence(),
            Optional.empty());
    when(habitRepository.findAll()).thenReturn(List.of(neverAttendedHabit));
    when(historyRepository.findByHabitId(new Id<>("habit-1"))).thenReturn(List.of());

    HabitCommandService realService =
        new HabitCommandServiceImpl(
            habitRepository,
            historyRepository,
            habitFactory,
            habitObserver,
            avatarClient,
            questClient);
    HabitCheckerJob realJob = new HabitCheckerJob(realService);

    realJob.run();

    ArgumentCaptor<HabitEvent> eventCaptor = ArgumentCaptor.forClass(HabitEvent.class);
    verify(habitObserver).notifyHabitEvent(eventCaptor.capture());
    HabitEvent event = eventCaptor.getValue();
    verify(habitRepository).findAll();
    verifyNoMoreInteractions(habitObserver);
    assertThat(event).isInstanceOf(HabitNotAttended.class);
    assertThat(((HabitNotAttended) event).habit().getId().value()).isEqualTo("habit-1");
  }
}
