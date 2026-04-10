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
import habitquest.quest.application.QuestProgressView;
import habitquest.quest.application.QuestService;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.MoneyReward;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.Reward;
import habitquest.quest.infrastructure.dto.QuestResponse;
import habitquest.quest.infrastructure.dto.QuestResponseAssembler;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
  @MockitoBean private QuestResponseAssembler questResponseAssembler;
  @MockitoBean private QuestLogger log;

  @Nested
  @DisplayName("POST /api/v1/quests")
  class CreateQuest {

    @Test
    @DisplayName("returns 201 with the new quest id")
    void shouldReturn201WithId() throws Exception {
      when(questService.createQuest(QUEST_NAME, Duration.ofDays(14))).thenReturn(fullQuest());
      when(questResponseAssembler.toCreatedModel(any(Quest.class)))
          .thenReturn(EntityModel.of(new QuestController.QuestCreatedResponse(QUEST_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Morning Routine\",\"durationDays\":14}"))
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/v1/quests/" + QUEST_ID.value()))
          .andExpect(jsonPath("$.id").value(QUEST_ID.value()));
    }

    @Test
    @DisplayName("delegates name and duration to the service")
    void shouldDelegateNameAndDurationToService() throws Exception {
      when(questService.createQuest(anyString(), any(Duration.class))).thenReturn(fullQuest());
      when(questResponseAssembler.toCreatedModel(any(Quest.class)))
          .thenReturn(EntityModel.of(new QuestController.QuestCreatedResponse(QUEST_ID.value())));

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Morning Routine\",\"durationDays\":14}"))
          .andExpect(status().isCreated());

      verify(questService).createQuest(QUEST_NAME, Duration.ofDays(14));
    }

    @Test
    @DisplayName("returns 400 when domain rejects the request")
    void shouldReturn400OnDomainError() throws Exception {
      when(questService.createQuest(anyString(), any(Duration.class)))
          .thenThrow(new IllegalArgumentException("Quest name cannot be blank"));

      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"\",\"durationDays\":14}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Quest name cannot be blank"));
    }

    @Test
    @DisplayName("returns 400 when duration days is invalid")
    void shouldReturn400OnInvalidDurationDays() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/quests")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\":\"Morning Routine\",\"durationDays\":0}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Duration days must be greater than 0"));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests")
  class GetAllQuests {

    @Test
    @DisplayName("returns 200 with all quests")
    void shouldReturn200WithAllQuests() throws Exception {
      when(questService.getAllQuests()).thenReturn(List.of(fullQuest()));
      when(questResponseAssembler.toCollectionModel(anyList()))
          .thenReturn(
              CollectionModel.of(
                  List.of(
                      EntityModel.of(
                          new QuestResponse(QUEST_ID.value(), QUEST_NAME, 2, 5, List.of())))));

      mockMvc
          .perform(get("/api/v1/quests"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$._embedded.*[0].id").value(QUEST_ID.value()))
          .andExpect(jsonPath("$._embedded.*[0].name").value(QUEST_NAME));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}")
  class GetQuest {

    @Test
    @DisplayName("returns 200 with quest data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(questService.getQuest(QUEST_ID)).thenReturn(fullQuest());
      when(questResponseAssembler.toModel(any(Quest.class)))
          .thenReturn(
              EntityModel.of(
                  new QuestResponse(
                      QUEST_ID.value(), QUEST_NAME, 2, 5, List.of(HABIT_ID_1.value()))));

      mockMvc
          .perform(get("/api/v1/quests/{id}", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(QUEST_ID.value()))
          .andExpect(jsonPath("$.name").value(QUEST_NAME))
          .andExpect(jsonPath("$.durationDays").value(2))
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
      when(questResponseAssembler.toNameModel(eq(QUEST_ID.value()), eq(QUEST_NAME)))
          .thenReturn(EntityModel.of(new QuestController.NameResponse(QUEST_NAME)));

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
      when(questService.getDuration(QUEST_ID)).thenReturn(Duration.ofDays(3));
      when(questResponseAssembler.toDurationModel(eq(QUEST_ID.value()), eq(Duration.ofDays(3))))
          .thenReturn(EntityModel.of(new QuestController.DurationResponse(3L)));

      mockMvc
          .perform(get("/api/v1/quests/{id}/duration", QUEST_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.durationDays").value(3));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/{id}/reward")
  class GetReward {

    @Test
    @DisplayName("returns 200 with quest reward")
    void shouldReturnReward() throws Exception {
      when(questService.getReward(QUEST_ID)).thenReturn(DEFAULT_MONEY_REWARD);
      when(questResponseAssembler.toRewardModel(eq(QUEST_ID.value()), any(Reward.class)))
          .thenReturn(EntityModel.of(DEFAULT_MONEY_REWARD));

      mockMvc
          .perform(get("/api/v1/quests/{id}/reward", QUEST_ID.value()))
          .andExpect(status().isOk());
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
    @DisplayName("returns 204 and delegates day-based duration to service")
    void shouldReturn204AndDelegateDuration() throws Exception {
      when(questService.updateDuration(eq(QUEST_ID), any(Duration.class))).thenReturn(fullQuest());
      mockMvc
          .perform(
              patch("/api/v1/quests/{id}/duration", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"durationDays\":7}"))
          .andExpect(status().isNoContent());

      ArgumentCaptor<Duration> captor = ArgumentCaptor.forClass(Duration.class);
      verify(questService).updateDuration(eq(QUEST_ID), captor.capture());
      assertThat(captor.getValue()).isEqualTo(Duration.ofDays(7));
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

  @Nested
  @DisplayName("POST /api/v1/quests/{id}/attendance")
  class RecordAttendance {

    @Test
    @DisplayName("returns 204 and delegates attendance payload to service")
    void shouldReturn204AndDelegateAttendance() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/quests/{id}/attendance", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId": "avatar-1",
                                        "habitId": "habit-1",
                                        "attendedOn": "2026-04-03"
                                      }
                                      """))
          .andExpect(status().isNoContent());

      verify(questService)
          .recordHabitAttendance(
              QUEST_ID, new Id<>("avatar-1"), HABIT_ID_1, java.time.LocalDate.parse("2026-04-03"));
    }
  }

  @Nested
  @DisplayName("POST /api/v1/quests/{id}/join")
  class JoinQuest {

    @Test
    @DisplayName("returns 204 and delegates join payload to service")
    void shouldReturn204AndDelegateJoin() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/quests/{id}/join", QUEST_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {
                                        "avatarId": "avatar-1"
                                      }
                                      """))
          .andExpect(status().isNoContent());

      verify(questService)
          .joinQuest(eq(QUEST_ID), eq(new Id<>("avatar-1")), any(java.time.LocalDate.class));
    }
  }

  @Nested
  @DisplayName("GET /api/v1/quests/progress/{avatarId}")
  class GetActiveQuestProgress {

    @Test
    @DisplayName("returns active quest progress for avatar")
    void shouldReturnProgressList() throws Exception {
      List<QuestProgressView> progressViews =
          List.of(
              new QuestProgressView(
                  QUEST_ID.value(),
                  QUEST_NAME,
                  "IN_PROGRESS",
                  50,
                  List.of(
                      new QuestProgressView.HabitProgressView(
                          HABIT_ID_1.value(), HABIT_TITLE, 2, 1, 1))));

      when(questService.getActiveQuestProgressByAvatar(new Id<>("avatar-1")))
          .thenReturn(progressViews);
      when(questResponseAssembler.toProgressModel(eq("avatar-1"), anyList()))
          .thenReturn(
              EntityModel.of(
                  new QuestController.AvatarQuestProgressResponse(
                      "avatar-1",
                      List.of(
                          new QuestController.QuestProgressResponse(
                              QUEST_ID.value(),
                              QUEST_NAME,
                              "IN_PROGRESS",
                              50,
                              List.of(
                                  new QuestController.HabitProgressResponse(
                                      HABIT_ID_1.value(), HABIT_TITLE, 2, 1, 1)))))));

      mockMvc
          .perform(get("/api/v1/quests/progress/{avatarId}", "avatar-1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.avatarId").value("avatar-1"))
          .andExpect(jsonPath("$.quests[0].questId").value(QUEST_ID.value()))
          .andExpect(jsonPath("$.quests[0].completionPercentage").value(50))
          .andExpect(jsonPath("$.quests[0].habits[0].remainingOccurrences").value(1));
    }
  }
}
