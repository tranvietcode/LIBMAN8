package vn.ptit.libman8.model;

import java.time.*;

public abstract class User8 {
  private int id;
  private String username;
  private String passwordHash;
  private String fullName;
  private LocalDate dateOfBirth;
  private String address;
  private String email;
  private String phone;
  private String note;

  public User8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    this.username = value;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String value) {
    this.passwordHash = value;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String value) {
    this.fullName = value;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate value) {
    this.dateOfBirth = value;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    this.address = value;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String value) {
    this.phone = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    this.note = value;
  }
}
