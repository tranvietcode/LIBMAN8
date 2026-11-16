package vn.ptit.libman8.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet(urlPatterns = {"/home", "/staff/dashboard"})
public class HomeServlet8 extends HttpServlet {
  protected void doGet(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    r.getRequestDispatcher(
            "/WEB-INF/views/"
                + (r.getServletPath().startsWith("/staff")
                    ? "libraryStaff/libraryStaffMain8.jsp"
                    : "reader/readerMain8.jsp"))
        .forward(r, p);
  }
}
