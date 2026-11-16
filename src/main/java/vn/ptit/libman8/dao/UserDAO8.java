package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class UserDAO8 extends DAO8 {
  public UserDAO8(Connection c) {
    super(c);
  }

  public boolean existsByUsername(String username) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT 1 FROM tblUser WHERE username=?")) {
      p.setString(1, username);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public boolean existsByEmail(String email) throws SQLException {
    try (PreparedStatement p = connection.prepareStatement("SELECT 1 FROM tblUser WHERE email=?")) {
      p.setString(1, email);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public int insert(User8 u) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO"
                + " tblUser(username,passwordHash,fullName,dateOfBirth,address,email,phone,note)"
                + " VALUES(?,?,?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, u.getUsername());
      p.setString(2, u.getPasswordHash());
      p.setString(3, u.getFullName());
      p.setObject(4, u.getDateOfBirth());
      p.setString(5, u.getAddress());
      p.setString(6, u.getEmail());
      p.setString(7, u.getPhone());
      p.setString(8, u.getNote());
      int id = insertKey(p);
      u.setId(id);
      return id;
    }
  }
}
