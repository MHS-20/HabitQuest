package habitquest.marketplace.infrastructure.inbound;

import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.exceptions.AvatarNotFoundException;
import habitquest.marketplace.application.exceptions.InsufficientLevelException;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(InsufficientLevelException.class)
  public ResponseEntity<ErrorResponse> handleBadOperation(RuntimeException ex) {
    return ResponseEntity.status(403).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(AvatarCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleAvatarCommunicationError(
      AvatarCommunicationException ex) {
    return ResponseEntity.status(502).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler({
    ItemNotFoundException.class,
    AvatarNotFoundException.class,
    MarketplaceNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

}
