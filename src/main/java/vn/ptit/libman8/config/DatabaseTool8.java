package vn.ptit.libman8.config;

import java.nio.file.*;
import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;
import vn.ptit.libman8.dao.*;

public final class DatabaseTool8 {
  private DatabaseTool8() {}

  public static void main(String[] args) throws Exception {
    if (args.length == 1 && args[0].equals("hash-demo")) {
      System.out.println(BCrypt.hashpw("ThuThu8!Demo", BCrypt.gensalt(12)));
      return;
    }
    Properties p = DatabaseConfig8.settings();
    Class.forName("com.mysql.cj.jdbc.Driver");
    String url = p.getProperty("db.url").replace("/libman8?", "/?");
    try (Connection c =
        DriverManager.getConnection(url, p.getProperty("db.user"), p.getProperty("db.password"))) {
      ScriptDAO8 dao = new ScriptDAO8(c);
      dao.probe();
      for (String file : args) {
        dao.run(Files.readString(Path.of(file)));
        System.out.println("Applied " + file);
      }
    }
  }
}
