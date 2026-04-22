package habitquest.guild.infrastructure.inbound;

import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.domain.guild.UnauthorizedGuildOperationException;
import habitquest.guild.infrastructure.dto.BattleCommands;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

  @ExceptionHandler(BattleNotFoundException.class)
  public ResponseEntity<Void> handleBattleNotFound(BattleNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(GuildNotFoundException.class)
  public ResponseEntity<Void> handleGuildNotFound(GuildNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<BattleCommands.ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new BattleCommands.ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedGuildOperationException.class)
  public ResponseEntity<BattleCommands.ErrorResponse> handleUnauthorized(
      UnauthorizedGuildOperationException ex) {
    return ResponseEntity.status(403).body(new BattleCommands.ErrorResponse(ex.getMessage()));
  }
}
