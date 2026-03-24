package habitquest.quest.infrastructure;

import static habitquest.quest.QuestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import common.ddd.Id;
import habitquest.quest.application.QuestLogger;
import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestService;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import java.time.Duration;
import java.util.List;
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

@WebMvcTest(QuestController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("QuestController")
public class QuestControllerIT {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private QuestService questService;
  @MockitoBean private QuestLogger log;

  @Nested
  @DisplayName("POST /api/v1/quests")
  class CreateQuest {

    @Test
    @DisplayName("returns 201 with the new quest id")
    void shouldReturn201WithId() throws Exception {
      when(questService.createQuest(QUEST_NAME)).thenReturn(fullQuest());

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Morning Routine\"}"))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/quests/" + QUEST_ID.value()))
          .andExpect(jsonPath("$.id").value(QUEST_ID.value()));
    }

    @Test
    @DisplayName("delegates name to the service")
    void shouldDelegateNameToService() throws Exception {
      when(questService.createQuest(anyString())).thenReturn(fullQuest());

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Morning Routine\"}"))
          .andExpect(status().isCreated());

      verify(questService).createQuest(QUEST_NAME);
    }

    @Test
    @DisplayName("returns 400 when domain rejects the request")
    void shouldReturn400OnDomainError() throws Exception {
      when(questService.createQuest(anyString()))
          .thenThrow(new IllegalArgumentException("Quest name cannot be blank"));

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Quest name cannot be blank"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}")
  class GetQuest {

    @Test
    @DisplayName("returns 200 with quest data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(questService.getQuest(QUEST_ID)).thenReturn(fullQuest());

      mockMvc
          .perform(get("/api/v1/quests/{id}", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(QUEST_ID.value()))
          .andExpect(jsonPath("$.name").value(QUEST_NAME))
          .andExpect(jsonPath("$.duration").value("PT2H"))
          .andExpect(jsonPath("$.habitIds[0]").value(HABIT_ID_1.value()));
    }

    @Test
    @DisplayName("returns 404 when quest does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(questService.getQuest(UNKNOWN_ID))
          .thenThrow(new QuestNotFoundException(UNKNOWN_ID.value()));

      mockMvc
          .perform(get("/api/v1/quests/{id}", UNKNOWN_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/quests/{id}")
  class DeleteQuest {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(questService).deleteQuest(QUEST_ID);

      mockMvc
          .perform(delete("/api/v1/quests/{id}", QUEST_ID.value()))
          .andExpect(status().isNoContent());

      verify(questService).deleteQuest(QUEST_ID);
    }

    @Test
    @DisplayName("returns 404 when quest does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new QuestNotFoundException(UNKNOWN_ID.value()))
          .when(questService)
          .deleteQuest(UNKNOWN_ID);

      mockMvc
          .perform(delete("/api/v1/quests/{id}", UNKNOWN_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}/name")
  class GetName {

    @Test
    @DisplayName("returns 200 with quest name")
    void shouldReturnName() throws Exception {
      when(questService.getName(QUEST_ID)).thenReturn(QUEST_NAME);

      mockMvc
          .perform(get("/api/v1/quests/{id}/name", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value(QUEST_NAME));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}/duration")
  class GetDuration {

    @Test
    @DisplayName("returns 200 with quest duration")
    void shouldReturnDuration() throws Exception {
      when(questService.getDuration(QUEST_ID)).thenReturn(Duration.ofMinutes(90));

      mockMvc
          .perform(get("/api/v1/quests/{id}/duration", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.duration").value("PT1H30M"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}/reward")
  class GetReward {

    @Test
    @DisplayName("returns 200 with quest reward")
    void shouldReturnReward() throws Exception {
      when(questService.getReward(QUEST_ID)).thenReturn(DEFAULT_MONEY_REWARD);

      mockMvc
          .perform(get("/api/v1/quests/{id}/reward", QUEST_ID.value()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}/habits")
  class GetHabits {

    @Test
    @DisplayName("returns 200 with habit data")
    void shouldReturnHabits() throws Exception {
      when(questService.getHabits(QUEST_ID)).thenReturn(List.of(morningRunHabit()));
      mockMvc
          .perform(get("/api/v1/quests/{id}/habits", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.habits[0].id").value(HABIT_ID_1.value()))
          .andExpect(jsonPath("$.habits[0].title").value(HABIT_TITLE))
          .andExpect(jsonPath("$.habits[0].description").value(HABIT_DESC))
          .andExpect(jsonPath("$.habits[0].tags[0]").value(TAG_HEALTH))
          .andExpect(jsonPath("$.habits[0].recurrence.type").value("DAILY"));
    }

    @Test
    @DisplayName("returns 200 with full habit data when all fields are set")
    void shouldReturnFullHabitData() throws Exception {
      when(questService.getHabits(QUEST_ID)).thenReturn(List.of(morningRunHabit()));

      mockMvc
          .perform(get("/api/v1/quests/{id}/habits", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.habits[0].id").value(HABIT_ID_1.value()))
          .andExpect(jsonPath("$.habits[0].title").value(HABIT_TITLE))
          .andExpect(jsonPath("$.habits[0].description").value(HABIT_DESC))
          .andExpect(jsonPath("$.habits[0].tags[0]").value(TAG_HEALTH));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/quests/{id}/name")
  class UpdateName {

    @Test
    @DisplayName("returns 204 and delegates new name to service")
    void shouldReturn204AndDelegate() throws Exception {
      when(questService.updateName(eq(QUEST_ID), anyString())).thenReturn(fullQuest());
      mockMvc
          .perform(
              patch("/api/v1/quests/{id}/name", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"New Name\"}"))
          .andExpect(status().isNoContent());
      verify(questService).updateName(QUEST_ID, "New Name");
    }

    @Test
    @DisplayName("returns 400 when domain rejects blank name")
    void shouldReturn400OnBlankName() throws Exception {
      when(questService.updateName(QUEST_ID, ""))
          .thenThrow(new IllegalArgumentException("Name cannot be blank"));
      mockMvc
          .perform(
              patch("/api/v1/quests/{id}/name", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Name cannot be blank"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/quests/{id}/duration")
  class UpdateDuration {

    @Test
    @DisplayName("returns 204 and delegates parsed duration to service")
    void shouldReturn204AndDelegateDuration() throws Exception {
      when(questService.updateDuration(eq(QUEST_ID), any(Duration.class))).thenReturn(fullQuest());
      mockMvc
          .perform(
              patch("/api/v1/quests/{id}/duration", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"duration\":\"PT45M\"}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<Duration> captor = ArgumentCaptor.forClass(Duration.class);
      verify(questService).updateDuration(eq(QUEST_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(Duration.ofMinutes(45));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/quests/{id}/reward")
  class UpdateReward {

    @Test
    @DisplayName("returns 204 and delegates a MoneyReward to service")
    void shouldReturn204AndDelegateMoneyReward() throws Exception {
      when(questService.updateReward(eq(QUEST_ID), any(MoneyReward.class))).thenReturn(fullQuest());
      mockMvc
          .perform(
              patch("/api/v1/quests/{id}/reward", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"experience\":100,\"money\":10}"))
          .andExpect(status().isNoContent());

      verify(questService).updateReward(eq(QUEST_ID), any(MoneyReward.class));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/quests/{id}/habits")
  class AddHabit {

    @Test
    @DisplayName("returns 204 and delegates habit id to service")
    void shouldReturn204AndDelegateHabit() throws Exception {
      when(questService.addHabit(eq(QUEST_ID), any(Habit.class))).thenReturn(fullQuest());
      mockMvc
          .perform(
              post("/api/v1/quests/{id}/habits", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"habitId\":\"habit-7\",\"title\":\"Hydrate\"}"))
          .andExpect(status().isNoContent());
      ArgumentCaptor<Habit> captor = ArgumentCaptor.forClass(Habit.class);
      verify(questService).addHabit(eq(QUEST_ID), captor.capture());
      assertThat(captor.getValue().getId().value()).isEqualTo("habit-7");
    }

    @Test
    @DisplayName("returns 404 when quest does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(questService.addHabit(eq(UNKNOWN_ID), any(Habit.class)))
          .thenThrow(new QuestNotFoundException(UNKNOWN_ID.value()));
      mockMvc
          .perform(
              post("/api/v1/quests/{id}/habits", UNKNOWN_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"habitId\":\"habit-7\"}"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/quests/{id}/habits")
  class RemoveHabit {

    @Test
    @DisplayName("returns 204 and delegates habit id to service")
    void shouldReturn204AndDelegateHabit() throws Exception {
      when(questService.removeHabit(eq(QUEST_ID), any(Id.class))).thenReturn(fullQuest());
      mockMvc
          .perform(
              delete("/api/v1/quests/{id}/habits", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"habitId\":\"habit-7\"}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<Id> captor = ArgumentCaptor.forClass(Id.class);
      verify(questService).removeHabit(eq(QUEST_ID), captor.capture());
      assertThat(captor.getValue().value()).isEqualTo("habit-7");
    }

    @Test
    @DisplayName("returns 400 when domain rejects removal")
    void shouldReturn400OnInvalidRemove() throws Exception {
      when(questService.removeHabit(eq(QUEST_ID), any(Id.class)))
          .thenThrow(new IllegalStateException("Habit is not part of this quest"));
      mockMvc
          .perform(
              delete("/api/v1/quests/{id}/habits", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"habitId\":\"habit-404\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Habit is not part of this quest"));
    }
  }
}
