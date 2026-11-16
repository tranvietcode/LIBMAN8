package vn.ptit.libman8.dao;

import java.sql.*;

public abstract class DAO8 {
  protected final Connection connection;

  protected DAO8(Connection c) {
    connection = c;
  }

  protected int insertKey(PreparedStatement p) throws SQLException {
    if (p.executeUpdate() != 1) throw new SQLException("Insert failed");
    try (ResultSet r = p.getGeneratedKeys()) {
      if (!r.next()) throw new SQLException("Missing generated id");
      return r.getInt(1);
    }
  }
}
