package habitquest.tracking.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.infrastructure.dto.HabitResponsesDto.*;
import habitquest.tracking.infrastructure.inbound.HabitController;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class HabitResponseAssembler {

  public EntityModel<HabitCreatedResponse> toCreatedModel(Habit habit) {
    String id = habit.getId().value();
    return EntityModel.of(
        new HabitCreatedResponse(id),
        selfLink(id),
        linkTo(methodOn(HabitController.class).getHabit(id)).withRel("habit"),
        linkTo(methodOn(HabitController.class).getTags(id)).withRel("tags"),
        linkTo(methodOn(HabitController.class).getRecurrence(id)).withRel("recurrence"),
        linkTo(methodOn(HabitController.class).getHistory(id)).withRel("history"));
  }

  public EntityModel<HabitResponse> toModel(Habit habit) {
    String id = habit.getId().value();
    return EntityModel.of(
        HabitMapper.toResponse(habit),
        selfLink(id),
        linkTo(methodOn(HabitController.class).getTitle(id)).withRel("title"),
        linkTo(methodOn(HabitController.class).getDescription(id)).withRel("description"),
        linkTo(methodOn(HabitController.class).getTags(id)).withRel("tags"),
        linkTo(methodOn(HabitController.class).getRecurrence(id)).withRel("recurrence"),
        linkTo(methodOn(HabitController.class).getLastAttendedDate(id)).withRel("lastAttendedDate"),
        linkTo(methodOn(HabitController.class).getHistory(id)).withRel("history"),
        linkTo(methodOn(HabitController.class).deleteHabit(id)).withRel("delete"));
  }

  public EntityModel<TitleResponse> toTitleModel(String id, String title) {
    return EntityModel.of(new TitleResponse(title), selfLink(id), habitLink(id));
  }

  public EntityModel<DescriptionResponse> toDescriptionModel(String id, String description) {
    return EntityModel.of(new DescriptionResponse(description), selfLink(id), habitLink(id));
  }

  public EntityModel<TagsResponse> toTagsModel(String id, List<Tag> tags) {
    return EntityModel.of(
        new TagsResponse(tags.stream().map(Tag::name).toList()),
        selfLink(id),
        habitLink(id),
        linkTo(methodOn(HabitController.class).updateTags(id, null)).withRel("update"));
  }

  public EntityModel<RecurrenceResponse> toRecurrenceModel(
      String id, RecurrenceResponse recurrence) {
    return EntityModel.of(
        recurrence,
        selfLink(id),
        habitLink(id),
        linkTo(methodOn(HabitController.class).updateRecurrence(id, null)).withRel("update"));
  }

  public EntityModel<LastAttendedDateResponse> toLastAttendedDateModel(
      String id, LocalDateTime date) {
    return EntityModel.of(
        new LastAttendedDateResponse(date),
        selfLink(id),
        habitLink(id),
        linkTo(methodOn(HabitController.class).attendHabit(id, null)).withRel("attend"));
  }

  public EntityModel<HistoryResponse> toHistoryModel(
      String id, List<HabitHistoryEventResponse> history) {
    return EntityModel.of(new HistoryResponse(history), selfLink(id), habitLink(id));
  }

  // ─── private helpers ────────────────────────────────────────────────────────

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
}
