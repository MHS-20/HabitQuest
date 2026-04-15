package habitquest.avatar.infrastructure.inbound;

import habitquest.avatar.application.exceptions.AvatarNotFoundException;
import habitquest.avatar.application.exceptions.MarketplaceCommunicationException;
import habitquest.avatar.application.port.out.AvatarLogger;
import habitquest.avatar.infrastructure.dto.AvatarCommands.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AvatarExceptionHandler {

  private final AvatarLogger log;

  public AvatarExceptionHandler(AvatarLogger log) {
    this.log = log;
  }

  @ExceptionHandler(AvatarNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAvatarNotFound(AvatarNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Avatar not found", ex);
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Domain error", ex);
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(MarketplaceCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleMarketplaceError(RuntimeException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage());
    log.error(error, "Marketplace communication error", ex);
    return ResponseEntity.badRequest().body(error);
  }
}
