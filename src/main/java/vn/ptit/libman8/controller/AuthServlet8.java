package vn.ptit.libman8.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import vn.ptit.libman8.model.*;
import vn.ptit.libman8.service.*;

@WebServlet(urlPatterns = {"/login", "/logout"})
public class AuthServlet8 extends HttpServlet {
  protected void doGet(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    if (r.getServletPath().equals("/logout")) {
      p.sendError(405);
      return;
    }
    r.getRequestDispatcher("/WEB-INF/views/common/LoginPage8.jsp").forward(r, p);
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    if (r.getServletPath().equals("/logout")) {
      r.getSession().invalidate();
      p.sendRedirect(r.getContextPath() + "/home");
      return;
    }
    String username = r.getParameter("username");
    String password = r.getParameter("password");
    r.setAttribute("username", username == null ? "" : username.strip());
    try {
      LibraryStaff8 s = new AuthService8().login(username, password);
      if (s != null) {
        r.changeSessionId();
        r.getSession().setAttribute("staff", s);
        r.getSession().removeAttribute("returnDraft");
        p.sendRedirect(r.getContextPath() + "/staff/dashboard");
        return;
      }
      if (password != null && !password.equals(password.strip())) {
        r.setAttribute(
            "error",
            "The password has leading or trailing whitespace. Re-enter it without extra spaces.");
      } else {
        r.setAttribute("error", "Incorrect username or password");
      }
    } catch (SQLException e) {
      log("Login database failure", e);
      r.setAttribute("error", "Cannot connect to the database. Check the local configuration.");
    }
    doGet(r, p);
  }
}
