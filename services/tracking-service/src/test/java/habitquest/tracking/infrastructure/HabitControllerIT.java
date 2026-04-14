package habitquest.tracking.infrastructure;

import static habitquest.tracking.HabitFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.in.HabitService;
import habitquest.tracking.application.port.out.HabitLogger;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import habitquest.tracking.infrastructure.dto.HabitMapper;
import habitquest.tracking.infrastructure.dto.HabitResponseAssembler;
import habitquest.tracking.infrastructure.dto.HabitResponsesDto.*;
import habitquest.tracking.infrastructure.exceptions.QuestCommunicationException;
import habitquest.tracking.infrastructure.inbound.HabitController;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HabitController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("HabitController")
public class HabitControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private HabitService habitService;
  @MockitoBean private HabitResponseAssembler habitResponseAssembler;
  @MockitoBean private HabitLogger log;

  @Nested
  @DisplayName("POST /api/v1/habits")
  class CreateHabit {

    @Test
    @DisplayName("returns 201 with the new habit id")
    void shouldReturn201WithId() throws Exception {
      when(habitService.createHabit(any(), anyString(), anyString(), any(), any(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"DAILY"
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION)))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/habits/" + HABIT_ID.value()))
          .andExpect(jsonPath("$.id").value(HABIT_ID.value()));
    }

    @Test
    @DisplayName("delegates DAILY payload to service with DailyRecurrence")
    void shouldDelegateDailyPayloadToService() throws Exception {
      when(habitService.createHabit(any(), anyString(), anyString(), any(), any(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"DAILY"
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION)))
          .andExpect(status().isCreated());

      verify(habitService)
          .createHabit(
              eq(AVATAR_ID), eq(TITLE), eq(DESCRIPTION), eq(DAILY_RECURRENCE), isNull(), isNull());
    }

    @Test
    @DisplayName("delegates WEEKLY payload to service with WeeklyRecurrence")
    void shouldDelegateWeeklyPayloadToService() throws Exception {
      when(habitService.createHabit(any(), anyString(), anyString(), any(), any(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"WEEKLY",
                                        "dayOfWeek":"MONDAY"
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION)))
          .andExpect(status().isCreated());

      verify(habitService)
          .createHabit(
              eq(AVATAR_ID), eq(TITLE), eq(DESCRIPTION), eq(WEEKLY_RECURRENCE), isNull(), isNull());
    }

    @Test
    @DisplayName("delegates MONTHLY payload to service with MonthlyRecurrence")
    void shouldDelegateMonthlyPayloadToService() throws Exception {
      when(habitService.createHabit(any(), anyString(), anyString(), any(), any(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"MONTHLY",
                                        "dayOfMonth":%d
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION, DEFAULT_DAY_OF_MONTH)))
          .andExpect(status().isCreated());

      verify(habitService)
          .createHabit(
              eq(AVATAR_ID),
              eq(TITLE),
              eq(DESCRIPTION),
              eq(MONTHLY_RECURRENCE),
              isNull(),
              isNull());
    }

    @Test
    @DisplayName("returns 400 when recurrence type is unknown")
    void shouldReturn400WhenRecurrenceTypeIsUnknown() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"YEARLY"
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Unknown recurrence type: YEARLY"));

      verify(habitService, never()).createHabit(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("applies tags when provided during creation")
    void shouldApplyTagsWhenProvided() throws Exception {
      when(habitService.createDailyHabit(any(), anyString(), anyString(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitService.updateTags(eq(HABIT_ID), anyList())).thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitController.HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"DAILY",
                                        "tags":["  %s  ","", "%s"]
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION, TAG_HEALTH, TAG_MINDSET)))
          .andExpect(status().isCreated());

      verify(habitService)
          .updateTags(
              eq(HABIT_ID),
              argThat(tags -> tags.equals(List.of(new Tag(TAG_HEALTH), new Tag(TAG_MINDSET)))));
    }

    @Test
    @DisplayName("does not apply tags when request has no tags")
    void shouldNotApplyTagsWhenMissing() throws Exception {
      when(habitService.createDailyHabit(any(), anyString(), anyString(), any()))
          .thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toCreatedModel(any(Habit.class)))
          .thenReturn(EntityModel.of(new HabitController.HabitCreatedResponse(HABIT_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId":"%s",
                                        "title":"%s",
                                        "description":"%s",
                                        "recurrenceType":"DAILY"
                                      }
                                      """
                          .formatted(AVATAR_1, TITLE, DESCRIPTION)))
          .andExpect(status().isCreated());

      verify(habitService, never()).updateTags(any(), anyList());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}")
  class GetHabit {

    @Test
    @DisplayName("returns 200 with habit data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(habitService.getHabitById(HABIT_ID)).thenReturn(hydrateHabitWithQuest());
      when(habitResponseAssembler.toModel(any(Habit.class)))
          .thenReturn(EntityModel.of(HabitMapper.toResponse(hydrateHabitWithQuest())));

      mockMvc
          .perform(get("/api/v1/habits/{id}", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(HABIT_ID.value()))
          .andExpect(jsonPath("$.avatarId").value(AVATAR_ID.value()))
          .andExpect(jsonPath("$.title").value(TITLE))
          .andExpect(jsonPath("$.description").value(DESCRIPTION))
          .andExpect(jsonPath("$.tags[0]").value(TAG_HEALTH))
          .andExpect(jsonPath("$.recurrence.type").value("DAILY"))
          .andExpect(jsonPath("$.associatedQuestId").value(QUEST_1));
    }

    @Test
    @DisplayName("returns 404 when habit does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(habitService.getHabitById(GHOST_ID))
          .thenThrow(new HabitNotFoundException(GHOST_ID.value()));

      mockMvc
          .perform(get("/api/v1/habits/{id}", GHOST_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/avatar/{avatarId}")
  class GetHabitsByAvatar {

    @Test
    @DisplayName("returns 200 with the avatar habits list")
    void shouldReturnHabitsForAvatar() throws Exception {
      when(habitService.getHabitsByAvatarId(AVATAR_ID))
          .thenReturn(List.of(hydrateHabitWithQuest(), hydrateHabit()));

      mockMvc
          .perform(get("/api/v1/habits/avatar/{avatarId}", AVATAR_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].avatarId").value(AVATAR_ID.value()))
          .andExpect(jsonPath("$[0].title").value(TITLE))
          .andExpect(jsonPath("$[1].avatarId").value(AVATAR_ID.value()));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/avatar/{avatarId}/history")
  class GetHistoryByAvatar {

    @Test
    @DisplayName("returns 200 with merged avatar history")
    void shouldReturnHistoryForAvatar() throws Exception {
      var habit = hydrateHabitWithQuest();
      when(habitService.getHistoryByAvatarId(AVATAR_ID))
          .thenReturn(
              List.of(
                  new HabitHistoryEvent(
                      new HabitAttended(habit, AVATAR_ID),
                      NEXT_ATTENDED_AT,
                      "attendedAt=" + NEXT_ATTENDED_AT)));

      mockMvc
          .perform(get("/api/v1/habits/avatar/{avatarId}/history", AVATAR_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[0].eventType").value("HabitAttended"))
          .andExpect(jsonPath("$[0].habitId").value(HABIT_ID.value()))
          .andExpect(jsonPath("$[0].avatarId").value(AVATAR_ID.value()))
          .andExpect(jsonPath("$[0].occurredAt").value("2026-03-17T09:30:00"));
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/habits/{id}")
  class DeleteHabit {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(habitService).deleteHabitById(HABIT_ID);

      mockMvc
          .perform(delete("/api/v1/habits/{id}", HABIT_ID.value()))
          .andExpect(status().isNoContent());

      verify(habitService).deleteHabitById(HABIT_ID);
    }

    @Test
    @DisplayName("returns 404 when habit does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new HabitNotFoundException(GHOST_ID.value()))
          .when(habitService)
          .deleteHabitById(GHOST_ID);

      mockMvc
          .perform(delete("/api/v1/habits/{id}", GHOST_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/title")
  class GetTitle {

    @Test
    @DisplayName("returns 200 with habit title")
    void shouldReturnTitle() throws Exception {
      when(habitService.getTitle(HABIT_ID)).thenReturn(TITLE);
      when(habitResponseAssembler.toTitleModel(eq(HABIT_ID.value()), eq(TITLE)))
          .thenReturn(EntityModel.of(new TitleResponse(TITLE)));

      mockMvc
          .perform(get("/api/v1/habits/{id}/title", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value(TITLE));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/description")
  class GetDescription {

    @Test
    @DisplayName("returns 200 with habit description")
    void shouldReturnDescription() throws Exception {
      when(habitService.getDescription(HABIT_ID)).thenReturn(DESCRIPTION);
      when(habitResponseAssembler.toDescriptionModel(eq(HABIT_ID.value()), eq(DESCRIPTION)))
          .thenReturn(EntityModel.of(new DescriptionResponse(DESCRIPTION)));

      mockMvc
          .perform(get("/api/v1/habits/{id}/description", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.description").value(DESCRIPTION));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/tags")
  class GetTags {

    @Test
    @DisplayName("returns 200 with habit tags")
    void shouldReturnTags() throws Exception {
      List<Tag> tags = List.of(new Tag(TAG_HEALTH), new Tag("fitness"));
      when(habitService.getTags(HABIT_ID)).thenReturn(tags);
      when(habitResponseAssembler.toTagsModel(eq(HABIT_ID.value()), eq(tags)))
          .thenReturn(EntityModel.of(new TagsResponse(List.of(TAG_HEALTH, "fitness"))));

      mockMvc
          .perform(get("/api/v1/habits/{id}/tags", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.tags[0]").value(TAG_HEALTH))
          .andExpect(jsonPath("$.tags[1]").value("fitness"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/recurrence")
  class GetRecurrence {

    @Test
    @DisplayName("returns 200 with recurrence payload")
    void shouldReturnRecurrence() throws Exception {
      when(habitService.getRecurrence(HABIT_ID)).thenReturn(WEEKLY_RECURRENCE);
      when(habitResponseAssembler.toRecurrenceModel(
              eq(HABIT_ID.value()), any(RecurrenceResponse.class)))
          .thenReturn(
              EntityModel.of(new RecurrenceResponse("WEEKLY", null, DEFAULT_DAY_OF_WEEK.name())));

      mockMvc
          .perform(get("/api/v1/habits/{id}/recurrence", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.type").value("WEEKLY"))
          .andExpect(jsonPath("$.dayOfWeek").value(DEFAULT_DAY_OF_WEEK.name()));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/last-attended-date")
  class GetLastAttendedDate {

    @Test
    @DisplayName("returns 200 with last attended date")
    void shouldReturnLastAttendedDate() throws Exception {
      when(habitService.getLastAttendedDate(HABIT_ID)).thenReturn(ATTENDED_AT);
      when(habitResponseAssembler.toLastAttendedDateModel(eq(HABIT_ID.value()), eq(ATTENDED_AT)))
          .thenReturn(EntityModel.of(new LastAttendedDateResponse(ATTENDED_AT)));

      mockMvc
          .perform(get("/api/v1/habits/{id}/last-attended-date", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.date").value("2026-03-16T10:00:00"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/history")
  class GetHistory {

    @Test
    @DisplayName("returns 200 with habit event history")
    void shouldReturnHistory() throws Exception {
      var habit = hydrateHabitWithQuest();
      var event =
          new HabitHistoryEvent(
              new HabitAttended(habit, AVATAR_ID),
              NEXT_ATTENDED_AT,
              "attendedAt=" + NEXT_ATTENDED_AT);
      when(habitService.getHistory(HABIT_ID)).thenReturn(List.of(event));

      HabitHistoryEventResponse eventResponse = HabitMapper.toResponse(event);
      when(habitResponseAssembler.toHistoryModel(eq(HABIT_ID.value()), anyList()))
          .thenReturn(EntityModel.of(new HistoryResponse(List.of(eventResponse))));

      mockMvc
          .perform(get("/api/v1/habits/{id}/history", HABIT_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.history[0].eventType").value("HabitAttended"))
          .andExpect(jsonPath("$.history[0].habitId").value(HABIT_ID.value()))
          .andExpect(jsonPath("$.history[0].avatarId").value(AVATAR_ID.value()))
          .andExpect(jsonPath("$.history[0].occurredAt").value("2026-03-17T09:30:00"))
          .andExpect(jsonPath("$.history[0].details").value("attendedAt=2026-03-17T09:30"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/title")
  class UpdateTitle {

    @Test
    @DisplayName("returns 204 and delegates title to service")
    void shouldReturn204AndDelegateTitle() throws Exception {
      when(habitService.updateTitle(eq(HABIT_ID), anyString())).thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/title", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"title\":\"Read\"}"))
          .andExpect(status().isNoContent());

      verify(habitService).updateTitle(HABIT_ID, "Read");
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/description")
  class UpdateDescription {

    @Test
    @DisplayName("returns 204 and delegates description to service")
    void shouldReturn204AndDelegateDescription() throws Exception {
      when(habitService.updateDescription(eq(HABIT_ID), anyString())).thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/description", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"description\":\"Read 20 pages\"}"))
          .andExpect(status().isNoContent());

      verify(habitService).updateDescription(HABIT_ID, "Read 20 pages");
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/tags")
  class UpdateTags {

    @Test
    @DisplayName("returns 204 and maps tags to value objects")
    void shouldReturn204AndMapTags() throws Exception {
      when(habitService.updateTags(eq(HABIT_ID), anyList())).thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/tags", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"tags\":[\"%s\",\"%s\"]}".formatted(TAG_HEALTH, TAG_MINDSET)))
          .andExpect(status().isNoContent());

      verify(habitService)
          .updateTags(
              eq(HABIT_ID),
              argThat(tags -> tags.equals(List.of(new Tag(TAG_HEALTH), new Tag(TAG_MINDSET)))));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/recurrence")
  class UpdateRecurrence {

    @Test
    @DisplayName("returns 204 for DAILY recurrence")
    void shouldReturn204ForDaily() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(DailyRecurrence.class)))
          .thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"type\":\"DAILY\"}"))
          .andExpect(status().isNoContent());

      verify(habitService).updateRecurrence(eq(HABIT_ID), any(DailyRecurrence.class));
    }

    @Test
    @DisplayName("returns 204 for WEEKLY recurrence")
    void shouldReturn204ForWeekly() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(WeeklyRecurrence.class)))
          .thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"type\":\"WEEKLY\",\"dayOfWeek\":\"%s\"}"
                          .formatted(DEFAULT_DAY_OF_WEEK.name())))
          .andExpect(status().isNoContent());

      ArgumentCaptor<habitquest.tracking.domain.reminder.Recurrence> captor =
          ArgumentCaptor.forClass(habitquest.tracking.domain.reminder.Recurrence.class);
      verify(habitService).updateRecurrence(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(WEEKLY_RECURRENCE);
    }

    @Test
    @DisplayName("returns 204 for MONTHLY recurrence")
    void shouldReturn204ForMonthly() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(MonthlyRecurrence.class)))
          .thenReturn(hydrateHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"type\":\"MONTHLY\",\"dayOfMonth\":%d}".formatted(DEFAULT_DAY_OF_MONTH)))
          .andExpect(status().isNoContent());

      ArgumentCaptor<habitquest.tracking.domain.reminder.Recurrence> captor =
          ArgumentCaptor.forClass(habitquest.tracking.domain.reminder.Recurrence.class);
      verify(habitService).updateRecurrence(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(MONTHLY_RECURRENCE);
    }

    @Test
    @DisplayName("returns 400 when recurrence type is unknown")
    void shouldReturn400OnUnknownType() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"type\":\"YEARLY\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Unknown recurrence type: YEARLY"));

      verify(habitService, never()).updateRecurrence(eq(HABIT_ID), any());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/habits/{id}/attend")
  class AttendHabit {

    @Test
    @DisplayName("returns 204 and delegates attend date")
    void shouldReturn204AndDelegateAttendDate() throws Exception {
      when(habitService.attendHabit(eq(HABIT_ID), any(LocalDateTime.class)))
          .thenReturn(hydrateHabit());

      mockMvc
          .perform(
              post("/api/v1/habits/{id}/attend", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"date\":\"%s\"}".formatted(NEXT_ATTENDED_AT)))
          .andExpect(status().isNoContent());

      ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
      verify(habitService).attendHabit(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(NEXT_ATTENDED_AT);
    }

    @Test
    @DisplayName("returns 400 when domain rejects attendance")
    void shouldReturn400OnDomainError() throws Exception {
      when(habitService.attendHabit(eq(HABIT_ID), any(LocalDateTime.class)))
          .thenThrow(new IllegalStateException("Attendance date cannot be in the future"));

      mockMvc
          .perform(
              post("/api/v1/habits/{id}/attend", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"date\":\"2027-01-01T10:00:00\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Attendance date cannot be in the future"));
    }

    @Test
    @DisplayName("returns 502 when quest-service synchronization fails")
    void shouldReturn502OnQuestCommunicationError() throws Exception {
      when(habitService.attendHabit(eq(HABIT_ID), any(LocalDateTime.class)))
          .thenThrow(
              new QuestCommunicationException(
                  "Failed to update quest progress for quest " + QUEST_1,
                  new RuntimeException("downstream error")));

      mockMvc
          .perform(
              post("/api/v1/habits/{id}/attend", HABIT_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"date\":\"%s\"}".formatted(NEXT_ATTENDED_AT)))
          .andExpect(status().isBadGateway())
          .andExpect(
              jsonPath("$.message").value("Failed to update quest progress for quest " + QUEST_1));
    }
  }
}
