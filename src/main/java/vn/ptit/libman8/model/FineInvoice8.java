package vn.ptit.libman8.model;

import java.time.*;

public class FineInvoice8 {
  private int id;
  private String invoiceCode;
  private LocalDateTime paymentDate;
  private String paymentMethod;

  public FineInvoice8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getInvoiceCode() {
    return invoiceCode;
  }

  public void setInvoiceCode(String value) {
    this.invoiceCode = value;
  }

  public LocalDateTime getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDateTime value) {
    this.paymentDate = value;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String value) {
    this.paymentMethod = value;
  }
}
