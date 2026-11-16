package vn.ptit.libman8.model;

import java.time.*;

public class DocumentCopy8 {
  private int id;
  private String copyCode;
  private String condition;
  private String status;
  private ImportDetail8 importDetail;

  public DocumentCopy8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getCopyCode() {
    return copyCode;
  }

  public void setCopyCode(String value) {
    this.copyCode = value;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String value) {
    this.condition = value;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String value) {
    this.status = value;
  }

  public ImportDetail8 getImportDetail() {
    return importDetail;
  }

  public void setImportDetail(ImportDetail8 value) {
    this.importDetail = value;
  }
}
