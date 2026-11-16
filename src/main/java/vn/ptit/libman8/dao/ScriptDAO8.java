package vn.ptit.libman8.dao;

import java.sql.*;

/** Local CLI bootstrap only; never exposed by the web app. */
public class ScriptDAO8 extends DAO8 {
  public ScriptDAO8(Connection c) {
    super(c);
  }

  public void run(String sql) throws SQLException {
    for (String statement : sql.split(";")) {
      if (!statement.isBlank())
        try (PreparedStatement p = connection.prepareStatement(statement)) {
          p.execute();
        }
    }
  }

  public void probe() throws SQLException {
    try (PreparedStatement p =
            connection.prepareStatement(
                "SELECT VERSION(), (SELECT COUNT(*) FROM information_schema.schemata WHERE"
                    + " schema_name='libman8')");
        ResultSet r = p.executeQuery()) {
      r.next();
      System.out.println("MySQL=" + r.getString(1) + "; libman8 exists=" + r.getInt(2));
    }
  }
}
