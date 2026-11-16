package vn.ptit.libman8.model;

import java.time.*;

public class Reader8 extends User8 {
  private String studentCode;
  private String academicYear;
  private ReaderCard8 card;

  public Reader8() {}

  public String getStudentCode() {
    return studentCode;
  }

  public void setStudentCode(String value) {
    this.studentCode = value;
  }

  public String getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(String value) {
    this.academicYear = value;
  }

  public ReaderCard8 getCard() {
    return card;
  }

  public void setCard(ReaderCard8 value) {
    this.card = value;
  }
}
