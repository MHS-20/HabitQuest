package habitquest.guild.domain.guild;

import common.ddd.Aggregate;
import java.util.List;

public class Guild implements Aggregate<String> {
  private final String id;
  private List<GuildMember> members;
  private Integer globalRank;

  public Guild(String id, GuildMember leader) {
    this.id = id;
    this.members.add(leader);
  }

  public String getId() {
    return this.getId();
  }
}
