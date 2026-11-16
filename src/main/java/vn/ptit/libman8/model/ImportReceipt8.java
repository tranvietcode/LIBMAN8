package vn.ptit.libman8.model;

import java.math.BigDecimal;
import java.time.*;
import java.util.Arrays;

public class ImportReceipt8 {
  private int id;
  private String receiptCode;
  private LocalDate importDate;
  private Supplier8 supplier;
  private Staff8 staff;
  private ImportDetail8[] details = new ImportDetail8[0];

  public ImportReceipt8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getReceiptCode() {
    return receiptCode;
  }

  public void setReceiptCode(String value) {
    this.receiptCode = value;
  }

  public LocalDate getImportDate() {
    return importDate;
  }

  public void setImportDate(LocalDate value) {
    this.importDate = value;
  }

  public Supplier8 getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier8 value) {
    this.supplier = value;
  }

  public Staff8 getStaff() {
    return staff;
  }

  public void setStaff(Staff8 value) {
    this.staff = value;
  }

  public ImportDetail8[] getDetails() {
    return details;
  }

  public void setDetails(ImportDetail8[] value) {
    this.details = value;
  }

  public BigDecimal getTotalAmount() {
    return details == null
        ? BigDecimal.ZERO
        : Arrays.stream(details)
            .map(ImportDetail8::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
