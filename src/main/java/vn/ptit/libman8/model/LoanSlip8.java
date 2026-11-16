package vn.ptit.libman8.model;

import java.time.*;

public class LoanSlip8 {
  private int id;
  private String loanCode;
  private LocalDateTime loanDate;
  private ReaderCard8 card;
  private Staff8 staff;
  private LoanDetail8[] details = new LoanDetail8[0];

  public LoanSlip8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getLoanCode() {
    return loanCode;
  }

  public void setLoanCode(String value) {
    this.loanCode = value;
  }

  public LocalDateTime getLoanDate() {
    return loanDate;
  }

  public void setLoanDate(LocalDateTime value) {
    this.loanDate = value;
  }

  public ReaderCard8 getCard() {
    return card;
  }

  public void setCard(ReaderCard8 value) {
    this.card = value;
  }

  public Staff8 getStaff() {
    return staff;
  }

  public void setStaff(Staff8 value) {
    this.staff = value;
  }

  public LoanDetail8[] getDetails() {
    return details;
  }

  public void setDetails(LoanDetail8[] value) {
    this.details = value;
  }
}
