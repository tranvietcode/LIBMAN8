package vn.ptit.libman8.dao;

import java.sql.*;
import java.util.*;
import vn.ptit.libman8.model.*;

public class ReturnDetailDAO8 extends DAO8 {
  public ReturnDetailDAO8(Connection c) {
    super(c);
  }

  public void insertAll(int returnSlipId, ReturnDetail8[] details) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO"
                + " tblReturnDetail(returnSlipId,loanDetailId,returnCondition,dailyLateFee,damageFine,note)"
                + " VALUES(?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      for (ReturnDetail8 d : details) {
        p.setInt(1, returnSlipId);
        p.setInt(2, d.getLoanDetail().getId());
        p.setString(3, d.getReturnCondition());
        p.setBigDecimal(4, d.getDailyLateFee());
        p.setBigDecimal(5, d.getDamageFine());
        p.setString(6, d.getNote());
        d.setId(insertKey(p));
      }
    }
  }

  public ReturnDetail8[] findBySlip(int id, java.time.LocalDateTime date) throws SQLException {
    List<ReturnDetail8> out = new ArrayList<>();
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT * FROM tblReturnDetail WHERE returnSlipId=? ORDER BY id")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          ReturnDetail8 d = new ReturnDetail8();
          d.setId(r.getInt("id"));
          d.setLoanDetail(new LoanDetailDAO8(connection).findById(r.getInt("loanDetailId")));
          d.setReturnCondition(r.getString("returnCondition"));
          d.setDailyLateFee(r.getBigDecimal("dailyLateFee"));
          d.setDamageFine(r.getBigDecimal("damageFine"));
          d.setNote(r.getString("note"));
          d.setReturnDate(date);
          out.add(d);
        }
      }
    }
    return out.toArray(new ReturnDetail8[0]);
  }
}
