package habitquest.tracking.infrastructure.inbound;

import common.ddd.Id;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.in.HabitQueryService;
import habitquest.tracking.application.port.out.HabitLogger;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.infrastructure.dto.HabitMapper;
import habitquest.tracking.infrastructure.dto.HabitQueries.*;
import habitquest.tracking.infrastructure.dto.HabitResponseAssembler;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitQueryController {

  private final HabitQueryService habitQueryService;
  private final HabitResponseAssembler habitResponseAssembler;
  private final HabitLogger log;

  public HabitQueryController(
      HabitQueryService habitQueryService,
      HabitResponseAssembler habitResponseAssembler,
      HabitLogger log) {
    this.habitQueryService = habitQueryService;
    this.habitResponseAssembler = habitResponseAssembler;
    this.log = log;
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<HabitResponse>> getHabit(@PathVariable String id)
      throws HabitNotFoundException {
    Habit habit = habitQueryService.getHabitById(idOfHabit(id));
    log.info(habit, "Fetched habit");
    return ResponseEntity.ok(habitResponseAssembler.toModel(habit));
  }

  @GetMapping("/avatar/{avatarId}")
  public ResponseEntity<List<HabitResponse>> getHabitsByAvatar(@PathVariable String avatarId) {
    List<HabitResponse> habits =
        habitQueryService.getHabitsByAvatarId(idOfAvatar(avatarId)).stream()
            .map(HabitMapper::toResponse)
            .toList();
    log.info(habits, "Fetched habits for avatar " + avatarId);
    return ResponseEntity.ok(habits);
  }

  @GetMapping("/avatar/{avatarId}/history")
  public ResponseEntity<List<HabitHistoryEventResponse>> getHabitHistoryByAvatar(
      @PathVariable String avatarId) {
    List<HabitHistoryEventResponse> history =
        habitQueryService.getHistoryByAvatarId(idOfAvatar(avatarId)).stream()
            .map(HabitMapper::toResponse)
            .toList();
    log.info(history, "Fetched habit history for avatar " + avatarId);
    return ResponseEntity.ok(history);
  }

  @GetMapping("/{id}/title")
  public ResponseEntity<EntityModel<TitleResponse>> getTitle(@PathVariable String id)
      throws HabitNotFoundException {
    String title = habitQueryService.getTitle(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit title");
    return ResponseEntity.ok(habitResponseAssembler.toTitleModel(id, title));
  }

  @GetMapping("/{id}/description")
  public ResponseEntity<EntityModel<DescriptionResponse>> getDescription(@PathVariable String id)
      throws HabitNotFoundException {
    String description = habitQueryService.getDescription(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit description");
    return ResponseEntity.ok(habitResponseAssembler.toDescriptionModel(id, description));
  }

  @GetMapping("/{id}/tags")
  public ResponseEntity<EntityModel<TagsResponse>> getTags(@PathVariable String id)
      throws HabitNotFoundException {
    List<Tag> tags = habitQueryService.getTags(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit tags");
    return ResponseEntity.ok(habitResponseAssembler.toTagsModel(id, tags));
  }

  @GetMapping("/{id}/recurrence")
  public ResponseEntity<EntityModel<RecurrenceResponse>> getRecurrence(@PathVariable String id)
      throws HabitNotFoundException {
    RecurrenceResponse recurrence =
        HabitMapper.toRecurrenceResponse(habitQueryService.getRecurrence(idOfHabit(id)));
    log.info(recurrence, "Fetched habit recurrence");
    return ResponseEntity.ok(habitResponseAssembler.toRecurrenceModel(id, recurrence));
  }

  @GetMapping("/{id}/last-attended-date")
  public ResponseEntity<EntityModel<LastAttendedDateResponse>> getLastAttendedDate(
      @PathVariable String id) throws HabitNotFoundException {
    LocalDateTime date = habitQueryService.getLastAttendedDate(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit last attended date");
    return ResponseEntity.ok(habitResponseAssembler.toLastAttendedDateModel(id, date));
  }

  @GetMapping("/{id}/history")
  public ResponseEntity<EntityModel<HistoryResponse>> getHistory(@PathVariable String id)
      throws HabitNotFoundException {
    List<HabitHistoryEventResponse> history =
        habitQueryService.getHistory(idOfHabit(id)).stream().map(HabitMapper::toResponse).toList();
    log.info(idOfHabit(id), "Fetched habit history");
    return ResponseEntity.ok(habitResponseAssembler.toHistoryModel(id, history));
  }

  // ─── private helpers ─────────────────────────────────────────────────────────

  private Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
  }
}
