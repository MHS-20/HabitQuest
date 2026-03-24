package habitquest.edge.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import common.ddd.Id;
import habitquest.edge.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-chars!!",
      "app.jwt.expiration-seconds=3600",
      "spring.cloud.config.enabled=false",
      "management.endpoint.health.group.readiness.include=",
      "management.endpoint.health.group.liveness.include="
    })
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class GatewayRoutingTest {

  @LocalServerPort private int port;

  @Autowired private RestClient.Builder restClientBuilder;

  private RestClient restClient;
  private String authHeader;
  private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-chars!!", 3600L);

  private static final String LOCALHOST_PREFIX = "http://localhost:";
  private static final String AVATAR_PATH = "/api/v1/avatars/1";
  private static final String AUTH_HEADER = "Authorization";

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

  @DynamicPropertySource
  static void overrideServiceUrls(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.gateway.mvc.routes[0].id", () -> "avatar-service");
    registry.add(
        "spring.cloud.gateway.mvc.routes[0].uri", () -> LOCALHOST_PREFIX + avatarService.getPort());
    registry.add(
        "spring.cloud.gateway.mvc.routes[0].predicates[0]", () -> "Path=/api/v1/avatars/**");

    registry.add("spring.cloud.gateway.mvc.routes[1].id", () -> "guild-service");
    registry.add(
        "spring.cloud.gateway.mvc.routes[1].uri", () -> LOCALHOST_PREFIX + guildService.getPort());
    registry.add(
        "spring.cloud.gateway.mvc.routes[1].predicates[0]", () -> "Path=/api/v1/guilds/**");

    registry.add("spring.cloud.gateway.mvc.routes[2].id", () -> "battle-service");
    registry.add(
        "spring.cloud.gateway.mvc.routes[2].uri", () -> LOCALHOST_PREFIX + guildService.getPort());
    registry.add(
        "spring.cloud.gateway.mvc.routes[2].predicates[0]", () -> "Path=/api/v1/battles/**");

    registry.add("spring.cloud.gateway.mvc.routes[3].id", () -> "quest-service");
    registry.add(
        "spring.cloud.gateway.mvc.routes[3].uri", () -> LOCALHOST_PREFIX + questService.getPort());
    registry.add(
        "spring.cloud.gateway.mvc.routes[3].predicates[0]", () -> "Path=/api/v1/quests/**");

    registry.add("spring.cloud.gateway.mvc.routes[4].id", () -> "tracking-service");
    registry.add(
        "spring.cloud.gateway.mvc.routes[4].uri",
        () -> LOCALHOST_PREFIX + trackingService.getPort());
    registry.add(
        "spring.cloud.gateway.mvc.routes[4].predicates[0]", () -> "Path=/api/v1/habits/**");

    registry.add("services.avatar.base-url", () -> LOCALHOST_PREFIX + avatarService.getPort());
  }

  @BeforeEach
  void setUp() {
    // Il RestClient punta all'istanza del Gateway in esecuzione sulla porta random
    restClient = restClientBuilder.baseUrl("http://localhost:" + port).build();
    User user = new User(new Id<>("user-1"), "mario rossi", "mario@example.com", "hash");
    authHeader = "Bearer " + jwtService.generateToken(user);
  }

  @Test
  @DisplayName("GET /api/v1/avatars/** → avatar-service")
  void route_avatars_forwardsToAvatarService() {
    avatarService.stubFor(
        WireMock.get(urlPathEqualTo(AVATAR_PATH))
            .willReturn(okJson("{\"id\":1,\"name\":\"hero\"}")));

    ResponseEntity<String> response =
        restClient
            .get()
            .uri(AVATAR_PATH)
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains("\"name\":\"hero\"");

    avatarService.verify(getRequestedFor(urlPathEqualTo(AVATAR_PATH)));
  }

  @Test
  @DisplayName("GET /api/v1/guilds/** → guild-service")
  void route_guilds_forwardsToGuildService() {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/guilds/1"))
            .willReturn(okJson("{\"id\":1,\"name\":\"Warriors\"}")));

    ResponseEntity<String> response =
        restClient
            .get()
            .uri("/api/v1/guilds/1")
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains("\"name\":\"Warriors\"");

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/guilds/1")));
  }

  @Test
  @DisplayName("GET /api/v1/battles/** → guild-service (stesso servizio)")
  void route_battles_forwardsToGuildService() {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/battles/42"))
            .willReturn(okJson("{\"id\":42,\"status\":\"active\"}")));

    ResponseEntity<String> response =
        restClient
            .get()
            .uri("/api/v1/battles/42")
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains("\"status\":\"active\"");

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/battles/42")));
  }

  @Test
  @DisplayName("GET /api/v1/quests/** → quest-service")
  void route_quests_forwardsToQuestService() {
    questService.stubFor(
        get(urlPathEqualTo("/api/v1/quests/7"))
            .willReturn(okJson("{\"id\":7,\"title\":\"Slay the dragon\"}")));

    ResponseEntity<String> response =
        restClient
            .get()
            .uri("/api/v1/quests/7")
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains("\"title\":\"Slay the dragon\"");

    questService.verify(getRequestedFor(urlPathEqualTo("/api/v1/quests/7")));
  }

  @Test
  @DisplayName("GET /api/v1/habits/** → tracking-service")
  void route_habits_forwardsToTrackingService() {
    trackingService.stubFor(
        get(urlPathEqualTo("/api/v1/habits/3"))
            .willReturn(okJson("{\"id\":3,\"name\":\"Morning run\"}")));

    ResponseEntity<String> response =
        restClient
            .get()
            .uri("/api/v1/habits/3")
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toEntity(String.class);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).contains("\"name\":\"Morning run\"");

    trackingService.verify(getRequestedFor(urlPathEqualTo("/api/v1/habits/3")));
  }

  @Test
  @DisplayName("il gateway propaga l'header Authorization ai servizi a valle")
  void route_propagatesAuthorizationHeader() {
    avatarService.stubFor(get(urlPathEqualTo(AVATAR_PATH)).willReturn(okJson("{\"id\":1}")));

    ResponseEntity<Void> response =
        restClient
            .get()
            .uri(AVATAR_PATH)
            .header(AUTH_HEADER, authHeader)
            .retrieve()
            .toBodilessEntity();

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

    avatarService.verify(
        getRequestedFor(urlPathEqualTo(AVATAR_PATH)).withHeader(AUTH_HEADER, equalTo(authHeader)));
  }
}
