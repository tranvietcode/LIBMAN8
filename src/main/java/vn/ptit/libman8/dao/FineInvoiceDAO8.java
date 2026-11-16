package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class FineInvoiceDAO8 extends DAO8 {
  public FineInvoiceDAO8(Connection c) {
    super(c);
  }

  public void insert(FineInvoice8 x, int returnSlipId) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO tblFineInvoice(invoiceCode,returnSlipId,paymentDate,paymentMethod)"
                + " VALUES(?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, x.getInvoiceCode());
      p.setInt(2, returnSlipId);
      p.setObject(3, x.getPaymentDate());
      p.setString(4, x.getPaymentMethod());
      x.setId(insertKey(p));
    }
  }

  public FineInvoice8 findBySlip(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT * FROM tblFineInvoice WHERE returnSlipId=?")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return null;
        FineInvoice8 x = new FineInvoice8();
        x.setId(r.getInt("id"));
        x.setInvoiceCode(r.getString("invoiceCode"));
        x.setPaymentDate(r.getObject("paymentDate", java.time.LocalDateTime.class));
        x.setPaymentMethod(r.getString("paymentMethod"));
        return x;
      }
    }
  }
}
