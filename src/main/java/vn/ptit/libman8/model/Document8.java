package vn.ptit.libman8.model;

import java.time.*;

public class Document8 {
  private int id;
  private String documentCode;
  private String title;
  private String documentType;
  private String author;
  private String publisher;
  private int publicationYear;
  private String description;
  private DocumentCopy8[] copies = new DocumentCopy8[0];

  public Document8() {}

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  public String getDocumentCode() {
    return documentCode;
  }

  public void setDocumentCode(String value) {
    this.documentCode = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String value) {
    this.documentType = value;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String value) {
    this.author = value;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String value) {
    this.publisher = value;
  }

  public int getPublicationYear() {
    return publicationYear;
  }

  public void setPublicationYear(int value) {
    this.publicationYear = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public DocumentCopy8[] getCopies() {
    return copies;
  }

  public void setCopies(DocumentCopy8[] value) {
    this.copies = value;
  }
}
