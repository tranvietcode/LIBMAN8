package vn.ptit.libman8.model;

import java.time.*;

public class LoanDetail8 {
  private int id;
  private DocumentCopy8 copy;
  private LocalDate dueDate;
  private String status;
  private int cardId;

  public LoanDetail8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public DocumentCopy8 getCopy() {
    return copy;
  }

  public void setCopy(DocumentCopy8 value) {
    this.copy = value;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate value) {
    this.dueDate = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public int getCardId() {
    return cardId;
  }

  public void setCardId(int value) {
    this.cardId = value;
  }
}
