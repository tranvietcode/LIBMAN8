package vn.ptit.libman8.config;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
import java.sql.*;
import java.util.*;

/** Releases JDBC driver resources when Tomcat undeploys this WAR. */
@WebListener
public class AppLifecycle8 implements ServletContextListener {
  public void contextDestroyed(ServletContextEvent event) {
    AbandonedConnectionCleanupThread.checkedShutdown();
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    while (drivers.hasMoreElements()) {
      Driver driver = drivers.nextElement();
      if (driver.getClass().getClassLoader() == getClass().getClassLoader())
        try {
          DriverManager.deregisterDriver(driver);
        } catch (SQLException e) {
          event.getServletContext().log("Cannot deregister JDBC driver", e);
        }
    }
  }
}
