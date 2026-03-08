package habitquest.guild.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Guild")
class GuildTest {

  private static final String GUILD_ID = "guild-1";
  private static final String GUILD_NAME = "Guild of Heroes";
  private static final String LEADER_ID = "avatar-1";
  private static final GuildRole LEADER_ROLE = new GuildRole("Leader");
  private static final GuildRole MEMBER_ROLE = new GuildRole("Member");
  private static final GuildRole OFFICER_ROLE = new GuildRole("Officer");

  private GuildMember leader;
  private Guild guild;

  @BeforeEach
  void setUp() {
    leader = new GuildMember(LEADER_ID, "LeaderNick", LEADER_ROLE);
    guild = new Guild(GUILD_ID, GUILD_NAME, leader);
  }

  @Nested
  @DisplayName("creation")
  class Creation {

    @Test
    @DisplayName("should have the correct id")
    void shouldHaveCorrectId() {
      assertThat(guild.getId()).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should contain the leader as first member")
    void shouldContainLeaderAsMember() {
      assertThat(guild.getMembers()).hasSize(1);
      assertThat(guild.getMembers().get(0).getId()).isEqualTo(LEADER_ID);
    }

    @Test
    @DisplayName("should start with globalRank zero")
    void shouldStartWithRankZero() {
      assertThat(guild.getGlobalRank()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("addMember")
  class AddMember {

    @Test
    @DisplayName("should add a new member to the guild")
    void shouldAddNewMember() {
      GuildMember newMember = new GuildMember("avatar-2", "NewGuy", MEMBER_ROLE);

      guild.addMember(newMember);

      assertThat(guild.getMembers()).hasSize(2);
      assertThat(guild.getMembers()).extracting(GuildMember::getId).contains("avatar-2");
    }

    @Test
    @DisplayName("should allow adding multiple members")
    void shouldAddMultipleMembers() {
      guild.addMember(new GuildMember("avatar-2", "Nick2", MEMBER_ROLE));
      guild.addMember(new GuildMember("avatar-3", "Nick3", MEMBER_ROLE));

      assertThat(guild.getMembers()).hasSize(3);
    }
  }

  @Nested
  @DisplayName("removeMember")
  class RemoveMember {

    @Test
    @DisplayName("should remove an existing member by id")
    void shouldRemoveExistingMember() {
      GuildMember member = new GuildMember("avatar-2", "Nick2", MEMBER_ROLE);
      guild.addMember(member);

      guild.removeMember("avatar-2");

      assertThat(guild.getMembers()).extracting(GuildMember::getId).doesNotContain("avatar-2");
    }

    @Test
    @DisplayName("should not affect other members when removing one")
    void shouldNotAffectOtherMembers() {
      guild.addMember(new GuildMember("avatar-2", "Nick2", MEMBER_ROLE));
      guild.addMember(new GuildMember("avatar-3", "Nick3", MEMBER_ROLE));

      guild.removeMember("avatar-2");

      assertThat(guild.getMembers()).hasSize(2);
      assertThat(guild.getMembers()).extracting(GuildMember::getId).contains(LEADER_ID, "avatar-3");
    }

    @Test
    @DisplayName("should do nothing when member id does not exist")
    void shouldDoNothingForUnknownMemberId() {
      guild.removeMember("non-existent-id");

      assertThat(guild.getMembers()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("updateGlobalRank")
  class UpdateGlobalRank {

    @Test
    @DisplayName("should update rank to the given value")
    void shouldUpdateRank() {
      guild.updateGlobalRank(42);

      assertThat(guild.getGlobalRank()).isEqualTo(42);
    }

    @Test
    @DisplayName("should allow updating rank multiple times")
    void shouldAllowMultipleUpdates() {
      guild.updateGlobalRank(10);
      guild.updateGlobalRank(5);

      assertThat(guild.getGlobalRank()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("promoteMember")
  class PromoteMember {

    @Test
    @DisplayName("should change the role of the target member")
    void shouldChangeRoleOfTargetMember() {
      GuildMember member = new GuildMember("avatar-2", "Nick2", MEMBER_ROLE);
      guild.addMember(member);

      guild.promoteMember("avatar-2", OFFICER_ROLE);

      GuildMember promoted =
          guild.getMembers().stream()
              .filter(m -> m.getId().equals("avatar-2"))
              .findFirst()
              .orElseThrow();
      assertThat(promoted.getRole()).isEqualTo(OFFICER_ROLE);
    }

    @Test
    @DisplayName("should not change roles of other members")
    void shouldNotAffectOtherMembersRoles() {
      guild.addMember(new GuildMember("avatar-2", "Nick2", MEMBER_ROLE));

      guild.promoteMember("avatar-2", OFFICER_ROLE);

      assertThat(leader.getRole()).isEqualTo(LEADER_ROLE);
    }

    @Test
    @DisplayName("should do nothing when member id does not exist")
    void shouldDoNothingForUnknownMemberId() {
      guild.promoteMember("non-existent-id", OFFICER_ROLE);

      assertThat(guild.getMembers()).extracting(m -> m.getRole()).containsOnly(LEADER_ROLE);
    }
  }
}
