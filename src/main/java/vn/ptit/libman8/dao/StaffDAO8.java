package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class StaffDAO8 extends DAO8 {
  public StaffDAO8(Connection c) {
    super(c);
  }

  public LibraryStaff8 findByUsername(String username) throws SQLException {
    return (LibraryStaff8) find("u.username=? AND s.position='LIBRARY_STAFF'", username);
  }

  public Staff8 findById(int id) throws SQLException {
    return find("u.id=?", id);
  }

  private Staff8 find(String where, Object value) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT u.*,s.staffCode,s.position FROM tblUser u JOIN tblStaff s ON s.userId=u.id"
                + " WHERE "
                + where)) {
      p.setObject(1, value);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return null;
        String position = r.getString("position");
        Staff8 s;
        if ("LIBRARY_STAFF".equals(position)) s = new LibraryStaff8();
        else if ("MANAGEMENT_STAFF".equals(position)) s = new ManagementStaff8();
        else throw new SQLException("Unsupported staff position");
        s.setId(r.getInt("id"));
        s.setUsername(r.getString("username"));
        s.setPasswordHash(r.getString("passwordHash"));
        s.setFullName(r.getString("fullName"));
        s.setDateOfBirth(r.getObject("dateOfBirth", java.time.LocalDate.class));
        s.setAddress(r.getString("address"));
        s.setEmail(r.getString("email"));
        s.setPhone(r.getString("phone"));
        s.setNote(r.getString("note"));
        s.setStaffCode(r.getString("staffCode"));
        s.setPosition(r.getString("position"));
        return s;
      }
    }
  }

  public boolean isLibraryStaff(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT 1 FROM tblStaff WHERE userId=? AND position='LIBRARY_STAFF'")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }
}
