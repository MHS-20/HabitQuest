package habitquest.quest.infrastructure.inbound;

import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.infrastructure.dto.QuestQueries;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {
  @ExceptionHandler(QuestNotFoundException.class)
  public ResponseEntity<QuestQueries.ErrorResponse> handleQuestNotFound(QuestNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<QuestQueries.ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new QuestQueries.ErrorResponse(ex.getMessage()));
  }
}
