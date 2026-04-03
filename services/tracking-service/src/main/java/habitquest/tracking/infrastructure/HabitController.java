package habitquest.tracking.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

  private final HabitService habitService;
  private final HabitLogger log;

  public HabitController(HabitService habitService, HabitLogger log) {
    this.habitService = habitService;
    this.log = log;
  }

  private Id<Habit> idOfHabit(String id) {
    return new Id<>(id);
  }

  private Id<Avatar> idOfAvatar(String id) {
    return new Id<>(id);
  }

  // ─── Habit CRUD ─────────────────────────────────────────────────────────────

  @PostMapping
  public ResponseEntity<EntityModel<HabitCreatedResponse>> createHabit(
      @RequestBody CreateHabitRequest request) {
    log.info(request, "Creating habit");
    Habit created =
        switch (Objects.requireNonNull(request.recurrenceType()).toUpperCase(Locale.ITALIAN)) {
          case "DAILY" ->
              habitService.createDailyHabit(
                  idOfAvatar(request.avatarId()),
                  request.title(),
                  request.description(),
                  request.associatedQuestId());
          case "WEEKLY" ->
              habitService.createWeeklyHabit(
                  idOfAvatar(request.avatarId()),
                  request.title(),
                  request.description(),
                  request.dayOfWeek(),
                  request.associatedQuestId());
          case "MONTHLY" ->
              habitService.createMonthlyHabit(
                  idOfAvatar(request.avatarId()),
                  request.title(),
                  request.description(),
                  request.dayOfMonth(),
                  request.associatedQuestId());
          default ->
              throw new IllegalArgumentException(
                  "Unknown recurrence type: " + request.recurrenceType());
        };
    log.info(created, "Habit created");

    HabitCreatedResponse body = new HabitCreatedResponse(created.getId().value());
    EntityModel<HabitCreatedResponse> model =
        EntityModel.of(
            body,
            selfLink(created.getId().value()),
            linkTo(methodOn(HabitController.class).getHabit(created.getId().value()))
                .withRel("habit"),
            linkTo(methodOn(HabitController.class).getTags(created.getId().value()))
                .withRel("tags"),
            linkTo(methodOn(HabitController.class).getRecurrence(created.getId().value()))
                .withRel("recurrence"),
            linkTo(methodOn(HabitController.class).getHistory(created.getId().value()))
                .withRel("history"));

    return ResponseEntity.created(URI.create("/api/v1/habits/" + created.getId().value()))
        .body(model);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<HabitResponse>> getHabit(@PathVariable String id)
      throws HabitNotFoundException {
    Habit habit = habitService.getHabitById(idOfHabit(id));
    log.info(habit, "Fetched habit");

    EntityModel<HabitResponse> model =
        EntityModel.of(
            HabitMapper.toResponse(habit),
            selfLink(id),
            linkTo(methodOn(HabitController.class).getTitle(id)).withRel("title"),
            linkTo(methodOn(HabitController.class).getDescription(id)).withRel("description"),
            linkTo(methodOn(HabitController.class).getTags(id)).withRel("tags"),
            linkTo(methodOn(HabitController.class).getRecurrence(id)).withRel("recurrence"),
            linkTo(methodOn(HabitController.class).getLastAttendedDate(id))
                .withRel("lastAttendedDate"),
            linkTo(methodOn(HabitController.class).getHistory(id)).withRel("history"),
            linkTo(methodOn(HabitController.class).deleteHabit(id)).withRel("delete"));
    return ResponseEntity.ok(model);
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
    EntityModel<TitleResponse> model =
        EntityModel.of(new TitleResponse(title), selfLink(id), habitLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/description")
  public ResponseEntity<EntityModel<DescriptionResponse>> getDescription(@PathVariable String id)
      throws HabitNotFoundException {
    String description = habitService.getDescription(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit description");
    EntityModel<DescriptionResponse> model =
        EntityModel.of(new DescriptionResponse(description), selfLink(id), habitLink(id));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/tags")
  public ResponseEntity<EntityModel<TagsResponse>> getTags(@PathVariable String id)
      throws HabitNotFoundException {
    List<Tag> tags = habitService.getTags(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit tags");
    EntityModel<TagsResponse> model =
        EntityModel.of(
            new TagsResponse(tags.stream().map(Tag::name).toList()),
            selfLink(id),
            habitLink(id),
            linkTo(methodOn(HabitController.class).updateTags(id, null)).withRel("update"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/recurrence")
  public ResponseEntity<EntityModel<RecurrenceResponse>> getRecurrence(@PathVariable String id)
      throws HabitNotFoundException {
    RecurrenceResponse recurrence =
        HabitMapper.toRecurrenceResponse(habitService.getRecurrence(idOfHabit(id)));
    log.info(recurrence, "Fetched habit recurrence");
    EntityModel<RecurrenceResponse> model =
        EntityModel.of(
            recurrence,
            selfLink(id),
            habitLink(id),
            linkTo(methodOn(HabitController.class).updateRecurrence(id, null)).withRel("update"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/last-attended-date")
  public ResponseEntity<EntityModel<LastAttendedDateResponse>> getLastAttendedDate(
      @PathVariable String id) throws HabitNotFoundException {
    LocalDateTime date = habitService.getLastAttendedDate(idOfHabit(id));
    log.info(idOfHabit(id), "Fetched habit last attended date");
    EntityModel<LastAttendedDateResponse> model =
        EntityModel.of(
            new LastAttendedDateResponse(date),
            selfLink(id),
            habitLink(id),
            linkTo(methodOn(HabitController.class).attendHabit(id, null)).withRel("attend"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{id}/history")
  public ResponseEntity<EntityModel<HistoryResponse>> getHistory(@PathVariable String id)
      throws HabitNotFoundException {
    List<HabitHistoryEventResponse> history =
        habitService.getHistory(idOfHabit(id)).stream().map(HabitMapper::toResponse).toList();
    log.info(idOfHabit(id), "Fetched habit history");
    EntityModel<HistoryResponse> model =
        EntityModel.of(new HistoryResponse(history), selfLink(id), habitLink(id));
    return ResponseEntity.ok(model);
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

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────
  private Link selfLink(String id) {
    try {
      return linkTo(methodOn(HabitController.class).getHabit(id)).withSelfRel();
    } catch (HabitNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private Link habitLink(String id) {
    try {
      return linkTo(methodOn(HabitController.class).getHabit(id)).withRel("habit");
    } catch (HabitNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record CreateHabitRequest(
      String avatarId,
      String title,
      String description,
      String recurrenceType,
      DayOfWeek dayOfWeek,
      Integer dayOfMonth,
      String associatedQuestId) {}

  public record UpdateTitleRequest(String title) {}

  public record UpdateDescriptionRequest(String description) {}

  public record UpdateTagsRequest(List<String> tags) {}

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
