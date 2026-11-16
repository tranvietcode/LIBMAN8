package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class ReaderCardDAO8 extends DAO8 {
  public ReaderCardDAO8(Connection c) {
    super(c);
  }

  public boolean existsByCardCode(String cardCode) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT 1 FROM tblReaderCard WHERE cardCode=?")) {
      p.setString(1, cardCode);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  public void insert(ReaderCard8 x, int readerId) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO tblReaderCard(cardCode,readerId,issueDate,expiryDate,status)"
                + " VALUES(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, x.getCardCode());
      p.setInt(2, readerId);
      p.setObject(3, x.getIssueDate());
      p.setObject(4, x.getExpiryDate());
      p.setString(5, x.getStatus());
      x.setId(insertKey(p));
    }
  }
}
