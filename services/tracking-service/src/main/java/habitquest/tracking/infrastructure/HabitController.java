package habitquest.tracking.infrastructure;

import common.ddd.Id;
import habitquest.tracking.application.HabitLogger;
import habitquest.tracking.application.HabitNotFoundException;
import habitquest.tracking.application.HabitService;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.Recurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import habitquest.tracking.infrastructure.dto.HabitHistoryEventResponse;
import habitquest.tracking.infrastructure.dto.HabitMapper;
import habitquest.tracking.infrastructure.dto.HabitResponse;
import habitquest.tracking.infrastructure.dto.HabitResponseAssembler;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

  private final HabitService habitService;
  private final HabitResponseAssembler habitResponseAssembler;
  private final HabitLogger log;

  public HabitController(
      HabitService habitService, HabitResponseAssembler habitResponseAssembler, HabitLogger log) {
    this.habitService = habitService;
    this.habitResponseAssembler = habitResponseAssembler;
    this.log = log;
  }

  // ─── Habit CRUD ─────────────────────────────────────────────────────────────

  @PostMapping
  public ResponseEntity<EntityModel<HabitCreatedResponse>> createHabit(
      @RequestBody CreateHabitRequest request) {
    log.info(request, "Creating habit");

    Id<Avatar> avatarId = idOfAvatar(request.avatarId());
    String title = request.title();
    String description = request.description();
    String questId = request.associatedQuestId();
    String sourceId = request.sourceHabitId();

    Habit created =
        switch (Objects.requireNonNull(request.recurrenceType()).toUpperCase(Locale.ITALIAN)) {
          case "DAILY" ->
              habitService.createDailyHabit(avatarId, title, description, questId, sourceId);
          case "WEEKLY" ->
              habitService.createWeeklyHabit(
                  avatarId, title, description, request.dayOfWeek(), questId, sourceId);
          case "MONTHLY" ->
              habitService.createMonthlyHabit(
                  avatarId, title, description, request.dayOfMonth(), questId, sourceId);
          default ->
              throw new IllegalArgumentException(
                  "Unknown recurrence type: " + request.recurrenceType());
        };


    List<Tag> tags =
        request.tags() == null
            ? List.of()
            : request.tags().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(Tag::new)
                .toList();
    if (!tags.isEmpty()) {
      try {
        created = habitService.updateTags(created.getId(), tags);
      } catch (HabitNotFoundException ex) {
        throw new IllegalStateException("Created habit not found while applying tags", ex);
      }
    }

    log.info(created, "Habit created");

    return ResponseEntity.created(URI.create("/api/v1/habits/" + created.getId().value()))
        .body(habitResponseAssembler.toCreatedModel(created));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<HabitResponse>> getHabit(@PathVariable String id)
      throws HabitNotFoundException {
    Habit habit = habitService.getHabitById(idOfHabit(id));
    log.info(habit, "Fetched habit");
    return ResponseEntity.ok(habitResponseAssembler.toModel(habit));
  }

  @GetMapping("/avatar/{avatarId}")
  public ResponseEntity<List<HabitResponse>> getHabitsByAvatar(@PathVariable String avatarId) {
    List<HabitResponse> habits =
        habitService.getHabitsByAvatarId(idOfAvatar(avatarId)).stream()
            .map(HabitMapper::toResponse)
            .toList();
    log.info(habits, "Fetched habits for avatar " + avatarId);
    return ResponseEntity.ok(habits);
  }

  @GetMapping("/avatar/{avatarId}/history")
  public ResponseEntity<List<HabitHistoryEventResponse>> getHabitHistoryByAvatar(
      @PathVariable String avatarId) {
    List<HabitHistoryEventResponse> history =
        habitService.getHistoryByAvatarId(idOfAvatar(avatarId)).stream()
            .map(HabitMapper::toResponse)
            .toList();
    log.info(history, "Fetched habit history for avatar " + avatarId);
    return ResponseEntity.ok(history);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHabit(@PathVariable String id) throws HabitNotFoundException {
    log.info(idOfHabit(id), "Deleting habit");
    habitService.deleteHabitById(idOfHabit(id));
    return ResponseEntity.noContent().build();
  }

  // ─── Queries ────────────────────────────────────────────────────────────────

  @GetMapping("/{id}/title")
  public ResponseEntity<EntityModel<TitleResponse>> getTitle(@PathVariable String id)
      throws HabitNotFoundException {
    String title = habitService.getTitle(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit title");
    return ResponseEntity.ok(habitResponseAssembler.toTitleModel(id, title));
  }

  @GetMapping("/{id}/description")
  public ResponseEntity<EntityModel<DescriptionResponse>> getDescription(@PathVariable String id)
      throws HabitNotFoundException {
    String description = habitService.getDescription(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit description");
    return ResponseEntity.ok(habitResponseAssembler.toDescriptionModel(id, description));
  }

  @GetMapping("/{id}/tags")
  public ResponseEntity<EntityModel<TagsResponse>> getTags(@PathVariable String id)
      throws HabitNotFoundException {
    List<Tag> tags = habitService.getTags(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit tags");
    return ResponseEntity.ok(habitResponseAssembler.toTagsModel(id, tags));
  }

  @GetMapping("/{id}/recurrence")
  public ResponseEntity<EntityModel<RecurrenceResponse>> getRecurrence(@PathVariable String id)
      throws HabitNotFoundException {
    RecurrenceResponse recurrence =
        HabitMapper.toRecurrenceResponse(habitService.getRecurrence(idOfHabit(id)));
    log.info(recurrence, "Fetched habit recurrence");
    return ResponseEntity.ok(habitResponseAssembler.toRecurrenceModel(id, recurrence));
  }

  @GetMapping("/{id}/last-attended-date")
  public ResponseEntity<EntityModel<LastAttendedDateResponse>> getLastAttendedDate(
      @PathVariable String id) throws HabitNotFoundException {
    LocalDateTime date = habitService.getLastAttendedDate(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit last attended date");
    return ResponseEntity.ok(habitResponseAssembler.toLastAttendedDateModel(id, date));
  }

  @GetMapping("/{id}/history")
  public ResponseEntity<EntityModel<HistoryResponse>> getHistory(@PathVariable String id)
      throws HabitNotFoundException {
    List<HabitHistoryEventResponse> history =
        habitService.getHistory(idOfHabit(id)).stream().map(HabitMapper::toResponse).toList();
    log.info(idOfHabit(id), "Fetched habit history");
    return ResponseEntity.ok(habitResponseAssembler.toHistoryModel(id, history));
  }

  // ─── Updaters ───────────────────────────────────────────────────────────────

  @PatchMapping("/{id}/title")
  public ResponseEntity<Void> updateTitle(
      @PathVariable String id, @RequestBody UpdateTitleRequest request)
      throws HabitNotFoundException {
    log.info(request, "Updating title for habit " + id);
    habitService.updateTitle(idOfHabit(id), request.title());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/description")
  public ResponseEntity<Void> updateDescription(
      @PathVariable String id, @RequestBody UpdateDescriptionRequest request)
      throws HabitNotFoundException {
    log.info(request, "Updating description for habit " + id);
    habitService.updateDescription(idOfHabit(id), request.description());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/tags")
  public ResponseEntity<Void> updateTags(
      @PathVariable String id, @RequestBody UpdateTagsRequest request)
      throws HabitNotFoundException {
    log.info(request, "Updating tags for habit " + id);
    List<Tag> tags = request.tags().stream().map(Tag::new).toList();
    habitService.updateTags(idOfHabit(id), tags);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/recurrence")
  public ResponseEntity<Void> updateRecurrence(
      @PathVariable String id, @RequestBody UpdateRecurrenceRequest request)
      throws HabitNotFoundException {
    log.info(request, "Updating recurrence for habit " + id);
    habitService.updateRecurrence(idOfHabit(id), request.toRecurrence());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/attend")
  public ResponseEntity<Void> attendHabit(
      @PathVariable String id, @RequestBody AttendRequest request) throws HabitNotFoundException {
    log.info(request, "Attending habit " + id);
    habitService.attendHabit(idOfHabit(id), request.date());
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ──────────────────────────────────────────────────────

  @ExceptionHandler(HabitNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleHabitNotFound(HabitNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    log.error(ex, "Domain error", ex);
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler({AvatarCommunicationException.class})
  public ResponseEntity<ErrorResponse> handleAvatarException(RuntimeException ex) {
    log.error(ex, "Domain error", ex);
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler({QuestCommunicationException.class})
  public ResponseEntity<ErrorResponse> handleQuestException(RuntimeException ex) {
    log.error(ex, "Quest service communication error", ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(ex.getMessage()));
  }

  // ─── private helpers ────────────────────────────────────────────────────────

  private Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record CreateHabitRequest(
      String avatarId,
      String title,
      String description,
      String recurrenceType,
      DayOfWeek dayOfWeek,
      Integer dayOfMonth,
      List<String> tags,
      String associatedQuestId,
      String sourceHabitId) {}

  public record UpdateTitleRequest(String title) {}

  public record UpdateDescriptionRequest(String description) {}

  public record UpdateTagsRequest(List<String> tags) {
    public UpdateTagsRequest {
      tags = tags != null ? List.copyOf(tags) : List.of();
    }
  }

  public record UpdateRecurrenceRequest(String type, DayOfWeek dayOfWeek, Integer dayOfMonth) {
    public Recurrence toRecurrence() {
      return switch (type.toUpperCase(Locale.ITALIAN)) {
        case "DAILY" -> new DailyRecurrence();
        case "WEEKLY" -> new WeeklyRecurrence(dayOfWeek);
        case "MONTHLY" -> new MonthlyRecurrence(dayOfMonth);
        default -> throw new IllegalArgumentException("Unknown recurrence type: " + type);
      };
    }
  }

  public record AttendRequest(LocalDateTime date) {}

  public record HabitCreatedResponse(String id) {}

  public record TitleResponse(String title) {}

  public record DescriptionResponse(String description) {}

  public record TagsResponse(List<String> tags) {}

  public record HistoryResponse(List<HabitHistoryEventResponse> history) {}

  public record LastAttendedDateResponse(LocalDateTime date) {}

  public record ErrorResponse(String message) {}

  public record RecurrenceResponse(String type, Integer dayOfMonth, String dayOfWeek) {}
}
