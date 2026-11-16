package vn.ptit.libman8.model;

import java.time.*;

public abstract class Staff8 extends User8 {
  private String staffCode;
  private String position;

  public Staff8() {}

  public String getStaffCode() {
    return staffCode;
  }

  public void setStaffCode(String value) {
    this.staffCode = value;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String value) {
    this.position = value;
  }
}
