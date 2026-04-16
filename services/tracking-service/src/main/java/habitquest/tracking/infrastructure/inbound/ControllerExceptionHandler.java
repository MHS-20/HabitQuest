package habitquest.tracking.infrastructure.inbound;

import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.infrastructure.dto.HabitQueries;
import habitquest.tracking.infrastructure.exceptions.AvatarCommunicationException;
import habitquest.tracking.infrastructure.exceptions.QuestCommunicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {
  @ExceptionHandler(HabitNotFoundException.class)
  public ResponseEntity<HabitQueries.ErrorResponse> handleHabitNotFound(HabitNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<HabitQueries.ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new HabitQueries.ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(AvatarCommunicationException.class)
  public ResponseEntity<HabitQueries.ErrorResponse> handleAvatarException(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new HabitQueries.ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(QuestCommunicationException.class)
  public ResponseEntity<HabitQueries.ErrorResponse> handleQuestException(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new HabitQueries.ErrorResponse(ex.getMessage()));
  }
}
