package habitquest.tracking.infrastructure.inbound;

import habitquest.tracking.application.port.in.HabitCommandService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HabitCheckerJob {
  private final HabitCommandService service;

  public HabitCheckerJob(HabitCommandService service) {
    this.service = service;
  }

  @Scheduled(cron = "${tracking.overdue.cron:0 * * * * *}") // every minute
  public void run() {
    service.detectOverdueHabits();
  }
}
