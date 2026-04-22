package habitquest.marketplace.infrastructure.inbound;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.exceptions.AvatarNotFoundException;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.in.MarketplaceCommandService;
import habitquest.marketplace.application.port.in.MarketplaceQueryService;
import habitquest.marketplace.application.port.out.MarketplaceLogger;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.infrastructure.dto.ItemMapper;
import habitquest.marketplace.infrastructure.dto.MarketplaceCommands.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponseAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marketplaces")
public class MarketplaceCommandController {

  private final MarketplaceCommandService commandService;
  private final MarketplaceQueryService queryService;
  private final MarketplaceLogger log;
  private final MarketplaceResponseAssembler assembler;

  public MarketplaceCommandController(
      MarketplaceCommandService commandService,
      MarketplaceQueryService queryService,
      MarketplaceLogger log,
      MarketplaceResponseAssembler assembler) {
    this.commandService = commandService;
    this.queryService = queryService;
    this.log = log;
    this.assembler = assembler;
  }

  private Id<Marketplace> idOfMarketplace(String marketplaceId) {
    return new Id<>(marketplaceId);
  }

  private Id<Avatar> idOfAvatar(String avatarId) {
    return new Id<>(avatarId);
  }

  // ─── Marketplace ────────────────────────────────────────────────────────────

  @PostMapping
  public ResponseEntity<EntityModel<MarketplaceResponse>> createMarketplace(
      @RequestBody CreateMarketplaceCommand request) throws AvatarCommunicationException {
    log.info(request, "Creating marketplace");
    String marketplaceId =
        commandService.createMarketplaceForAvatar(idOfAvatar(request.avatarId())).value();
    Marketplace marketplace = queryService.getMarketplace(idOfMarketplace(marketplaceId));
    log.info(marketplace, "Marketplace created");
    return ResponseEntity.created(
            linkTo(methodOn(MarketplaceQueryController.class).getMarketplace(marketplaceId))
                .toUri())
        .body(assembler.toModel(marketplace));
  }

  // ─── Commands ───────────────────────────────────────────────────────────────

  @PostMapping("/{marketplaceId}/items/buy")
  public ResponseEntity<Void> buyItem(
      @PathVariable String marketplaceId,
      @RequestParam Integer currentLevel,
      @RequestBody ItemCommand request)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {
    Item item = ItemMapper.toItem(request);
    commandService.buyItem(idOfMarketplace(marketplaceId), item, new Level(currentLevel));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{marketplaceId}/sold-items/sell")
  public ResponseEntity<Void> sellItem(
      @PathVariable String marketplaceId, @RequestBody ItemCommand request)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {
    Item item = ItemMapper.toItem(request);
    commandService.sellItem(idOfMarketplace(marketplaceId), item);
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ─────────────────────────────────────────────────────
  @ExceptionHandler({
    ItemNotFoundException.class,
    AvatarNotFoundException.class,
    MarketplaceNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }
}
