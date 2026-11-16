package vn.ptit.libman8.service;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.dao.*;
import vn.ptit.libman8.model.*;

public class AuthService8 {
  public LibraryStaff8 login(String username, String password) throws SQLException {
    if (username == null || username.isBlank()) return null;
    username = username.strip();
    try (Connection c = DatabaseConfig8.open()) {
      LibraryStaff8 s = new StaffDAO8(c).findByUsername(username);
      if (s == null
          || password == null
          || password.length() > 72
          || !BCrypt.checkpw(password, s.getPasswordHash())) return null;
      s.setPasswordHash(null);
      return s;
    }
  }
}
