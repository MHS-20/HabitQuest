package habitquest.marketplace.infrastructure.dto;

public record ItemResponse(
    String type, String name, String description, Integer power, Integer price) {}
