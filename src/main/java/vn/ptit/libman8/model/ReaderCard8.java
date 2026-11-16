package vn.ptit.libman8.model;

import java.time.*;

public class ReaderCard8 {
  private int id;
  private String cardCode;
  private LocalDate issueDate;
  private LocalDate expiryDate;
  private String status;

  public ReaderCard8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getCardCode() {
    return cardCode;
  }

  public void setCardCode(String value) {
    this.cardCode = value;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public void setIssueDate(LocalDate value) {
    this.issueDate = value;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate value) {
    this.expiryDate = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }
}
