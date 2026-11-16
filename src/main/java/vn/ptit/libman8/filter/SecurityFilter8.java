package vn.ptit.libman8.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import vn.ptit.libman8.model.*;

@WebFilter("/*")
public class SecurityFilter8 implements Filter {
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    HttpServletResponse p = (HttpServletResponse) res;
    r.setCharacterEncoding("UTF-8");
    p.setCharacterEncoding("UTF-8");
    p.setHeader("X-Content-Type-Options", "nosniff");
    p.setHeader("X-Frame-Options", "DENY");
    p.setHeader("Cache-Control", "no-store");
    HttpSession s = r.getSession();
    s.setMaxInactiveInterval(1800);
    if (s.getAttribute("csrf") == null) s.setAttribute("csrf", UUID.randomUUID().toString());
    if ("POST".equals(r.getMethod())
        && !Objects.equals(s.getAttribute("csrf"), r.getParameter("csrf"))) {
      p.sendError(403, "Invalid CSRF token. Reload the form and try again.");
      return;
    }
    String path = r.getServletPath();
    if (path.startsWith("/staff")) {
      Object user = s.getAttribute("staff");
      if (!(user instanceof LibraryStaff8)
          || !"LIBRARY_STAFF".equals(((LibraryStaff8) user).getPosition())) {
        p.sendRedirect(r.getContextPath() + "/login");
        return;
      }
    }
    chain.doFilter(r, p);
  }
}
