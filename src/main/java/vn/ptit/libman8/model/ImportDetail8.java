package vn.ptit.libman8.model;

import java.math.BigDecimal;
import java.time.*;

public class ImportDetail8 {
  private int id;
  private Document8 document;
  private int quantity;
  private BigDecimal unitPrice = BigDecimal.ZERO;

  public ImportDetail8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public Document8 getDocument() {
    return document;
  }

  public void setDocument(Document8 value) {
    this.document = value;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int value) {
    this.quantity = value;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal value) {
    this.unitPrice = value;
  }

  public BigDecimal getSubtotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
