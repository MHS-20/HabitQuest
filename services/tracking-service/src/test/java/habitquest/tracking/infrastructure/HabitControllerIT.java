package habitquest.tracking.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import habitquest.tracking.application.HabitNotFoundException;
import habitquest.tracking.application.HabitService;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.DailyRecurrence;
import habitquest.tracking.domain.reminder.MonthlyRecurrence;
import habitquest.tracking.domain.reminder.WeeklyRecurrence;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

  private static final String HABIT_ID = "habit-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String TITLE = "Hydrate";
  private static final String DESCRIPTION = "Drink 2L of water";
  private static final String UNKNOWN_ID = "ghost-99";

  private Habit stubHabit() {
    Habit habit =
        new Habit(
            HABIT_ID, AVATAR_ID, TITLE, DESCRIPTION, new DailyRecurrence(), Optional.of("quest-1"));
    habit.setTitle(TITLE);
    habit.setDescription(DESCRIPTION);
    habit.setRecurrence(new DailyRecurrence());
    habit.setTags(List.of(new Tag("health")));
    habit.attendHabit(LocalDateTime.of(2026, 3, 17, 8, 0));
    return habit;
  }

  @Nested
  @DisplayName("POST /api/v1/habits")
  class CreateHabit {

    @Test
    @DisplayName("returns 201 with the new habit id")
    void shouldReturn201WithId() throws Exception {
      when(habitService.createDailyHabit(anyString(), anyString(), anyString()))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "avatarId":"avatar-1",
                        "title":"Hydrate",
                        "description":"Drink 2L of water",
                        "recurrenceType":"DAILY"
                      }
                      """))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/habits/" + HABIT_ID))
          .andExpect(jsonPath("$.id").value(HABIT_ID));
    }

    @Test
    @DisplayName("delegates DAILY payload to createDailyHabit")
    void shouldDelegateDailyPayloadToService() throws Exception {
      when(habitService.createDailyHabit(anyString(), anyString(), anyString()))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "avatarId":"avatar-1",
                        "title":"Hydrate",
                        "description":"Drink 2L of water",
                        "recurrenceType":"DAILY"
                      }
                      """))
          .andExpect(status().isCreated());

      verify(habitService).createDailyHabit(AVATAR_ID, TITLE, DESCRIPTION);
      verify(habitService, never()).createWeeklyHabit(anyString(), anyString(), anyString(), any());
      verify(habitService, never())
          .createMonthlyHabit(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("delegates WEEKLY payload to createWeeklyHabit")
    void shouldDelegateWeeklyPayloadToService() throws Exception {
      when(habitService.createWeeklyHabit(anyString(), anyString(), anyString(), any()))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "avatarId":"avatar-1",
                        "title":"Hydrate",
                        "description":"Drink 2L of water",
                        "recurrenceType":"WEEKLY",
                        "dayOfWeek":"MONDAY"
                      }
                      """))
          .andExpect(status().isCreated());

      verify(habitService).createWeeklyHabit(AVATAR_ID, TITLE, DESCRIPTION, DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("delegates MONTHLY payload to createMonthlyHabit")
    void shouldDelegateMonthlyPayloadToService() throws Exception {
      when(habitService.createMonthlyHabit(anyString(), anyString(), anyString(), anyInt()))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              post("/api/v1/habits")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "avatarId":"avatar-1",
                        "title":"Hydrate",
                        "description":"Drink 2L of water",
                        "recurrenceType":"MONTHLY",
                        "dayOfMonth":15
                      }
                      """))
          .andExpect(status().isCreated());

      verify(habitService).createMonthlyHabit(AVATAR_ID, TITLE, DESCRIPTION, 15);
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
                        "avatarId":"avatar-1",
                        "title":"Hydrate",
                        "description":"Drink 2L of water",
                        "recurrenceType":"YEARLY"
                      }
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Unknown recurrence type: YEARLY"));

      verify(habitService, never()).createDailyHabit(anyString(), anyString(), anyString());
      verify(habitService, never()).createWeeklyHabit(anyString(), anyString(), anyString(), any());
      verify(habitService, never())
          .createMonthlyHabit(anyString(), anyString(), anyString(), anyInt());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}")
  class GetHabit {

    @Test
    @DisplayName("returns 200 with habit data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(habitService.getHabitById(HABIT_ID)).thenReturn(stubHabit());

      mockMvc
          .perform(get("/api/v1/habits/{id}", HABIT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(HABIT_ID));
    }

    @Test
    @DisplayName("returns 404 when habit does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(habitService.getHabitById(UNKNOWN_ID)).thenThrow(new HabitNotFoundException(UNKNOWN_ID));

      mockMvc.perform(get("/api/v1/habits/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/habits/{id}")
  class DeleteHabit {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(habitService).deleteHabitById(HABIT_ID);

      mockMvc.perform(delete("/api/v1/habits/{id}", HABIT_ID)).andExpect(status().isNoContent());

      verify(habitService).deleteHabitById(HABIT_ID);
    }

    @Test
    @DisplayName("returns 404 when habit does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new HabitNotFoundException(UNKNOWN_ID))
          .when(habitService)
          .deleteHabitById(UNKNOWN_ID);

      mockMvc.perform(delete("/api/v1/habits/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/title")
  class GetTitle {

    @Test
    @DisplayName("returns 200 with habit title")
    void shouldReturnTitle() throws Exception {
      when(habitService.getTitle(HABIT_ID)).thenReturn(TITLE);

      mockMvc
          .perform(get("/api/v1/habits/{id}/title", HABIT_ID))
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

      mockMvc
          .perform(get("/api/v1/habits/{id}/description", HABIT_ID))
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
      when(habitService.getTags(HABIT_ID))
          .thenReturn(List.of(new Tag("health"), new Tag("fitness")));

      mockMvc
          .perform(get("/api/v1/habits/{id}/tags", HABIT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.tags[0].name").value("health"))
          .andExpect(jsonPath("$.tags[1].name").value("fitness"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/recurrence")
  class GetRecurrence {

    @Test
    @DisplayName("returns 200 with recurrence payload")
    void shouldReturnRecurrence() throws Exception {
      when(habitService.getRecurrence(HABIT_ID)).thenReturn(new WeeklyRecurrence(DayOfWeek.MONDAY));

      mockMvc
          .perform(get("/api/v1/habits/{id}/recurrence", HABIT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/last-attended-date")
  class GetLastAttendedDate {

    @Test
    @DisplayName("returns 200 with last attended date")
    void shouldReturnLastAttendedDate() throws Exception {
      LocalDateTime attendedAt = LocalDateTime.of(2026, 3, 16, 10, 15);
      when(habitService.getLastAttendedDate(HABIT_ID)).thenReturn(attendedAt);

      mockMvc
          .perform(get("/api/v1/habits/{id}/last-attended-date", HABIT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.date").value("2026-03-16T10:15:00"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/habits/{id}/history")
  class GetHistory {

    @Test
    @DisplayName("returns 200 with habit event history")
    void shouldReturnHistory() throws Exception {
      when(habitService.getHistory(HABIT_ID))
          .thenReturn(
              List.of(
                  new HabitHistoryEvent(
                      new HabitAttended(stubHabit()),
                      LocalDateTime.of(2026, 3, 17, 9, 30),
                      "attendedAt=2026-03-17T09:30")));

      mockMvc
          .perform(get("/api/v1/habits/{id}/history", HABIT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.history[0].event.habit.id").value(HABIT_ID))
          .andExpect(jsonPath("$.history[0].details").value("attendedAt=2026-03-17T09:30"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/title")
  class UpdateTitle {

    @Test
    @DisplayName("returns 204 and delegates title to service")
    void shouldReturn204AndDelegateTitle() throws Exception {
      when(habitService.updateTitle(eq(HABIT_ID), anyString())).thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/title", HABIT_ID)
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
      when(habitService.updateDescription(eq(HABIT_ID), anyString())).thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/description", HABIT_ID)
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
      when(habitService.updateTags(eq(HABIT_ID), anyList())).thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/tags", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"tags\":[\"health\",\"mindset\"]}"))
          .andExpect(status().isNoContent());

      verify(habitService)
          .updateTags(
              eq(HABIT_ID),
              argThat(tags -> tags.equals(List.of(new Tag("health"), new Tag("mindset")))));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/habits/{id}/recurrence")
  class UpdateRecurrence {

    @Test
    @DisplayName("returns 204 for DAILY recurrence")
    void shouldReturn204ForDaily() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(DailyRecurrence.class)))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"type\":\"DAILY\"}"))
          .andExpect(status().isNoContent());

      verify(habitService).updateRecurrence(eq(HABIT_ID), any(DailyRecurrence.class));
    }

    @Test
    @DisplayName("returns 204 for WEEKLY recurrence")
    void shouldReturn204ForWeekly() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(WeeklyRecurrence.class)))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"type\":\"WEEKLY\",\"dayOfWeek\":\"MONDAY\"}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<habitquest.tracking.domain.reminder.Recurrence> captor =
          ArgumentCaptor.forClass(habitquest.tracking.domain.reminder.Recurrence.class);
      verify(habitService).updateRecurrence(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(new WeeklyRecurrence(DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("returns 204 for MONTHLY recurrence")
    void shouldReturn204ForMonthly() throws Exception {
      when(habitService.updateRecurrence(eq(HABIT_ID), any(MonthlyRecurrence.class)))
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"type\":\"MONTHLY\",\"dayOfMonth\":15}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<habitquest.tracking.domain.reminder.Recurrence> captor =
          ArgumentCaptor.forClass(habitquest.tracking.domain.reminder.Recurrence.class);
      verify(habitService).updateRecurrence(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(new MonthlyRecurrence(15));
    }

    @Test
    @DisplayName("returns 400 when recurrence type is unknown")
    void shouldReturn400OnUnknownType() throws Exception {
      mockMvc
          .perform(
              patch("/api/v1/habits/{id}/recurrence", HABIT_ID)
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
          .thenReturn(stubHabit());

      mockMvc
          .perform(
              post("/api/v1/habits/{id}/attend", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"date\":\"2026-03-17T09:30:00\"}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
      verify(habitService).attendHabit(eq(HABIT_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(LocalDateTime.of(2026, 3, 17, 9, 30));
    }

    @Test
    @DisplayName("returns 400 when domain rejects attendance")
    void shouldReturn400OnDomainError() throws Exception {
      when(habitService.attendHabit(eq(HABIT_ID), any(LocalDateTime.class)))
          .thenThrow(new IllegalStateException("Attendance date cannot be in the future"));

      mockMvc
          .perform(
              post("/api/v1/habits/{id}/attend", HABIT_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"date\":\"2027-01-01T10:00:00\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Attendance date cannot be in the future"));
    }
  }
}
