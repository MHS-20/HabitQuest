package habitquest.guild.domain.guild;

import common.ddd.Aggregate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Guild implements Aggregate<String> {
  private final String id;
  private String name;
  private List<GuildMember> members;
  private Integer globalRank;

  public Guild(String id, String name, GuildMember leader) {
    this.id = id;
    this.name = name;
    this.members = new ArrayList<>();
    this.members.add(leader);
    this.globalRank = 0;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public List<GuildMember> getMembers() {
    return Collections.unmodifiableList(members);
  }

  public boolean isLeader(String memberId) {
    return members.stream()
        .filter(member -> member.getId().equals(memberId))
        .anyMatch(member -> member.getRole() == GuildRole.LEADER);
  }

  public Integer getGlobalRank() {
    return this.globalRank;
  }

  public void addMember(GuildMember member) {
    this.members.add(member);
  }

  public void leaveGuild(String memberId) {
    this.members.removeIf(member -> member.getId().equals(memberId));
  }

  private void removeMember(String memberId) {
    this.members.removeIf(member -> member.getId().equals(memberId));
  }

  public void removeMember(String requestorId, String targetMemberId) {
    requireLeader(requestorId, "removeMember");
    removeMember(targetMemberId);
  }

  public void updateGlobalRank(Integer newRank) {
    this.globalRank = newRank;
  }

  private void promoteMember(String memberId, GuildRole newRole) {
    members.stream()
        .filter(m -> m.getId().equals(memberId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId))
        .promoteTo(newRole);
  }

  public void promoteMember(String requestorId, String targetMemberId, GuildRole newRole) {
    requireLeader(requestorId, "promoteMember");
    promoteMember(targetMemberId, newRole);
  }

  private void requireLeader(String requestorId, String operation) {
    if (!isLeader(requestorId)) {
      throw new UnauthorizedGuildOperationException(requestorId, operation);
    }
  }
}
