package vn.ptit.libman8.dao;

import java.sql.*;
import java.time.*;
import java.util.*;
import vn.ptit.libman8.model.*;

/** All mutation and cleanup are restricted to UUID-prefixed test fixtures. */
public class FixtureDAO8 extends DAO8 {
  private final String tag;
  private final List<Integer> readers = new ArrayList<>();
  private final List<Integer> loans = new ArrayList<>();
  private int supplierId, documentId, importId, importDetailId;

  public FixtureDAO8(Connection c, String tag) {
    super(c);
    this.tag = tag;
  }

  private int insert(String sql, Object... values) throws SQLException {
    try (PreparedStatement p = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
      return insertKey(p);
    }
  }

  public void init(int staff) throws SQLException {
    supplierId = insert("INSERT INTO tblSupplier(supplierCode,name) VALUES(?,?)", tag, tag);
    documentId =
        insert(
            "INSERT INTO"
                + " tblDocument(documentCode,title,documentType,author,publisher,publicationYear)"
                + " VALUES(?,?,'BOOK','Test','Test',2024)",
            tag,
            "Tài liệu kiểm thử");
    importId =
        insert(
            "INSERT INTO tblImportReceipt(receiptCode,importDate,supplierId,staffId)"
                + " VALUES(?,CURDATE(),?,?)",
            tag,
            supplierId,
            staff);
    importDetailId =
        insert(
            "INSERT INTO tblImportDetail(importReceiptId,documentId,quantity,unitPrice)"
                + " VALUES(?,?,100,10000)",
            importId,
            documentId);
  }

  public void track(Reader8 r) {
    readers.add(r.getId());
  }

  public int loan(Reader8 r, int staff, int daysOverdue) throws SQLException {
    String code = tag + UUID.randomUUID().toString().substring(0, 6);
    int cp =
        insert(
            "INSERT INTO tblDocumentCopy(copyCode,documentId,importDetailId,`condition`,status)"
                + " VALUES(?,?,?,'GOOD','ON_LOAN')",
            code,
            documentId,
            importDetailId);
    int slip =
        insert(
            "INSERT INTO tblLoanSlip(loanCode,loanDate,cardId,staffId) VALUES(?,NOW(),?,?)",
            code,
            r.getCard().getId(),
            staff);
    int ld =
        insert(
            "INSERT INTO tblLoanDetail(loanSlipId,copyId,dueDate,status) VALUES(?,?,?,'ON_LOAN')",
            slip,
            cp,
            LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(daysOverdue));
    loans.add(ld);
    return ld;
  }

  public int scalar(String sql, Object... values) throws SQLException {
    try (PreparedStatement p = connection.prepareStatement(sql)) {
      for (int i = 0; i < values.length; i++) p.setObject(i + 1, values[i]);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }

  public Integer findExistingInvoiceReceiptId(int excludedCardId) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT rs.id FROM tblReturnSlip rs JOIN tblFineInvoice fi ON fi.returnSlipId=rs.id"
                + " WHERE rs.cardId<>? ORDER BY rs.id DESC LIMIT 1")) {
      p.setInt(1, excludedCardId);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? r.getInt(1) : null;
      }
    }
  }

  public String status(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT status FROM tblLoanDetail WHERE id=?")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getString(1);
      }
    }
  }

  public String copyStatus(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT cp.status FROM tblDocumentCopy cp JOIN tblLoanDetail ld ON ld.copyId=cp.id"
                + " WHERE ld.id=?")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getString(1);
      }
    }
  }

  public void changeDueDate(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "UPDATE tblLoanDetail SET dueDate=DATE_SUB(dueDate,INTERVAL 1 DAY) WHERE id=?")) {
      p.setInt(1, id);
      p.executeUpdate();
    }
  }

  private void run(String sql, Object value) throws SQLException {
    try (PreparedStatement p = connection.prepareStatement(sql)) {
      p.setObject(1, value);
      p.executeUpdate();
    }
  }

  public void cleanup() throws SQLException {
    for (int uid : readers) {
      run(
          "DELETE fi FROM tblFineInvoice fi JOIN tblReturnSlip rs ON rs.id=fi.returnSlipId JOIN"
              + " tblReaderCard c ON c.id=rs.cardId WHERE c.readerId=?",
          uid);
      run(
          "DELETE rd FROM tblReturnDetail rd JOIN tblReturnSlip rs ON rs.id=rd.returnSlipId JOIN"
              + " tblReaderCard c ON c.id=rs.cardId WHERE c.readerId=?",
          uid);
      run(
          "DELETE rs FROM tblReturnSlip rs JOIN tblReaderCard c ON c.id=rs.cardId WHERE"
              + " c.readerId=?",
          uid);
      run(
          "DELETE ld FROM tblLoanDetail ld JOIN tblLoanSlip ls ON ls.id=ld.loanSlipId JOIN"
              + " tblReaderCard c ON c.id=ls.cardId WHERE c.readerId=?",
          uid);
      run(
          "DELETE ls FROM tblLoanSlip ls JOIN tblReaderCard c ON c.id=ls.cardId WHERE c.readerId=?",
          uid);
      run("DELETE FROM tblReaderCard WHERE readerId=?", uid);
      run("DELETE FROM tblReader WHERE userId=?", uid);
      run("DELETE FROM tblUser WHERE id=?", uid);
    }
    run("DELETE FROM tblDocumentCopy WHERE importDetailId=?", importDetailId);
    run("DELETE FROM tblImportDetail WHERE id=?", importDetailId);
    run("DELETE FROM tblImportReceipt WHERE id=?", importId);
    run("DELETE FROM tblDocument WHERE id=?", documentId);
    run("DELETE FROM tblSupplier WHERE id=?", supplierId);
  }

  public String snapshot() throws SQLException {
    StringBuilder out = new StringBuilder();
    for (String table :
        new String[] {
          "tblUser",
          "tblReader",
          "tblStaff",
          "tblReaderCard",
          "tblSupplier",
          "tblDocument",
          "tblImportReceipt",
          "tblImportDetail",
          "tblDocumentCopy",
          "tblLoanSlip",
          "tblLoanDetail",
          "tblReturnSlip",
          "tblReturnDetail",
          "tblFineInvoice"
        }) {
      try (PreparedStatement p =
              connection.prepareStatement("SELECT * FROM " + table + " ORDER BY 1");
          ResultSet r = p.executeQuery()) {
        out.append(table);
        while (r.next()) {
          for (int i = 1; i <= r.getMetaData().getColumnCount(); i++)
            out.append('|').append(r.getString(i));
          out.append('\n');
        }
      }
    }
    return out.toString();
  }
}
