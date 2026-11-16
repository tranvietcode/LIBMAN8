package vn.ptit.libman8.service;

import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.dao.*;
import vn.ptit.libman8.model.*;

public class ReturnDocumentService8 {
  protected Connection openConnection() throws SQLException {
    return DatabaseConfig8.open();
  }

  private void authorize(Connection c, Staff8 staff) throws SQLException {
    if (staff == null || !new StaffDAO8(c).isLibraryStaff(staff.getId()))
      throw new SecurityException("Library staff access is required");
  }

  public Reader8 findReader(String keyword) throws SQLException {
    try (Connection c = openConnection()) {
      return new ReaderDAO8(c).findByCardOrStudentCode(keyword);
    }
  }

  public LoanSlip8[] getOutstandingLoans(int cardId) throws SQLException {
    try (Connection c = openConnection()) {
      return new LoanSlipDAO8(c).findOutstandingByCardId(cardId);
    }
  }

  public ReturnSlip8 createDraft(int cardId, int[] loanDetailIds, Staff8 staff)
      throws SQLException {
    if (loanDetailIds == null || loanDetailIds.length == 0 || loanDetailIds.length > 100)
      throw new IllegalArgumentException("Select between 1 and 100 document copies to return");
    Set<Integer> ids = new HashSet<>();
    for (int id : loanDetailIds)
      if (!ids.add(id)) throw new IllegalArgumentException("Duplicate loan detail selection");
    try (Connection c = openConnection()) {
      authorize(c, staff);
      Reader8 reader = new ReaderDAO8(c).findByCardId(cardId);
      if (reader == null) throw new IllegalArgumentException("Reader card not found");
      Map<Integer, LoanDetail8> available = new HashMap<>();
      for (LoanDetail8 d : new LoanDetailDAO8(c).findOutstanding(cardId))
        available.put(d.getId(), d);
      ReturnSlip8 draft = new ReturnSlip8();
      draft.setCard(reader.getCard());
      draft.setStaff(staff);
      draft.setReturnDate(PolicyConfig8.now());
      List<ReturnDetail8> details = new ArrayList<>();
      for (int id : loanDetailIds) {
        LoanDetail8 ld = available.get(id);
        if (ld == null)
          throw new IllegalArgumentException(
              "The selected loan does not belong to this card or has already been returned");
        ReturnDetail8 d = new ReturnDetail8();
        d.setLoanDetail(ld);
        d.setReturnCondition("GOOD");
        d.setDailyLateFee(PolicyConfig8.DAILY_LATE_FEE);
        d.setDamageFine(BigDecimal.ZERO);
        d.setReturnDate(draft.getReturnDate());
        details.add(d);
      }
      draft.setDetails(details.toArray(new ReturnDetail8[0]));
      return draft;
    }
  }

  public void calculateFine(ReturnSlip8 draft) {
    if (draft == null || draft.getDetails().length == 0)
      throw new IllegalArgumentException("The return draft is empty");
    draft.setReturnDate(PolicyConfig8.now());
    for (ReturnDetail8 d : draft.getDetails()) {
      validateDetail(d);
      d.setReturnDate(draft.getReturnDate());
      d.setDailyLateFee(PolicyConfig8.DAILY_LATE_FEE);
    }
  }

  private void validateDetail(ReturnDetail8 d) {
    if (d.getReturnCondition() == null
        || !Set.of("GOOD", "DAMAGED").contains(d.getReturnCondition()))
      throw new IllegalArgumentException("Select a valid return condition");
    BigDecimal m = d.getDamageFine();
    if (m == null
        || m.signum() < 0
        || m.scale() > 2
        || m.compareTo(new BigDecimal("9999999999999.99")) > 0)
      throw new IllegalArgumentException(
          "Damage fine must be non-negative, within the allowed range, and have at most two decimal"
              + " places");
    if ("GOOD".equals(d.getReturnCondition()) && m.signum() != 0)
      throw new IllegalArgumentException("A copy in GOOD condition must have a damage fine of 0");
    if (d.getNote() != null && d.getNote().length() > 500)
      throw new IllegalArgumentException("Note must not exceed 500 characters");
  }

  public int confirmReturn(ReturnSlip8 draft, boolean paymentReceived) throws SQLException {
    if (draft == null)
      throw new IllegalArgumentException("The return draft is no longer available");
    try (Connection c = openConnection()) {
      c.setAutoCommit(false);
      try {
        authorize(c, draft.getStaff());
        int[] ids =
            Arrays.stream(draft.getDetails()).mapToInt(d -> d.getLoanDetail().getId()).toArray();
        if (ids.length == 0 || Arrays.stream(ids).distinct().count() != ids.length)
          throw new IllegalArgumentException("Invalid loan detail selection");
        LoanDetail8[] locked = new LoanDetailDAO8(c).findForUpdate(ids);
        if (locked.length != ids.length)
          throw new IllegalArgumentException("One or more selected loan details no longer exist");
        Map<Integer, LoanDetail8> map = new HashMap<>();
        for (LoanDetail8 x : locked) {
          if (x.getCardId() != draft.getCard().getId() || !"ON_LOAN".equals(x.getStatus()))
            throw new IllegalArgumentException(
                "A selected loan has already been returned or does not belong to this card");
          map.put(x.getId(), x);
        }
        LocalDateTime now = PolicyConfig8.now();
        if (!now.toLocalDate().equals(draft.getReturnDate().toLocalDate()))
          throw new IllegalArgumentException(
              "The return date has changed. Recalculate and review the return before confirming.");
        BigDecimal preview = draft.getTotalFine();
        ReturnSlip8 saved = new ReturnSlip8();
        saved.setReturnDate(now);
        saved.setReturnCode(PolicyConfig8.code("RET-"));
        saved.setCard(new ReaderDAO8(c).findByCardId(draft.getCard().getId()).getCard());
        saved.setStaff(draft.getStaff());
        List<ReturnDetail8> out = new ArrayList<>();
        for (ReturnDetail8 old : draft.getDetails()) {
          validateDetail(old);
          ReturnDetail8 d = new ReturnDetail8();
          d.setLoanDetail(map.get(old.getLoanDetail().getId()));
          d.setReturnDate(now);
          d.setReturnCondition(old.getReturnCondition());
          d.setDamageFine(old.getDamageFine());
          d.setDailyLateFee(PolicyConfig8.DAILY_LATE_FEE);
          d.setNote(old.getNote());
          out.add(d);
        }
        saved.setDetails(out.toArray(new ReturnDetail8[0]));
        if (saved.getTotalFine().compareTo(preview) != 0)
          throw new IllegalArgumentException(
              "The fine has changed. Recalculate and review the return before confirming.");
        if (saved.getTotalFine().signum() > 0 && !paymentReceived)
          throw new IllegalArgumentException(
              "Confirm that the full cash payment has been received");
        new ReturnSlipDAO8(c).insert(saved);
        new ReturnDetailDAO8(c).insertAll(saved.getId(), saved.getDetails());
        new LoanDetailDAO8(c).markReturned(ids);
        for (ReturnDetail8 d : saved.getDetails()) {
          DocumentCopy8 cp = d.getLoanDetail().getCopy();
          cp.setCondition(d.getReturnCondition());
          cp.setStatus("GOOD".equals(d.getReturnCondition()) ? "AVAILABLE" : "UNDER_REPAIR");
          new DocumentCopyDAO8(c).updateConditionAndStatus(cp);
        }
        if (saved.getTotalFine().signum() > 0) {
          FineInvoice8 inv = new FineInvoice8();
          inv.setInvoiceCode(PolicyConfig8.code("FINE-"));
          inv.setPaymentDate(now);
          inv.setPaymentMethod("CASH");
          new FineInvoiceDAO8(c).insert(inv, saved.getId());
        }
        c.commit();
        return saved.getId();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public ReturnSlip8 getReceipt(int returnSlipId, Staff8 staff) throws SQLException {
    try (Connection c = openConnection()) {
      authorize(c, staff);
      return new ReturnSlipDAO8(c).findById(returnSlipId);
    }
  }
}
