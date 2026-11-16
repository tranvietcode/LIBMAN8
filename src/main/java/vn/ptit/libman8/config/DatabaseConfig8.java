package vn.ptit.libman8.config;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public final class DatabaseConfig8 {
  private DatabaseConfig8() {}

  public static Properties settings() throws IOException {
    Properties p = new Properties();
    String f = System.getenv("LIBMAN_CONFIG");
    if (f == null) f = System.getProperty("libman.config", "config/db.local.properties");
    Path path = Path.of(f);
    if (Files.exists(path))
      try (InputStream in = Files.newInputStream(path)) {
        p.load(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
      }
    for (String key : new String[] {"url", "user", "password"}) {
      String v = System.getenv("LIBMAN_DB_" + key.toUpperCase());
      if (v != null) p.setProperty("db." + key, v);
    }
    return p;
  }

  public static Connection open() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Properties p = settings();
      if (!p.containsKey("db.password"))
        throw new SQLException("Local MySQL configuration is missing");
      return DriverManager.getConnection(
          p.getProperty(
              "db.url", "jdbc:mysql://localhost:3306/libman8?serverTimezone=Asia/Ho_Chi_Minh"),
          p.getProperty("db.user", "root"),
          p.getProperty("db.password"));
    } catch (IOException | ClassNotFoundException e) {
      throw new SQLException("Cannot load the database configuration or JDBC driver", e);
    }
  }
}
