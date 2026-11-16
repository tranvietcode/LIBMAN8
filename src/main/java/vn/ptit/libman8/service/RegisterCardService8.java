package vn.ptit.libman8.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.dao.*;
import vn.ptit.libman8.model.*;

public class RegisterCardService8 {
  protected Connection openConnection() throws SQLException {
    return DatabaseConfig8.open();
  }

  public Map<String, String> validate(Reader8 r) {
    Map<String, String> e = new LinkedHashMap<>();
    if (r.getUsername() != null) {
      if (!r.getUsername().matches("[A-Za-z0-9_.-]{3,80}"))
        e.put(
            "username", "Username must contain 3–80 letters, digits, dots, underscores or hyphens");
      if (r.getPasswordHash() == null || !r.getPasswordHash().matches("\\$2[aby]\\$\\d{2}\\$.{53}"))
        e.put("password", "Enter a valid password");
    }
    if (r.getCard() != null
        && (r.getCard().getCardCode() == null
            || !r.getCard().getCardCode().matches("[A-Za-z0-9_-]{3,50}")))
      e.put("cardCode", "Card code must contain 3–50 letters, digits, underscores or hyphens");
    if (r.getFullName() == null || r.getFullName().isBlank() || r.getFullName().length() > 150)
      e.put("fullName", "Full name is required and must not exceed 150 characters");
    if (r.getStudentCode() == null || !r.getStudentCode().matches("[A-Za-z0-9_-]{3,30}"))
      e.put(
          "studentCode", "Student code must contain 3–30 letters, digits, underscores or hyphens");
    if (r.getDateOfBirth() == null
        || !r.getDateOfBirth().isBefore(PolicyConfig8.now().toLocalDate()))
      e.put("dateOfBirth", "Enter a valid date of birth before today");
    if (r.getEmail() == null
        || r.getEmail().length() > 150
        || !r.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      e.put("email", "Enter a valid email address");
    if (r.getPhone() == null || !r.getPhone().matches("[+0-9 ()-]{8,20}"))
      e.put("phone", "Phone number must contain 8–20 characters");
    if (r.getAddress() != null && r.getAddress().length() > 255)
      e.put("address", "Address must not exceed 255 characters");
    if (r.getAcademicYear() != null && r.getAcademicYear().length() > 30)
      e.put("academicYear", "Academic year must not exceed 30 characters");
    return e;
  }

  /** Only the BCrypt hash is retained in a registration draft. */
  public Map<String, String> prepareCredentials(Reader8 reader, String password) {
    Map<String, String> errors = new LinkedHashMap<>();
    reader.setPasswordHash(null);
    if (password == null
        || password.length() < 8
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
      errors.put(
          "password", "Password must contain at least 8 characters and at most 72 UTF-8 bytes");
    } else {
      reader.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
    }
    return errors;
  }

  public Map<String, String> checkDuplicate(Reader8 r) throws SQLException {
    try (Connection c = openConnection()) {
      return duplicates(c, r);
    }
  }

  private Map<String, String> duplicates(Connection c, Reader8 r) throws SQLException {
    Map<String, String> e = new LinkedHashMap<>();
    if (r.getUsername() != null && new UserDAO8(c).existsByUsername(r.getUsername()))
      e.put("username", "This username is already registered");
    if (r.getCard() != null && new ReaderCardDAO8(c).existsByCardCode(r.getCard().getCardCode()))
      e.put("cardCode", "This card code is already registered");
    if (new UserDAO8(c).existsByEmail(r.getEmail()))
      e.put("email", "This email is already registered");
    if (new ReaderDAO8(c).existsByStudentCode(r.getStudentCode()))
      e.put("studentCode", "This student code is already registered");
    return e;
  }

  public Reader8 register(Reader8 r) throws SQLException {
    if (!validate(r).isEmpty()) throw new IllegalArgumentException("Invalid registration details");
    try (Connection c = openConnection()) {
      c.setAutoCommit(false);
      try {
        if (!duplicates(c, r).isEmpty())
          throw new IllegalArgumentException(
              "A username, email, student code or card code is already registered");
        new UserDAO8(c).insert(r);
        new ReaderDAO8(c).insert(r);
        ReaderCard8 card = r.getCard() == null ? new ReaderCard8() : r.getCard();
        if (card.getCardCode() == null) card.setCardCode(PolicyConfig8.code("CARD-"));
        card.setIssueDate(PolicyConfig8.now().toLocalDate());
        card.setExpiryDate(card.getIssueDate().plusMonths(12));
        card.setStatus("ACTIVE");
        new ReaderCardDAO8(c).insert(card, r.getId());
        r.setCard(card);
        c.commit();
        return r;
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }
}
