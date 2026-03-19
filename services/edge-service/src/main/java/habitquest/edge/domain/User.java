package habitquest.edge.domain;

import common.ddd.Aggregate;

public class User implements Aggregate<String> {

  private final String id;
  private final String email;
  private String passwordHash;
  private UserRole role;

  public User(String id, String email, String passwordHash, UserRole role) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public boolean matchesPassword(String candidateHash) {
    return this.passwordHash.equals(candidateHash);
  }

  public void changeRole(UserRole newRole) {
    this.role = newRole;
  }

  @Override
  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UserRole getRole() {
    return role;
  }
}
