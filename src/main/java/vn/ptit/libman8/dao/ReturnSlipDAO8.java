package vn.ptit.libman8.dao;

import java.sql.*;
import vn.ptit.libman8.model.*;

public class ReturnSlipDAO8 extends DAO8 {
  public ReturnSlipDAO8(Connection c) {
    super(c);
  }

  public void insert(ReturnSlip8 x) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO tblReturnSlip(returnCode,returnDate,cardId,staffId) VALUES(?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, x.getReturnCode());
      p.setObject(2, x.getReturnDate());
      p.setInt(3, x.getCard().getId());
      p.setInt(4, x.getStaff().getId());
      x.setId(insertKey(p));
    }
  }

  public ReturnSlip8 findById(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT * FROM tblReturnSlip WHERE id=?")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return null;
        ReturnSlip8 x = new ReturnSlip8();
        x.setId(id);
        x.setReturnCode(r.getString("returnCode"));
        x.setReturnDate(r.getObject("returnDate", java.time.LocalDateTime.class));
        x.setCard(new ReaderDAO8(connection).findByCardId(r.getInt("cardId")).getCard());
        x.setStaff(new StaffDAO8(connection).findById(r.getInt("staffId")));
        x.setDetails(new ReturnDetailDAO8(connection).findBySlip(id, x.getReturnDate()));
        x.setFineInvoice(new FineInvoiceDAO8(connection).findBySlip(id));
        return x;
      }
    }
  }
}
