package vn.ptit.libman8.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.time.*;
import org.junit.jupiter.api.*;
import vn.ptit.libman8.model.*;

public class FineTest8 {
  @Test
  void formula() {
    for (int days : new int[] {-3, 0, 4})
      for (int damage : new int[] {0, 20000}) {
        LoanDetail8 loan = new LoanDetail8();
        loan.setDueDate(LocalDate.of(2026, 1, 10));
        ReturnDetail8 d = new ReturnDetail8();
        d.setLoanDetail(loan);
        d.setReturnDate(LocalDateTime.of(2026, 1, 10, 12, 0).plusDays(days));
        d.setDailyLateFee(new BigDecimal("5000"));
        d.setDamageFine(BigDecimal.valueOf(damage));
        assertEquals(Math.max(0, days), d.getOverdueDays());
        assertEquals(
            0, BigDecimal.valueOf(Math.max(0, days) * 5000L + damage).compareTo(d.getTotalFine()));
      }
  }
}
