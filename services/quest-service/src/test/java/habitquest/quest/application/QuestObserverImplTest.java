package habitquest.quest.application;

import static habitquest.quest.QuestFixtures.AVATAR_ID_1;
import static habitquest.quest.QuestFixtures.fullQuest;
import static org.mockito.Mockito.verify;

import habitquest.quest.domain.events.QuestCompleted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestObserverImpl")
class QuestObserverImplTest {

  @Mock private QuestNotifier questNotifier;
  @Mock private QuestLogger log;

  private QuestObserverImpl observer;

  @BeforeEach
  void setUp() {
    observer = new QuestObserverImpl(questNotifier, log);
  }

  @Test
  @DisplayName("handleQuestCompleted publishes event")
  void handleQuestCompletedShouldPublishEvent() {
    QuestCompleted event = new QuestCompleted(fullQuest(), AVATAR_ID_1);

    observer.handleQuestCompleted(event);

    verify(questNotifier).notifyQuestCompleted(event);
  }
}
