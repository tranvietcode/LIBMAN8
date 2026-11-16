package vn.ptit.libman8.dao;

import java.sql.*;
import java.util.*;
import vn.ptit.libman8.model.*;

public class LoanDetailDAO8 extends DAO8 {
  public LoanDetailDAO8(Connection c) {
    super(c);
  }

  private static final String SELECT =
      "SELECT ld.id,ld.dueDate,ld.status,ls.cardId,cp.id AS"
          + " copyId,cp.copyCode,cp.`condition`,cp.status AS copyStatus,cp.documentId AS"
          + " copyDocumentId, im.id AS importDetailId,im.quantity,im.unitPrice,d.id AS documentId,"
          + " d.documentCode,d.title,d.documentType,d.author,d.publisher,d.publicationYear,d.description"
          + " FROM tblLoanDetail ld JOIN tblLoanSlip ls ON ls.id=ld.loanSlipId JOIN tblDocumentCopy"
          + " cp ON cp.id=ld.copyId JOIN tblImportDetail im ON im.id=cp.importDetailId JOIN"
          + " tblDocument d ON d.id=im.documentId ";

  public LoanDetail8[] findForUpdate(int[] ids) throws SQLException {
    if (ids.length == 0) return new LoanDetail8[0];
    String marks = String.join(",", Collections.nCopies(ids.length, "?"));
    try (PreparedStatement p =
        connection.prepareStatement(
            SELECT + "WHERE ld.id IN (" + marks + ") ORDER BY ld.id FOR UPDATE")) {
      int[] sorted = ids.clone();
      Arrays.sort(sorted);
      for (int i = 0; i < sorted.length; i++) p.setInt(i + 1, sorted[i]);
      return read(p);
    }
  }

  public LoanDetail8[] findOutstanding(int cardId) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            SELECT + "WHERE ls.cardId=? AND ld.status='ON_LOAN' ORDER BY ld.id")) {
      p.setInt(1, cardId);
      return read(p);
    }
  }

  public LoanDetail8 findById(int id) throws SQLException {
    try (PreparedStatement p = connection.prepareStatement(SELECT + "WHERE ld.id=?")) {
      p.setInt(1, id);
      LoanDetail8[] a = read(p);
      return a.length == 0 ? null : a[0];
    }
  }

  private LoanDetail8[] read(PreparedStatement p) throws SQLException {
    List<LoanDetail8> list = new ArrayList<>();
    try (ResultSet r = p.executeQuery()) {
      while (r.next()) {
        if (r.getInt("copyDocumentId") != r.getInt("documentId"))
          throw new SQLException("Document copy and import detail refer to different documents");
        LoanDetail8 x = new LoanDetail8();
        x.setId(r.getInt("id"));
        x.setDueDate(r.getObject("dueDate", java.time.LocalDate.class));
        x.setStatus(r.getString("status"));
        x.setCardId(r.getInt("cardId"));
        Document8 d = new Document8();
        d.setId(r.getInt("documentId"));
        d.setDocumentCode(r.getString("documentCode"));
        d.setTitle(r.getString("title"));
        d.setDocumentType(r.getString("documentType"));
        d.setAuthor(r.getString("author"));
        d.setPublisher(r.getString("publisher"));
        d.setPublicationYear(r.getInt("publicationYear"));
        d.setDescription(r.getString("description"));
        ImportDetail8 importDetail = new ImportDetail8();
        importDetail.setId(r.getInt("importDetailId"));
        importDetail.setDocument(d);
        importDetail.setQuantity(r.getInt("quantity"));
        importDetail.setUnitPrice(r.getBigDecimal("unitPrice"));
        DocumentCopy8 cp = new DocumentCopy8();
        cp.setId(r.getInt("copyId"));
        cp.setCopyCode(r.getString("copyCode"));
        cp.setCondition(r.getString("condition"));
        cp.setStatus(r.getString("copyStatus"));
        cp.setImportDetail(importDetail);
        x.setCopy(cp);
        list.add(x);
      }
    }
    return list.toArray(new LoanDetail8[0]);
  }

  public void markReturned(int[] ids) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "UPDATE tblLoanDetail SET status='RETURNED' WHERE id=? AND status='ON_LOAN'")) {
      for (int id : ids) {
        p.setInt(1, id);
        if (p.executeUpdate() != 1) throw new SQLException("Loan changed concurrently");
      }
    }
  }
}
