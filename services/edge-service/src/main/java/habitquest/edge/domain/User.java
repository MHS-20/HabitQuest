package habitquest.edge.domain;

import common.ddd.Aggregate;

public class User implements Aggregate<String> {

  private final String avatarId;
  private final String email;
  private String name;
  private String passwordHash;

  public User(String avatarId, String name, String email, String passwordHash) {
    this.avatarId = avatarId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
  }

  public boolean matchesPassword(String candidateHash) {
    return this.passwordHash.equals(candidateHash);
  }

  @Override
  public String getId() {
    return avatarId;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getName() {
    return name;
  }
}
