package habitquest.avatar.application.service;

import common.cqrs.Query;

public record AvatarSearchQuery(String name, Integer minLevel, Integer maxLevel) implements Query {}
