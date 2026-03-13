package habitquest.tracking.infrastructure;

import habitquest.tracking.application.HabitService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HabitCheckerJob {
  private final HabitService service;

  public HabitCheckerJob(HabitService service) {
    this.service = service;
  }

  @Scheduled(cron = "${tracking.overdue.cron:0 * * * * *}") // every minute
  public void run() {
    service.detectOverdueHabits();
  }
}
