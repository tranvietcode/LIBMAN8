package vn.ptit.libman8.service;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import vn.ptit.libman8.config.*;
import vn.ptit.libman8.dao.*;
import vn.ptit.libman8.model.*;

@EnabledIfEnvironmentVariable(named = "LIBMAN_MYSQL_TESTS", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MySqlIntegrationTest8 {
  Connection connection;
  FixtureDAO8 fixtures;
  LibraryStaff8 staff;
  String tag = "IT8" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  int sequence;

  @BeforeAll
  void setup() throws Exception {
    connection = DatabaseConfig8.open();
    staff = new AuthService8().login("librarian8", "ThuThu8!Demo");
    assertNotNull(staff);
    fixtures = new FixtureDAO8(connection, tag);
    fixtures.init(staff.getId());
  }

  @AfterAll
  void cleanup() throws Exception {
    if (fixtures != null) fixtures.cleanup();
    if (connection != null) connection.close();
  }

  Reader8 data() {
    Reader8 r = new Reader8();
    String id = tag + (++sequence);
    r.setFullName("Test Reader");
    r.setStudentCode(id);
    r.setEmail(id + "@test.invalid");
    r.setPhone("0901234567");
    r.setDateOfBirth(LocalDate.of(2004, 1, 1));
    r.setAddress("Hà Nội");
    r.setAcademicYear("2022–2026");
    r.setNote("Reader note");
    return r;
  }

  Reader8 reader() throws Exception {
    Reader8 r = new RegisterCardService8().register(data());
    fixtures.track(r);
    return r;
  }

  ReturnSlip8 draft(int days, int damage) throws Exception {
    Reader8 r = reader();
    int ld = fixtures.loan(r, staff.getId(), days);
    ReturnDocumentService8 svc = new ReturnDocumentService8();
    ReturnSlip8 d = svc.createDraft(r.getCard().getId(), new int[] {ld}, staff);
    d.getDetails()[0].setReturnCondition(damage > 0 ? "DAMAGED" : "GOOD");
    d.getDetails()[0].setDamageFine(BigDecimal.valueOf(damage));
    svc.calculateFine(d);
    return d;
  }

  @Test
  void registerSuccessAndDuplicates() throws Exception {
    Reader8 r = reader();
    assertTrue(r.getId() > 0);
    assertNotNull(r.getCard());
    assertEquals(r.getCard().getIssueDate().plusMonths(12), r.getCard().getExpiryDate());
    Reader8 stored = new ReaderDAO8(connection).findByCardOrStudentCode(r.getStudentCode());
    assertEquals(r.getId(), stored.getId());
    assertEquals(r.getFullName(), stored.getFullName());
    assertEquals(r.getDateOfBirth(), stored.getDateOfBirth());
    assertEquals(r.getEmail(), stored.getEmail());
    assertEquals(r.getPhone(), stored.getPhone());
    assertEquals(r.getAddress(), stored.getAddress());
    assertEquals(r.getAcademicYear(), stored.getAcademicYear());
    assertEquals(r.getNote(), stored.getNote());
    assertNull(stored.getUsername());
    assertNull(stored.getPasswordHash());
    assertEquals(r.getCard().getId(), stored.getCard().getId());
    assertEquals(r.getCard().getCardCode(), stored.getCard().getCardCode());
    assertEquals(r.getCard().getIssueDate(), stored.getCard().getIssueDate());
    assertEquals(r.getCard().getExpiryDate(), stored.getCard().getExpiryDate());
    assertEquals("ACTIVE", stored.getCard().getStatus());
    RegisterCardService8 svc = new RegisterCardService8();
    Reader8 another = data();
    another.setEmail(r.getEmail());
    assertTrue(svc.checkDuplicate(another).containsKey("email"));
    assertThrows(IllegalArgumentException.class, () -> svc.register(another));
    another.setEmail(data().getEmail());
    another.setStudentCode(r.getStudentCode());
    assertTrue(svc.checkDuplicate(another).containsKey("studentCode"));
    assertThrows(IllegalArgumentException.class, () -> svc.register(another));
  }

  @Test
  void invalidRegistration() {
    Reader8 r = data();
    r.setEmail("wrong");
    r.setDateOfBirth(null);
    r.setFullName("");
    r.setStudentCode("!");
    r.setPhone("x");
    assertEquals(5, new RegisterCardService8().validate(r).size());
    assertThrows(IllegalArgumentException.class, () -> new RegisterCardService8().register(r));
  }

  static Connection failOn(String table) throws SQLException {
    Connection actual = DatabaseConfig8.open();
    return (Connection)
        Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[] {Connection.class},
            (proxy, method, args) -> {
              if (method.getName().equals("prepareStatement")
                  && args[0].toString().startsWith("INSERT INTO " + table))
                throw new SQLException("Injected JDBC write failure");
              try {
                return method.invoke(actual, args);
              } catch (InvocationTargetException e) {
                throw e.getCause();
              }
            });
  }

  static class FailingRegisterService8 extends RegisterCardService8 {
    protected Connection openConnection() throws SQLException {
      return failOn("tblReaderCard");
    }
  }

  static class FailingReturnService8 extends ReturnDocumentService8 {
    protected Connection openConnection() throws SQLException {
      return failOn("tblFineInvoice");
    }
  }

  @Test
  void registrationRollback() throws Exception {
    Reader8 r = data();
    assertThrows(SQLException.class, () -> new FailingRegisterService8().register(r));
    assertEquals(0, fixtures.scalar("SELECT COUNT(*) FROM tblUser WHERE email=?", r.getEmail()));
    assertEquals(
        0,
        fixtures.scalar("SELECT COUNT(*) FROM tblReader WHERE studentCode=?", r.getStudentCode()));
  }

  void complete(int days, int damage) throws Exception {
    ReturnSlip8 d = draft(days, damage);
    ReturnDocumentService8 svc = new ReturnDocumentService8();
    int ld = d.getDetails()[0].getLoanDetail().getId();
    assertEquals(
        0, fixtures.scalar("SELECT COUNT(*) FROM tblReturnDetail WHERE loanDetailId=?", ld));
    assertEquals(
        0, BigDecimal.valueOf(Math.max(0, days) * 5000L + damage).compareTo(d.getTotalFine()));
    int id = svc.confirmReturn(d, d.getTotalFine().signum() > 0);
    ReturnSlip8 stored = svc.getReceipt(id, staff);
    assertEquals(0, d.getTotalFine().compareTo(stored.getTotalFine()));
    assertEquals("RETURNED", fixtures.status(ld));
    assertEquals(damage > 0 ? "UNDER_REPAIR" : "AVAILABLE", fixtures.copyStatus(ld));
    assertEquals(d.getTotalFine().signum() > 0, stored.getFineInvoice() != null);
    int count = fixtures.scalar("SELECT COUNT(*) FROM tblReturnSlip");
    svc.getReceipt(id, staff);
    svc.getReceipt(id, staff);
    assertEquals(count, fixtures.scalar("SELECT COUNT(*) FROM tblReturnSlip"));
    assertThrows(IllegalArgumentException.class, () -> svc.confirmReturn(d, true));
    assertEquals(
        1, fixtures.scalar("SELECT COUNT(*) FROM tblReturnDetail WHERE loanDetailId=?", ld));
  }

  @Test
  void onTime() throws Exception {
    complete(-4, 0);
  }

  @Test
  void overdue() throws Exception {
    complete(3, 0);
  }

  @Test
  void damaged() throws Exception {
    complete(-4, 25000);
  }

  @Test
  void overdueAndDamaged() throws Exception {
    complete(5, 40000);
  }

  @Test
  void wrongOwner() throws Exception {
    Reader8 a = reader(), b = reader();
    int ld = fixtures.loan(b, staff.getId(), 3);
    assertThrows(
        IllegalArgumentException.class,
        () -> new ReturnDocumentService8().createDraft(a.getCard().getId(), new int[] {ld}, staff));
  }

  @Test
  void paymentRequired() throws Exception {
    ReturnSlip8 d = draft(2, 0);
    assertThrows(
        IllegalArgumentException.class, () -> new ReturnDocumentService8().confirmReturn(d, false));
    assertEquals("ON_LOAN", fixtures.status(d.getDetails()[0].getLoanDetail().getId()));
  }

  @Test
  void goodMustHaveZeroDamageAndNegativeRejected() throws Exception {
    ReturnSlip8 d = draft(-1, 0);
    d.getDetails()[0].setDamageFine(BigDecimal.TEN);
    assertThrows(
        IllegalArgumentException.class, () -> new ReturnDocumentService8().calculateFine(d));
    d.getDetails()[0].setReturnCondition("DAMAGED");
    d.getDetails()[0].setDamageFine(BigDecimal.valueOf(-1));
    assertThrows(
        IllegalArgumentException.class, () -> new ReturnDocumentService8().calculateFine(d));
    d.getDetails()[0].setReturnCondition(null);
    assertEquals(
        "Select a valid return condition",
        assertThrows(
                IllegalArgumentException.class, () -> new ReturnDocumentService8().calculateFine(d))
            .getMessage());
  }

  @Test
  void nestedDocumentAndStaffMapping() throws Exception {
    Reader8 reader = reader();
    int id = fixtures.loan(reader, staff.getId(), -2);
    LoanSlip8[] loans = new ReturnDocumentService8().getOutstandingLoans(reader.getCard().getId());
    assertEquals(1, loans.length);
    assertNotNull(loans[0].getLoanDate());
    assertEquals(staff.getStaffCode(), loans[0].getStaff().getStaffCode());
    assertEquals(staff.getFullName(), loans[0].getStaff().getFullName());
    assertEquals("LIBRARY_STAFF", loans[0].getStaff().getPosition());
    LoanDetail8 detail = loans[0].getDetails()[0];
    assertEquals(id, detail.getId());
    ImportDetail8 imported = detail.getCopy().getImportDetail();
    assertNotNull(imported);
    assertTrue(imported.getId() > 0);
    assertEquals(100, imported.getQuantity());
    assertEquals(0, new BigDecimal("10000").compareTo(imported.getUnitPrice()));
    assertNotNull(imported.getDocument());
    assertEquals("Tài liệu kiểm thử", imported.getDocument().getTitle());
    assertEquals("BOOK", imported.getDocument().getDocumentType());
    assertEquals(2024, imported.getDocument().getPublicationYear());
  }

  @Test
  void saveFailureRollsBackEverything() throws Exception {
    ReturnSlip8 d = draft(2, 10000);
    int ld = d.getDetails()[0].getLoanDetail().getId();
    int count = fixtures.scalar("SELECT COUNT(*) FROM tblReturnSlip");
    assertThrows(SQLException.class, () -> new FailingReturnService8().confirmReturn(d, true));
    assertEquals(count, fixtures.scalar("SELECT COUNT(*) FROM tblReturnSlip"));
    assertEquals("ON_LOAN", fixtures.status(ld));
    assertEquals("ON_LOAN", fixtures.copyStatus(ld));
    assertEquals(
        0, fixtures.scalar("SELECT COUNT(*) FROM tblReturnDetail WHERE loanDetailId=?", ld));
  }

  @Test
  void changedFineRequiresPreview() throws Exception {
    ReturnSlip8 d = draft(2, 0);
    int ld = d.getDetails()[0].getLoanDetail().getId();
    fixtures.changeDueDate(ld);
    assertThrows(
        IllegalArgumentException.class, () -> new ReturnDocumentService8().confirmReturn(d, true));
    assertEquals("ON_LOAN", fixtures.status(ld));
  }

  @Test
  void changedDateRequiresPreview() throws Exception {
    ReturnSlip8 d = draft(2, 0);
    d.setReturnDate(d.getReturnDate().minusDays(1));
    assertThrows(
        IllegalArgumentException.class, () -> new ReturnDocumentService8().confirmReturn(d, true));
  }

  @Test
  void concurrentReturnOnlyOnce() throws Exception {
    ReturnSlip8 a = draft(2, 0);
    ReturnDocumentService8 svc = new ReturnDocumentService8();
    int ld = a.getDetails()[0].getLoanDetail().getId();
    ReturnSlip8 b = svc.createDraft(a.getCard().getId(), new int[] {ld}, staff);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (ReturnSlip8 d : new ReturnSlip8[] {a, b})
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  try {
                    new ReturnDocumentService8().confirmReturn(d, true);
                    return true;
                  } catch (IllegalArgumentException e) {
                    return false;
                  }
                }));
      start.countDown();
      int successes = 0;
      for (Future<Boolean> f : results) if (f.get(15, TimeUnit.SECONDS)) successes++;
      assertEquals(1, successes);
      assertEquals(
          1, fixtures.scalar("SELECT COUNT(*) FROM tblReturnDetail WHERE loanDetailId=?", ld));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void authorization() throws Exception {
    assertThrows(SecurityException.class, () -> new ReturnDocumentService8().getReceipt(1, null));
    assertNull(new AuthService8().login("librarian8", "wrong"));
  }

  @Test
  void multiCopyReturnAndDuplicateSelection() throws Exception {
    Reader8 r = reader();
    int a = fixtures.loan(r, staff.getId(), 2), b = fixtures.loan(r, staff.getId(), -4);
    ReturnDocumentService8 svc = new ReturnDocumentService8();
    assertThrows(
        IllegalArgumentException.class,
        () -> svc.createDraft(r.getCard().getId(), new int[] {a, a}, staff));
    ReturnSlip8 d = svc.createDraft(r.getCard().getId(), new int[] {a, b}, staff);
    svc.calculateFine(d);
    assertEquals(0, new BigDecimal("10000").compareTo(d.getTotalFine()));
    int id = svc.confirmReturn(d, true);
    assertEquals(2, svc.getReceipt(id, staff).getDetails().length);
    assertEquals("RETURNED", fixtures.status(a));
    assertEquals("RETURNED", fixtures.status(b));
    assertEquals(
        1, fixtures.scalar("SELECT COUNT(*) FROM tblFineInvoice WHERE returnSlipId=?", id));
  }

  @Test
  void seedIsIdempotent() throws Exception {
    String before = fixtures.snapshot();
    String seed = java.nio.file.Files.readString(java.nio.file.Path.of("sql/seed.sql"));
    new ScriptDAO8(connection).run(seed);
    new ScriptDAO8(connection).run(seed);
    assertEquals(before, fixtures.snapshot());
  }

  Reader8 accountData() {
    Reader8 reader = data();
    reader.setUsername(reader.getStudentCode().toLowerCase(Locale.ROOT));
    ReaderCard8 card = new ReaderCard8();
    card.setCardCode("CARD-" + reader.getStudentCode());
    reader.setCard(card);
    assertTrue(
        new RegisterCardService8()
            .prepareCredentials(reader, UUID.randomUUID().toString())
            .isEmpty());
    return reader;
  }

  @Test
  void suppliedCardAndUsernameAreUnique() throws Exception {
    Reader8 reader = accountData();
    new RegisterCardService8().register(reader);
    fixtures.track(reader);
    Reader8 duplicate = accountData();
    duplicate.getCard().setCardCode(reader.getCard().getCardCode());
    assertTrue(new RegisterCardService8().checkDuplicate(duplicate).containsKey("cardCode"));
    assertThrows(
        IllegalArgumentException.class, () -> new RegisterCardService8().register(duplicate));
    assertEquals(
        0, fixtures.scalar("SELECT COUNT(*) FROM tblUser WHERE email=?", duplicate.getEmail()));
    duplicate.getCard().setCardCode("OTHER-" + duplicate.getStudentCode());
    duplicate.setUsername(reader.getUsername());
    assertTrue(new RegisterCardService8().checkDuplicate(duplicate).containsKey("username"));
    assertThrows(
        IllegalArgumentException.class, () -> new RegisterCardService8().register(duplicate));
  }

  @Test
  void accountAndSuppliedCardRollback() throws Exception {
    Reader8 reader = accountData();
    assertThrows(SQLException.class, () -> new FailingRegisterService8().register(reader));
    assertFalse(new UserDAO8(connection).existsByUsername(reader.getUsername()));
    assertFalse(new ReaderDAO8(connection).existsByStudentCode(reader.getStudentCode()));
    assertFalse(new ReaderCardDAO8(connection).existsByCardCode(reader.getCard().getCardCode()));
  }

  @Test
  void partialReturnDueTodayLeavesOtherCopyOnLoan() throws Exception {
    Reader8 reader = reader();
    int first = fixtures.loan(reader, staff.getId(), 0);
    int second = fixtures.loan(reader, staff.getId(), -2);
    ReturnDocumentService8 service = new ReturnDocumentService8();
    ReturnSlip8 draft = service.createDraft(reader.getCard().getId(), new int[] {first}, staff);
    service.calculateFine(draft);
    assertEquals(0, draft.getTotalFine().signum());
    int receipt = service.confirmReturn(draft, false);
    assertEquals("RETURNED", fixtures.status(first));
    assertEquals("ON_LOAN", fixtures.status(second));
    assertNull(service.getReceipt(receipt, staff).getFineInvoice());
    LoanSlip8[] outstanding = service.getOutstandingLoans(reader.getCard().getId());
    assertEquals(1, outstanding.length);
    assertEquals(second, outstanding[0].getDetails()[0].getId());
  }
}
