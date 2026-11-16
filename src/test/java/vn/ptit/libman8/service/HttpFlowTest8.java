package vn.ptit.libman8.service;

import static org.junit.jupiter.api.Assertions.*;

import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.dao.*;
import vn.ptit.libman8.model.*;

@EnabledIfEnvironmentVariable(named = "LIBMAN_HTTP_TESTS", matches = "true")
public class HttpFlowTest8 {
  private String base =
      System.getenv().getOrDefault("LIBMAN_TEST_URL", "http://127.0.0.1:8080/libman8");
  private final CookieManager cookies = new CookieManager();
  private final HttpClient client =
      HttpClient.newBuilder()
          .cookieHandler(cookies)
          .followRedirects(HttpClient.Redirect.NEVER)
          .build();
  private String csrf;

  HttpResponse<String> get(String route) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create(base + route)).GET().build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  HttpResponse<String> post(String route, Map<String, String> fields) throws Exception {
    String body =
        fields.entrySet().stream()
            .map(
                e ->
                    URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
    return client.send(
        HttpRequest.newBuilder(URI.create(base + route))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  Map<String, String> form(String... values) {
    Map<String, String> m = new HashMap<>();
    m.put("csrf", csrf);
    for (int i = 0; i < values.length; i += 2) m.put(values[i], values[i + 1]);
    return m;
  }

  String redirect(HttpResponse<String> r) {
    assertEquals(302, r.statusCode(), r.body());
    return r.headers().firstValue("location").orElseThrow().replace("/libman8", "");
  }

  @Test
  void loginWhitespaceAndFailureFeedback() throws Exception {
    String html = get("/login").body();
    Matcher token = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
    assertTrue(token.find());
    csrf = token.group(1);
    HttpResponse<String> failed =
        post("/login", form("username", "librarian8", "password", "ThuThu8!Demo "));
    assertEquals(200, failed.statusCode());
    assertTrue(failed.body().contains("The password has leading or trailing whitespace"));
    assertTrue(failed.body().contains("value=\"librarian8\""));
    assertFalse(failed.body().contains("ThuThu8!Demo"));
    assertEquals(
        "/staff/dashboard",
        redirect(post("/login", form("username", " librarian8 ", "password", "ThuThu8!Demo"))));
    assertEquals(200, get("/staff/dashboard").statusCode());
  }

  @Test
  void fullBrowserHttpFlow() throws Exception {
    try (Connection c = DatabaseConfig8.open()) {
      String tag = "HTTP8" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
      String readerPassword = UUID.randomUUID().toString();
      FixtureDAO8 f = new FixtureDAO8(c, tag);
      LibraryStaff8 staff = new AuthService8().login("librarian8", "ThuThu8!Demo");
      f.init(staff.getId());
      try {
        HttpResponse<String> home = get("/home");
        assertEquals(200, home.statusCode());
        assertTrue(home.body().contains("Register for a Reader Card"));
        assertFalse(
            home.body().contains("Information Systems Analysis and Design Midterm Project"));
        assertFalse(home.body().contains("<footer>"));
        assertEquals(302, get("/staff/returns").statusCode());
        assertEquals(404, get("/WEB-INF/views/reader/RegisterCardPage8.jsp").statusCode());
        String html = get("/register").body();
        Matcher token = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(token.find());
        csrf = token.group(1);
        assertEquals(403, post("/register", Map.of("action", "continue")).statusCode());
        Map<String, String> registration =
            form(
                "action",
                "continue",
                "fullName",
                "HTTP Reader <script>alert(1)</script>",
                "studentCode",
                tag,
                "dateOfBirth",
                "2004-01-01",
                "email",
                tag + "@test.invalid",
                "phone",
                "0901234567",
                "academicYear",
                "2022-2026",
                "address",
                "Hà Nội");
        registration.put("username", tag.toLowerCase(Locale.ROOT));
        registration.put("password", readerPassword);
        registration.put("cardCode", "CARD-" + tag);
        Map<String, String> invalid = new HashMap<>(registration);
        invalid.put("email", "invalid");
        HttpResponse<String> bad = post("/register", invalid);
        assertEquals(200, bad.statusCode());
        assertTrue(bad.body().contains("Enter a valid email address"), bad.body());
        assertFalse(bad.body().contains(readerPassword));
        String confirm = get(redirect(post("/register", registration))).body();
        assertTrue(confirm.contains("Review Your Details"));
        assertTrue(confirm.contains("&lt;script&gt;"));
        assertTrue(confirm.contains("CARD-" + tag.toUpperCase(Locale.ROOT)));
        assertFalse(confirm.contains(readerPassword));
        assertEquals(
            0, f.scalar("SELECT COUNT(*) FROM tblUser WHERE email=?", tag + "@test.invalid"));
        assertEquals("/register", redirect(post("/register", form("action", "edit"))));
        assertTrue(get("/register").body().contains(tag.toUpperCase(Locale.ROOT)));
        String result = redirect(post("/register", form("action", "confirm")));
        assertTrue(get(result).body().contains("Registration Successful"));
        Reader8 registered = new ReaderDAO8(c).findByCardOrStudentCode(tag);
        assertNotNull(registered);
        f.track(registered);
        assertEquals("HTTP Reader <script>alert(1)</script>", registered.getFullName());
        assertEquals("Hà Nội", registered.getAddress());
        assertEquals("2022-2026", registered.getAcademicYear());
        assertEquals("0901234567", registered.getPhone());
        assertEquals(tag.toLowerCase(Locale.ROOT) + "@test.invalid", registered.getEmail());
        assertEquals(java.time.LocalDate.of(2004, 1, 1), registered.getDateOfBirth());
        assertEquals(tag.toLowerCase(Locale.ROOT), registered.getUsername());
        assertTrue(
            org.mindrot.jbcrypt.BCrypt.checkpw(readerPassword, registered.getPasswordHash()));
        assertEquals("CARD-" + tag.toUpperCase(Locale.ROOT), registered.getCard().getCardCode());
        post("/register", form("action", "confirm"));
        assertEquals(
            1, f.scalar("SELECT COUNT(*) FROM tblUser WHERE email=?", tag + "@test.invalid"));
        Map<String, String> duplicate = new HashMap<>(registration);
        duplicate.put("username", tag.toLowerCase(Locale.ROOT) + "x");
        duplicate.put("studentCode", tag + "x");
        duplicate.put("email", tag + "x@test.invalid");
        HttpResponse<String> duplicateCard = post("/register", duplicate);
        assertEquals(200, duplicateCard.statusCode());
        assertTrue(duplicateCard.body().contains("This card code is already registered"));
        assertFalse(duplicateCard.body().contains(readerPassword));
        duplicate.put("cardCode", "CARD-" + tag + "x");
        duplicate.put("username", tag.toLowerCase(Locale.ROOT));
        assertTrue(
            post("/register", duplicate).body().contains("This username is already registered"));
        redirect(post("/register", form("action", "cancel")));
        assertEquals(
            "/staff/dashboard",
            redirect(post("/login", form("username", "librarian8", "password", "ThuThu8!Demo"))));
        assertEquals(200, get("/staff/dashboard").statusCode());
        assertTrue(get("/staff/returns?keyword=SV005").body().contains("no outstanding loans"));
        assertTrue(
            get("/staff/returns?keyword=SV006").body().contains("This reader card has expired"));
        assertTrue(
            get("/staff/returns?keyword=DOES-NOT-EXIST8").body().contains("Reader not found"));
        int ld = f.loan(registered, staff.getId(), 3);
        HttpResponse<String> copies = get("/staff/returns?keyword=" + tag);
        assertEquals(200, copies.statusCode());
        assertTrue(copies.body().contains("Tài liệu kiểm thử"));
        assertTrue(copies.body().contains("returnCondition_" + ld));
        assertTrue(copies.body().contains("damageFine_" + ld));
        assertTrue(copies.body().contains("Loan Date"));
        HttpResponse<String> missingCondition =
            post(
                "/staff/returns",
                form(
                    "action",
                    "calculate",
                    "loanDetailId",
                    Integer.toString(ld),
                    "damageFine_" + ld,
                    "0"));
        assertEquals(200, missingCondition.statusCode());
        assertTrue(missingCondition.body().contains("Select a valid return condition"));
        assertEquals("ON_LOAN", f.status(ld));
        String preview =
            redirect(
                post(
                    "/staff/returns",
                    form(
                        "action",
                        "calculate",
                        "loanDetailId",
                        Integer.toString(ld),
                        "returnCondition_" + ld,
                        "DAMAGED",
                        "damageFine_" + ld,
                        "20000",
                        "note_" + ld,
                        "Ghi chú tiếng Việt")));
        assertTrue(get(preview).body().contains("35000"));
        assertEquals("ON_LOAN", f.status(ld));
        String edit = redirect(post("/staff/returns", form("action", "edit")));
        String editHtml = get(edit).body();
        assertTrue(editHtml.contains("checked"));
        assertTrue(editHtml.contains("20000"));
        assertTrue(editHtml.contains("Ghi chú tiếng Việt"));
        assertTrue(editHtml.contains("DAMAGED"));
        assertEquals("ON_LOAN", f.status(ld));
        HttpResponse<String> missingPayment = post("/staff/returns", form("action", "confirm"));
        assertEquals(200, missingPayment.statusCode());
        assertTrue(
            missingPayment.body().contains("Confirm that the full cash payment has been received"));
        String receipt =
            redirect(
                post(
                    "/staff/returns",
                    form(
                        "action",
                        "confirm",
                        "paymentReceived",
                        "yes",
                        "totalFine",
                        "0",
                        "staffId",
                        "0")));
        String saved = get(receipt).body();
        assertTrue(saved.contains("35000"));
        assertTrue(saved.contains("CASH"));
        assertTrue(saved.contains("window.print()"));
        assertTrue(saved.contains("Ghi chú tiếng Việt"));
        assertTrue(saved.contains("Tài liệu kiểm thử"));
        assertTrue(saved.contains("Fine Invoice:"));
        assertTrue(saved.contains("Export Invoice"));
        assertEquals("RETURNED", f.status(ld));
        int count = f.scalar("SELECT COUNT(*) FROM tblReturnSlip");
        assertEquals(
            receipt,
            redirect(post("/staff/returns", form("action", "confirm", "paymentReceived", "yes"))));
        get(receipt);
        get(receipt);
        String invoiceUrl = receipt + "&document=invoice";
        HttpResponse<String> invoice = get(invoiceUrl);
        assertEquals(200, invoice.statusCode());
        assertTrue(invoice.body().contains("<h1>Fine Invoice</h1>"));
        assertTrue(invoice.body().contains("Invoice Code"));
        assertTrue(invoice.body().contains("Print Invoice / Save PDF"));
        assertTrue(invoice.body().contains("35000"));
        assertTrue(invoice.body().contains("&lt;script&gt;"));
        assertTrue(get(invoiceUrl + "&print=true").body().contains("window.addEventListener"));
        Integer demoReceiptId = f.findExistingInvoiceReceiptId(registered.getCard().getId());
        String demoRoute =
            demoReceiptId == null
                ? invoiceUrl
                : "/staff/returns?receipt=" + demoReceiptId + "&document=invoice";
        HttpResponse<String> demo = get(demoRoute);
        assertEquals(200, demo.statusCode());
        String css =
            java.nio.file.Files.readString(
                java.nio.file.Path.of("src/main/webapp/assets/style.css"));
        String demoHtml =
            demo.body()
                .replace(
                    "<link rel=\"stylesheet\" href=\"/libman8/assets/style.css\">",
                    "<style>" + css + "</style>")
                .replace("href=\"/libman8/", "href=\"http://localhost:8080/libman8/");
        java.nio.file.Files.createDirectories(java.nio.file.Path.of(".tools"));
        java.nio.file.Files.writeString(
            java.nio.file.Path.of(".tools/invoice-demo.html"), demoHtml);
        java.nio.file.Files.writeString(
            java.nio.file.Path.of(".tools/invoice-demo-info.txt"),
            demoReceiptId == null
                ? "Snapshot from a disposable MySQL test fixture"
                : "Saved invoice URL: http://localhost:8080/libman8" + demoRoute);
        assertEquals(count, f.scalar("SELECT COUNT(*) FROM tblReturnSlip"));
        assertEquals(1, f.scalar("SELECT COUNT(*) FROM tblReturnDetail WHERE loanDetailId=?", ld));
        assertEquals(
            1,
            f.scalar(
                "SELECT COUNT(*) FROM tblFineInvoice fi JOIN tblReturnSlip rs ON"
                    + " rs.id=fi.returnSlipId WHERE rs.cardId=?",
                registered.getCard().getId()));
        int onTime = f.loan(registered, staff.getId(), -3);
        get("/staff/returns?keyword=" + tag);
        redirect(
            post(
                "/staff/returns",
                form(
                    "action",
                    "calculate",
                    "loanDetailId",
                    Integer.toString(onTime),
                    "returnCondition_" + onTime,
                    "GOOD",
                    "damageFine_" + onTime,
                    "0")));
        String noFineReceipt = redirect(post("/staff/returns", form("action", "confirm")));
        assertFalse(get(noFineReceipt).body().contains("Export Invoice"));
        assertEquals(404, get(noFineReceipt + "&document=invoice").statusCode());
        assertEquals(
            1,
            f.scalar(
                "SELECT COUNT(*) FROM tblFineInvoice fi JOIN tblReturnSlip rs ON"
                    + " rs.id=fi.returnSlipId WHERE rs.cardId=?",
                registered.getCard().getId()));
        assertEquals("/home", redirect(post("/logout", form())));
        assertEquals(302, get(receipt).statusCode());
        assertEquals(302, get(invoiceUrl).statusCode());
      } finally {
        f.cleanup();
      }
    }
  }
}
