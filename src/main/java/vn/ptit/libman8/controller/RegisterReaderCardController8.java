package vn.ptit.libman8.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import vn.ptit.libman8.model.*;
import vn.ptit.libman8.service.*;

@WebServlet("/register")
public class RegisterReaderCardController8 extends HttpServlet {
  private void view(HttpServletRequest r, HttpServletResponse p, String name)
      throws ServletException, IOException {
    r.getRequestDispatcher("/WEB-INF/views/reader/" + name + ".jsp").forward(r, p);
  }

  protected void doGet(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    HttpSession s = r.getSession();
    String stage = r.getParameter("stage");
    if ("result".equals(stage) && s.getAttribute("registrationResult") != null) {
      r.setAttribute("reader", s.getAttribute("registrationResult"));
      view(r, p, "RegistrationResultPage8");
      return;
    }
    r.setAttribute("reader", s.getAttribute("registrationDraft"));
    view(
        r,
        p,
        "confirm".equals(stage) && s.getAttribute("registrationDraft") != null
            ? "ConfirmCardPage8"
            : "registerReaderCard8");
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    HttpSession s = r.getSession();
    synchronized (s) {
      String action = r.getParameter("action");
      RegisterCardService8 service = new RegisterCardService8();
      if ("cancel".equals(action)) {
        s.removeAttribute("registrationDraft");
        p.sendRedirect(r.getContextPath() + "/home");
        return;
      }
      if ("edit".equals(action)) {
        p.sendRedirect(r.getContextPath() + "/register");
        return;
      }
      Reader8 reader = (Reader8) s.getAttribute("registrationDraft");
      try {
        if ("confirm".equals(action)) {
          if (reader == null) {
            p.sendRedirect(r.getContextPath() + "/register?stage=result");
            return;
          }
          service.register(reader);
          reader.setPasswordHash(null);
          s.setAttribute("registrationResult", reader);
          s.removeAttribute("registrationDraft");
          p.sendRedirect(r.getContextPath() + "/register?stage=result");
          return;
        }
        if (!"continue".equals(action)) {
          p.sendError(400);
          return;
        }
        reader = new Reader8();
        reader.setUsername(value(r, "username"));
        ReaderCard8 card = new ReaderCard8();
        card.setCardCode(value(r, "cardCode").toUpperCase(Locale.ROOT));
        reader.setCard(card);
        reader.setFullName(value(r, "fullName"));
        reader.setStudentCode(value(r, "studentCode").toUpperCase(Locale.ROOT));
        reader.setEmail(value(r, "email").toLowerCase(Locale.ROOT));
        reader.setPhone(value(r, "phone"));
        reader.setAddress(value(r, "address"));
        reader.setAcademicYear(value(r, "academicYear"));
        try {
          reader.setDateOfBirth(LocalDate.parse(value(r, "dateOfBirth")));
        } catch (java.time.format.DateTimeParseException ignored) {
        }
        Map<String, String> credentialErrors =
            service.prepareCredentials(reader, r.getParameter("password"));
        Map<String, String> errors = service.validate(reader);
        errors.putAll(credentialErrors);
        errors.putAll(service.checkDuplicate(reader));
        s.setAttribute("registrationDraft", reader);
        r.setAttribute("reader", reader);
        r.setAttribute("dateInput", value(r, "dateOfBirth"));
        if (!errors.isEmpty()) {
          reader.setPasswordHash(null);
          r.setAttribute("errors", errors);
          view(r, p, "registerReaderCard8");
          return;
        }
        p.sendRedirect(r.getContextPath() + "/register?stage=confirm");
      } catch (SQLException e) {
        log("Registration database failure", e);
        if (e.getSQLState() != null && e.getSQLState().startsWith("23"))
          r.setAttribute(
              "error",
              "A username, email, student code or card code is already registered. Please update"
                  + " your details.");
        else
          r.setAttribute(
              "error",
              "Could not save the registration. No changes were saved. Check MySQL and try again.");
        r.setAttribute("reader", reader);
        attachFieldErrors(r, service, reader);
        view(r, p, "registerReaderCard8");
      } catch (IllegalArgumentException e) {
        r.setAttribute("error", e.getMessage());
        r.setAttribute("reader", reader);
        attachFieldErrors(r, service, reader);
        view(r, p, "registerReaderCard8");
      }
    }
  }

  private void attachFieldErrors(
      HttpServletRequest r, RegisterCardService8 service, Reader8 reader) {
    if (reader == null) return;
    Map<String, String> errors = service.validate(reader);
    try {
      errors.putAll(service.checkDuplicate(reader));
    } catch (SQLException e) {
      log("Cannot recheck duplicate fields after failed registration", e);
    }
    r.setAttribute("errors", errors);
  }

  private String value(HttpServletRequest r, String name) {
    String v = r.getParameter(name);
    return v == null ? "" : v.trim();
  }
}
