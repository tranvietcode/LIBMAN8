package vn.ptit.libman8.dao;

import java.sql.*;
import java.util.*;
import vn.ptit.libman8.model.*;

public class LoanSlipDAO8 extends DAO8 {
  public LoanSlipDAO8(Connection c) {
    super(c);
  }

  public LoanSlip8[] findOutstandingByCardId(int cardId) throws SQLException {
    List<LoanSlip8> list = new ArrayList<>();
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT DISTINCT ls.* FROM tblLoanSlip ls JOIN tblLoanDetail ld ON ld.loanSlipId=ls.id"
                + " WHERE ls.cardId=? AND ld.status='ON_LOAN' ORDER BY ls.id")) {
      p.setInt(1, cardId);
      try (ResultSet r = p.executeQuery()) {
        LoanDetail8[] all = new LoanDetailDAO8(connection).findOutstanding(cardId);
        while (r.next()) {
          LoanSlip8 x = new LoanSlip8();
          x.setId(r.getInt("id"));
          x.setLoanCode(r.getString("loanCode"));
          x.setLoanDate(r.getObject("loanDate", java.time.LocalDateTime.class));
          x.setCard(new ReaderDAO8(connection).findByCardId(cardId).getCard());
          List<LoanDetail8> ds = new ArrayList<>();
          try (PreparedStatement q =
              connection.prepareStatement("SELECT id FROM tblLoanDetail WHERE loanSlipId=?")) {
            q.setInt(1, x.getId());
            try (ResultSet z = q.executeQuery()) {
              Set<Integer> ids = new HashSet<>();
              while (z.next()) ids.add(z.getInt(1));
              for (LoanDetail8 d : all) if (ids.contains(d.getId())) ds.add(d);
            }
          }
          x.setDetails(ds.toArray(new LoanDetail8[0]));
          x.setStaff(new StaffDAO8(connection).findById(r.getInt("staffId")));
          list.add(x);
        }
      }
    }
    return list.toArray(new LoanSlip8[0]);
  }
}
