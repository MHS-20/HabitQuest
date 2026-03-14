package habitquest.guild.domain.guild;

import common.ddd.Aggregate;
import java.util.ArrayList;
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
    return this.members;
  }

  public boolean isLeader(String memberId) {
    return members.stream()
        .filter(member -> member.getId().equals(memberId))
        .anyMatch(member -> member.getRole().roleName().equals("LEADER"));
  }

  public Integer getGlobalRank() {
    return this.globalRank;
  }

  public void addMember(GuildMember member) {
    this.members.add(member);
  }

  public void removeMember(String memberId) {
    this.members.removeIf(member -> member.getId().equals(memberId));
  }

  public void updateGlobalRank(Integer newRank) {
    this.globalRank = newRank;
  }

  public void promoteMember(String memberId, GuildRole newRole) {
    for (GuildMember member : members) {
      if (member.getId().equals(memberId)) {
        member.promoteTo(newRole);
        break;
      }
    }
  }
}
