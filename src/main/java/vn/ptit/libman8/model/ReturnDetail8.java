package vn.ptit.libman8.model;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class ReturnDetail8 {
  private int id;
  private LoanDetail8 loanDetail;
  private String returnCondition;
  private BigDecimal dailyLateFee = BigDecimal.ZERO;
  private BigDecimal damageFine = BigDecimal.ZERO;
  private String note;
  private LocalDateTime returnDate;

  public ReturnDetail8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public LoanDetail8 getLoanDetail() {
    return loanDetail;
  }

  public void setLoanDetail(LoanDetail8 value) {
    this.loanDetail = value;
  }

  public String getReturnCondition() {
    return returnCondition;
  }

  public void setReturnCondition(String value) {
    this.returnCondition = value;
  }

  public BigDecimal getDailyLateFee() {
    return dailyLateFee;
  }

  public void setDailyLateFee(BigDecimal value) {
    this.dailyLateFee = value;
  }

  public BigDecimal getDamageFine() {
    return damageFine;
  }

  public void setDamageFine(BigDecimal value) {
    this.damageFine = value;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String value) {
    this.note = value;
  }

  public LocalDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDateTime value) {
    this.returnDate = value;
  }

  public long getOverdueDays() {
    return Math.max(0, ChronoUnit.DAYS.between(loanDetail.getDueDate(), returnDate.toLocalDate()));
  }

  public BigDecimal getLateFine() {
    return dailyLateFee.multiply(BigDecimal.valueOf(getOverdueDays()));
  }

  public BigDecimal getTotalFine() {
    return getLateFine().add(damageFine);
  }
}
