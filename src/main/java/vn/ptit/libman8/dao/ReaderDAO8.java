package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class ReaderDAO8 extends DAO8 {
  public ReaderDAO8(Connection c) {
    super(c);
  }

  public boolean existsByStudentCode(String code) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT 1 FROM tblReader WHERE studentCode=?")) {
      p.setString(1, code);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public void insert(Reader8 x) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO tblReader(userId,studentCode,academicYear) VALUES(?,?,?)")) {
      p.setInt(1, x.getId());
      p.setString(2, x.getStudentCode());
      p.setString(3, x.getAcademicYear());
      p.executeUpdate();
    }
  }

  public Reader8 findByCardOrStudentCode(String keyword) throws SQLException {
    return find("c.cardCode=? OR r.studentCode=?", keyword, keyword);
  }

  public Reader8 findByCardId(int id) throws SQLException {
    return find("c.id=?", id);
  }

  private Reader8 find(String where, Object... values) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT u.*,r.studentCode,r.academicYear,c.id AS"
                + " cardId,c.cardCode,c.issueDate,c.expiryDate,c.status AS cardStatus FROM tblUser"
                + " u JOIN tblReader r ON r.userId=u.id JOIN tblReaderCard c ON c.readerId=r.userId"
                + " WHERE "
                + where)) {
      for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return null;
        Reader8 x = new Reader8();
        x.setId(r.getInt("id"));
        x.setFullName(r.getString("fullName"));
        x.setDateOfBirth(r.getObject("dateOfBirth", java.time.LocalDate.class));
        x.setAddress(r.getString("address"));
        x.setEmail(r.getString("email"));
        x.setPhone(r.getString("phone"));
        x.setNote(r.getString("note"));
        x.setUsername(r.getString("username"));
        x.setPasswordHash(r.getString("passwordHash"));
        x.setStudentCode(r.getString("studentCode"));
        x.setAcademicYear(r.getString("academicYear"));
        ReaderCard8 c = new ReaderCard8();
        c.setId(r.getInt("cardId"));
        c.setCardCode(r.getString("cardCode"));
        c.setIssueDate(r.getObject("issueDate", java.time.LocalDate.class));
        c.setExpiryDate(r.getObject("expiryDate", java.time.LocalDate.class));
        c.setStatus(r.getString("cardStatus"));
        x.setCard(c);
        return x;
      }
    }
  }
}
