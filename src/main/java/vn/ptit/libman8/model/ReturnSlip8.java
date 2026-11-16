package vn.ptit.libman8.model;

import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;

public class ReturnSlip8 {
  private int id;
  private String returnCode;
  private LocalDateTime returnDate;
  private ReaderCard8 card;
  private Staff8 staff;
  private ReturnDetail8[] details = new ReturnDetail8[0];
  private FineInvoice8 fineInvoice;

  public ReturnSlip8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getReturnCode() {
    return returnCode;
  }

  public void setReturnCode(String value) {
    this.returnCode = value;
  }

  public LocalDateTime getReturnDate() {
    return returnDate;
  }

  public void setReturnDate(LocalDateTime value) {
    this.returnDate = value;
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

  public ReturnDetail8[] getDetails() {
    return details;
  }

  public void setDetails(ReturnDetail8[] value) {
    this.details = value;
  }

  public FineInvoice8 getFineInvoice() {
    return fineInvoice;
  }

  public void setFineInvoice(FineInvoice8 value) {
    this.fineInvoice = value;
  }

  public BigDecimal getTotalFine() {
    return details == null
        ? BigDecimal.ZERO
        : Arrays.stream(details)
            .map(ReturnDetail8::getTotalFine)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
