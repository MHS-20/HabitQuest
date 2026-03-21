package habitquest.edge.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import habitquest.edge.domain.User;
import java.net.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-chars!!",
      "app.jwt.expiration-seconds=3600",
      "spring.cloud.config.enabled=false",
      "management.endpoint.health.group.readiness.include=",
      "management.endpoint.health.group.liveness.include="
    })
@AutoConfigureMockMvc
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
@Import(GatewayRoutingTest.Http1RestClientConfig.class)
class GatewayRoutingTest {

  @TestConfiguration
  static class Http1RestClientConfig {
    @Bean
    @Primary
    @SuppressWarnings("unused")
    RestClient.Builder gatewayRestClientBuilder() {
      HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
      return RestClient.builder().requestFactory(new JdkClientHttpRequestFactory(httpClient));
    }
  }

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

  @Autowired MockMvc mockMvc;

  // Must match app.jwt.secret property above
  private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-chars!!", 3600L);

  private String authHeader;

  @BeforeEach
  void setUp() {
    User user = new User("user-1", "mario rossi", "mario@example.com", "hash");
    authHeader = "Bearer " + jwtService.generateToken(user);
  }

  @Test
  @DisplayName("GET /api/v1/avatars/** → avatar-service")
  void route_avatars_forwardsToAvatarService() throws Exception {
    avatarService.stubFor(
        WireMock.get(urlPathEqualTo(AVATAR_PATH))
            .willReturn(okJson("{\"id\":1,\"name\":\"hero\"}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get(AVATAR_PATH).header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("hero"));

    avatarService.verify(getRequestedFor(urlPathEqualTo(AVATAR_PATH)));
  }

  @Test
  @DisplayName("GET /api/v1/guilds/** → guild-service")
  void route_guilds_forwardsToGuildService() throws Exception {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/guilds/1"))
            .willReturn(okJson("{\"id\":1,\"name\":\"Warriors\"}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/guilds/1").header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Warriors"));

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/guilds/1")));
  }

  @Test
  @DisplayName("GET /api/v1/battles/** → guild-service (stesso servizio)")
  void route_battles_forwardsToGuildService() throws Exception {
    guildService.stubFor(
        get(urlPathEqualTo("/api/v1/battles/42"))
            .willReturn(okJson("{\"id\":42,\"status\":\"active\"}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/battles/42").header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("active"));

    guildService.verify(getRequestedFor(urlPathEqualTo("/api/v1/battles/42")));
  }

  @Test
  @DisplayName("GET /api/v1/quests/** → quest-service")
  void route_quests_forwardsToQuestService() throws Exception {
    questService.stubFor(
        get(urlPathEqualTo("/api/v1/quests/7"))
            .willReturn(okJson("{\"id\":7,\"title\":\"Slay the dragon\"}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/quests/7").header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Slay the dragon"));

    questService.verify(getRequestedFor(urlPathEqualTo("/api/v1/quests/7")));
  }

  @Test
  @DisplayName("GET /api/v1/habits/** → tracking-service")
  void route_habits_forwardsToTrackingService() throws Exception {
    trackingService.stubFor(
        get(urlPathEqualTo("/api/v1/habits/3"))
            .willReturn(okJson("{\"id\":3,\"name\":\"Morning run\"}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/v1/habits/3").header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Morning run"));

    trackingService.verify(getRequestedFor(urlPathEqualTo("/api/v1/habits/3")));
  }

  @Test
  @DisplayName("il gateway propaga l'header Authorization ai servizi a valle")
  void route_propagatesAuthorizationHeader() throws Exception {
    avatarService.stubFor(get(urlPathEqualTo(AVATAR_PATH)).willReturn(okJson("{\"id\":1}")));

    mockMvc
        .perform(MockMvcRequestBuilders.get(AVATAR_PATH).header(AUTH_HEADER, authHeader))
        .andExpect(status().isOk());

    avatarService.verify(
        getRequestedFor(urlPathEqualTo(AVATAR_PATH)).withHeader(AUTH_HEADER, equalTo(authHeader)));
  }
}
