package habitquest.edge.domain;

import common.ddd.Id;

public record AuthResponse(String token, Id<User> userId) {}
