package vn.ptit.libman8.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.model.*;
import vn.ptit.libman8.service.*;

@WebServlet("/staff/returns")
public class ReturnDocumentsController8 extends HttpServlet {
  private void view(HttpServletRequest r, HttpServletResponse p, String name)
      throws ServletException, IOException {
    r.getRequestDispatcher("/WEB-INF/views/libraryStaff/" + name + ".jsp").forward(r, p);
  }

  protected void doGet(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    ReturnDocumentService8 svc = new ReturnDocumentService8();
    try {
      if (r.getParameter("receipt") != null) {
        ReturnSlip8 slip =
            svc.getReceipt(
                Integer.parseInt(r.getParameter("receipt")),
                (Staff8) r.getSession().getAttribute("staff"));
        if (slip == null) {
          p.sendError(404);
          return;
        }
        boolean invoiceOnly = "invoice".equals(r.getParameter("document"));
        if (invoiceOnly && slip.getFineInvoice() == null) {
          p.sendError(404, "No fine invoice was issued for this return");
          return;
        }
        r.setAttribute("slip", slip);
        r.setAttribute("invoiceOnly", invoiceOnly);
        if (invoiceOnly) r.setAttribute("reader", svc.findReader(slip.getCard().getCardCode()));
        view(r, p, "returnSlip8");
        return;
      }
      if ("preview".equals(r.getParameter("stage"))
          && r.getSession().getAttribute("returnDraft") != null) {
        r.setAttribute("slip", r.getSession().getAttribute("returnDraft"));
        view(r, p, "ConfirmReturnPage8");
        return;
      }
      String keyword = r.getParameter("keyword");
      if (keyword == null || keyword.isBlank()) {
        view(r, p, "findReaderCard8");
        return;
      }
      Reader8 reader = svc.findReader(keyword.trim());
      if (reader == null) {
        r.setAttribute("error", "Reader not found");
        view(r, p, "findReaderCard8");
        return;
      }
      r.getSession().setAttribute("returnReaderCardId", reader.getCard().getId());
      r.setAttribute("reader", reader);
      r.setAttribute("loans", svc.getOutstandingLoans(reader.getCard().getId()));
      ReturnSlip8 draft = (ReturnSlip8) r.getSession().getAttribute("returnDraft");
      if ("true".equals(r.getParameter("edit"))
          && draft != null
          && draft.getCard().getId() == reader.getCard().getId()) {
        Map<Integer, ReturnDetail8> selected = new HashMap<>();
        for (ReturnDetail8 detail : draft.getDetails())
          selected.put(detail.getLoanDetail().getId(), detail);
        r.setAttribute("selectedDetails", selected);
      }
      r.setAttribute(
          "expired", reader.getCard().getExpiryDate().isBefore(PolicyConfig8.now().toLocalDate()));
      view(r, p, "selectReturnDocuments8");
    } catch (SQLException e) {
      log("Return read failure", e);
      r.setAttribute("error", "Cannot read from MySQL. Check the local configuration.");
      view(r, p, "findReaderCard8");
    } catch (IllegalArgumentException e) {
      p.sendError(400);
    }
  }

  protected void doPost(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    HttpSession s = r.getSession();
    synchronized (s) {
      ReturnDocumentService8 svc = new ReturnDocumentService8();
      String action = r.getParameter("action");
      try {
        if ("edit".equals(action)) {
          ReturnSlip8 draft = (ReturnSlip8) s.getAttribute("returnDraft");
          if (draft == null)
            throw new IllegalArgumentException("The return draft is no longer available");
          s.setAttribute("returnReaderCardId", draft.getCard().getId());
          p.sendRedirect(
              r.getContextPath()
                  + "/staff/returns?keyword="
                  + java.net.URLEncoder.encode(
                      draft.getCard().getCardCode(), java.nio.charset.StandardCharsets.UTF_8)
                  + "&edit=true");
          return;
        }
        if ("cancel".equals(action)) {
          s.removeAttribute("returnDraft");
          p.sendRedirect(r.getContextPath() + "/staff/returns");
          return;
        }
        if ("confirm".equals(action)) {
          ReturnSlip8 draft = (ReturnSlip8) s.getAttribute("returnDraft");
          if (draft == null) {
            Object last = s.getAttribute("lastReceipt");
            p.sendRedirect(
                r.getContextPath() + "/staff/returns" + (last == null ? "" : "?receipt=" + last));
            return;
          }
          int id = svc.confirmReturn(draft, "yes".equals(r.getParameter("paymentReceived")));
          s.removeAttribute("returnDraft");
          s.setAttribute("lastReceipt", id);
          p.sendRedirect(r.getContextPath() + "/staff/returns?receipt=" + id);
          return;
        }
        if (!"calculate".equals(action)) {
          p.sendError(400);
          return;
        }
        Integer cardId = (Integer) s.getAttribute("returnReaderCardId");
        if (cardId == null)
          throw new IllegalArgumentException("Find a reader before selecting documents");
        String[] selected = r.getParameterValues("loanDetailId");
        if (selected == null)
          throw new IllegalArgumentException("Select at least one document copy to return");
        int[] ids = Arrays.stream(selected).mapToInt(Integer::parseInt).toArray();
        ReturnSlip8 draft = svc.createDraft(cardId, ids, (Staff8) s.getAttribute("staff"));
        for (ReturnDetail8 d : draft.getDetails()) {
          int id = d.getLoanDetail().getId();
          d.setReturnCondition(r.getParameter("returnCondition_" + id));
          String money = r.getParameter("damageFine_" + id);
          d.setDamageFine(new BigDecimal(money == null || money.isBlank() ? "0" : money));
          d.setNote(r.getParameter("note_" + id));
        }
        svc.calculateFine(draft);
        s.setAttribute("returnDraft", draft);
        p.sendRedirect(r.getContextPath() + "/staff/returns?stage=preview");
      } catch (SQLException e) {
        log("Return transaction failure", e);
        r.setAttribute(
            "error",
            "The return could not be saved. No changes were saved. Check the loans before trying"
                + " again.");
        error(r, p);
      } catch (IllegalArgumentException e) {
        r.setAttribute(
            "error",
            e instanceof NumberFormatException
                ? "Invalid amount or loan detail ID"
                : e.getMessage());
        error(r, p);
      }
    }
  }

  private void error(HttpServletRequest r, HttpServletResponse p)
      throws ServletException, IOException {
    Object draft = r.getSession().getAttribute("returnDraft");
    if (draft != null) {
      r.setAttribute("slip", draft);
      view(r, p, "ConfirmReturnPage8");
    } else view(r, p, "findReaderCard8");
  }
}
