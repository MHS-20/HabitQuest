package habitquest.edge.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import common.ddd.Id;
import habitquest.edge.domain.User;
import habitquest.edge.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-chars!!",
      "app.jwt.expiration-seconds=3600",
      "spring.cloud.config.enabled=false",
      "management.endpoint.health.group.readiness.include=",
      "management.endpoint.health.group.liveness.include="
    })
@AutoConfigureWebTestClient
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class GatewayRoutingTest {

  @RegisterExtension
  static WireMockExtension avatarService =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @RegisterExtension
  static WireMockExtension guildService =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @RegisterExtension
  static WireMockExtension questService =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @RegisterExtension
  static WireMockExtension trackingService =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  // Common constants to avoid repeated literals
  private static final String LOCALHOST_PREFIX = "http://localhost:";
  private static final String AVATAR_PATH = "/api/v1/avatars/1";
  private static final String AUTH_HEADER = "Authorization";

  @DynamicPropertySource
  @SuppressWarnings("unused")
  static void overrideServiceUrls(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.gateway.routes[0].id", () -> "avatar-service");
    registry.add(
        "spring.cloud.gateway.routes[0].uri", () -> LOCALHOST_PREFIX + avatarService.getPort());
    registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/api/v1/avatars/**");

    registry.add("spring.cloud.gateway.routes[1].id", () -> "guild-service");
    registry.add(
        "spring.cloud.gateway.routes[1].uri", () -> LOCALHOST_PREFIX + guildService.getPort());
    registry.add("spring.cloud.gateway.routes[1].predicates[0]", () -> "Path=/api/v1/guilds/**");

    registry.add("spring.cloud.gateway.routes[2].id", () -> "battle-service");
    registry.add(
        "spring.cloud.gateway.routes[2].uri", () -> LOCALHOST_PREFIX + guildService.getPort());
    registry.add("spring.cloud.gateway.routes[2].predicates[0]", () -> "Path=/api/v1/battles/**");

    registry.add("spring.cloud.gateway.routes[3].id", () -> "quest-service");
    registry.add(
        "spring.cloud.gateway.routes[3].uri", () -> LOCALHOST_PREFIX + questService.getPort());
    registry.add(
        "spring.cloud.gateway.routes[3].predicates[0]",
        () -> "Path=/api/v1/quests,/api/v1/quests/**");

    registry.add("spring.cloud.gateway.routes[4].id", () -> "tracking-service");
    registry.add(
        "spring.cloud.gateway.routes[4].uri", () -> LOCALHOST_PREFIX + trackingService.getPort());
    registry.add("spring.cloud.gateway.routes[4].predicates[0]", () -> "Path=/api/v1/habits/**");
    registry.add("services.avatar.base-url", () -> LOCALHOST_PREFIX + avatarService.getPort());
  }

  @Autowired WebTestClient webTestClient;

  // Must match app.jwt.secret property above
  private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-chars!!", 3600L);

  private String authHeader;

  @BeforeEach
  void setUp() {
    User user = new User(new Id<>("user-1"), "mario rossi", "mario@example.com", "hash");
    authHeader = "Bearer " + jwtService.generateToken(user);
  }

  @Test
  @DisplayName("GET /api/v1/avatars/** → avatar-service")
  void route_avatars_forwardsToAvatarService() {
    avatarService.stubFor(
        WireMock.get(urlPathEqualTo(AVATAR_PATH))
            .willReturn(okJson("{\"id\":1,\"name\":\"hero\"}")));

    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("hero");

    avatarService.verify(getRequestedFor(urlPathEqualTo(AVATAR_PATH)));
  }

  @Test
  @DisplayName("GET /api/v1/guilds/** → guild-service")
  void route_guilds_forwardsToGuildService() {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/guilds/1"))
            .willReturn(okJson("{\"id\":1,\"name\":\"Warriors\"}")));

    webTestClient
        .get()
        .uri("/api/v1/guilds/1")
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("Warriors");

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/guilds/1")));
  }

  @Test
  @DisplayName("GET /api/v1/battles/** → guild-service (stesso servizio)")
  void route_battles_forwardsToGuildService() {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/battles/42"))
            .willReturn(okJson("{\"id\":42,\"status\":\"active\"}")));

    webTestClient
        .get()
        .uri("/api/v1/battles/42")
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("active");

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/battles/42")));
  }

  @Test
  @DisplayName("GET /api/v1/quests/** → quest-service")
  void route_quests_forwardsToQuestService() {
    questService.stubFor(
        get(urlPathEqualTo("/api/v1/quests/7"))
            .willReturn(okJson("{\"id\":7,\"title\":\"Slay the dragon\"}")));

    webTestClient
        .get()
        .uri("/api/v1/quests/7")
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.title")
        .isEqualTo("Slay the dragon");

    questService.verify(getRequestedFor(urlPathEqualTo("/api/v1/quests/7")));
  }

  @Test
  @DisplayName("GET /api/v1/quests → quest-service")
  void route_questsCollection_forwardsToQuestService() {
    questService.stubFor(
        get(urlPathEqualTo("/api/v1/quests")).willReturn(okJson("{\"_embedded\":{}}")));

    webTestClient
        .get()
        .uri("/api/v1/quests")
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk();

    questService.verify(getRequestedFor(urlPathEqualTo("/api/v1/quests")));
  }

  @Test
  @DisplayName("GET /api/v1/habits/** → tracking-service")
  void route_habits_forwardsToTrackingService() {
    trackingService.stubFor(
        get(urlPathEqualTo("/api/v1/habits/3"))
            .willReturn(okJson("{\"id\":3,\"name\":\"Morning run\"}")));

    webTestClient
        .get()
        .uri("/api/v1/habits/3")
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("Morning run");

    trackingService.verify(getRequestedFor(urlPathEqualTo("/api/v1/habits/3")));
  }

  @Test
  @DisplayName("il gateway propaga l'header Authorization ai servizi a valle")
  void route_propagatesAuthorizationHeader() {
    avatarService.stubFor(get(urlPathEqualTo(AVATAR_PATH)).willReturn(okJson("{\"id\":1}")));

    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, authHeader)
        .exchange()
        .expectStatus()
        .isOk();

    avatarService.verify(
        getRequestedFor(urlPathEqualTo(AVATAR_PATH)).withHeader(AUTH_HEADER, equalTo(authHeader)));
  }
}
