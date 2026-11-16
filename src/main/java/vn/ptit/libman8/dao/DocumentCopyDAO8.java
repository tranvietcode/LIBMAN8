package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class DocumentCopyDAO8 extends DAO8 {
  public DocumentCopyDAO8(Connection c) {
    super(c);
  }

  public void updateConditionAndStatus(DocumentCopy8 x) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "UPDATE tblDocumentCopy SET " + "`condition`" + "=?,status=? WHERE id=?")) {
      p.setString(1, x.getCondition());
      p.setString(2, x.getStatus());
      p.setInt(3, x.getId());
      if (p.executeUpdate() != 1) throw new SQLException("Copy missing");
    }
  }
}
