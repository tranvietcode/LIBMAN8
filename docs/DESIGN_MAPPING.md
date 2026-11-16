> Cập nhật 16/11/2026: yêu cầu mới thay quy tắc form đăng ký chỉ thẻ bằng biểu mẫu tạo username/password và nhập cardCode. Không cần tài khoản có sẵn. Các độc giả cũ có username/passwordHash NULL được giữ; Service tiếp tục hỗ trợ luồng cấp mã tự động nội bộ/fixture. Tên canonical và bảng đối chiếu mới ở mục 10; tên cũ trong mô tả lịch sử không còn là tên controller đang chạy.

# Hợp đồng thiết kế và đối chiếu LibMANPTTK8

## 1. Nguồn chuẩn và phạm vi

Nguồn chuẩn là hợp đồng entity/schema/API/nghiệp vụ trong yêu cầu ban đầu (C, D, E, F, G), cùng yêu cầu rà soát và chuyển UI sang tiếng Anh ngày 03/11/2026. Tài liệu này phân biệt **thuộc tính theo thiết kế**, **helper triển khai**, **chữ ký đã xác minh**. Không lấy code làm căn cứ thay đổi hợp đồng. Không tìm thấy UML hoặc tài liệu thiết kế độc lập khác trong workspace; README/DEMO/TEST_REPORT là tài liệu hướng dẫn và kiểm tra.

Java target 17; Maven WAR; Tomcat 10.1 / Jakarta Servlet 6; JSP + JDBC + MySQL. Base package `vn.ptit.libman8`; package model, dao, service, controller, config, filter. Mọi lớp tự viết kể cả test/lớp lồng có hậu tố 8; thuộc tính/phương thức không thêm 8. JSP đặt dưới WEB-INF/views/common, reader, libraryStaff.

Luồng: **JSP → Servlet → Service → DAO**, các tầng dùng model. SQL chỉ ở DAO/script SQL, không trong JSP/Servlet/entity. DAO8 nhận Connection qua constructor, không commit/rollback/đóng connection do Service sở hữu. Service mở connection cho từng thao tác; các DAO trong transaction dùng chính connection đó.

Bảng trước sửa và các điểm thiếu/mâu thuẫn: [REVIEW_NOTES.md](REVIEW_NOTES.md).

## 2. Entity theo hợp đồng

Các id là int; ngày LocalDate; thời điểm LocalDateTime; tiền BigDecimal. Scalar văn bản dùng String. Mọi entity có constructor không tham số, getter/setter tương ứng. Public collection giữ entity[], không đổi sang List. Thuộc tính dẫn xuất chỉ có getter và không có cột DB.

| Entity | Kế thừa | Thuộc tính chính thức theo thiết kế |
|---|---|---|
| User8 (abstract) | — | int id; String username, passwordHash, fullName; LocalDate dateOfBirth; String address, email, phone, note |
| Reader8 | User8 | String studentCode, academicYear; ReaderCard8 card |
| Staff8 (abstract) | User8 | String staffCode, position |
| LibraryStaff8 | Staff8 | kế thừa |
| ManagementStaff8 | Staff8 | kế thừa |
| ReaderCard8 | — | int id; String cardCode; LocalDate issueDate, expiryDate; String status |
| Document8 | — | int id; String documentCode, title, documentType, author, publisher; int publicationYear; String description; DocumentCopy8[] copies |
| DocumentCopy8 | — | int id; String copyCode, condition, status; ImportDetail8 importDetail |
| Supplier8 | — | int id; String supplierCode, name, address, email, phone |
| ImportReceipt8 | — | int id; String receiptCode; LocalDate importDate; Supplier8 supplier; Staff8 staff; ImportDetail8[] details |
| ImportDetail8 | — | int id; Document8 document; int quantity; BigDecimal unitPrice |
| LoanSlip8 | — | int id; String loanCode; LocalDateTime loanDate; ReaderCard8 card; Staff8 staff; LoanDetail8[] details |
| LoanDetail8 | — | int id; DocumentCopy8 copy; LocalDate dueDate; String status |
| ReturnSlip8 | — | int id; String returnCode; LocalDateTime returnDate; ReaderCard8 card; Staff8 staff; ReturnDetail8[] details; FineInvoice8 fineInvoice |
| ReturnDetail8 | — | int id; LoanDetail8 loanDetail; String returnCondition; BigDecimal dailyLateFee, damageFine; String note |
| FineInvoice8 | — | int id; String invoiceCode; LocalDateTime paymentDate; String paymentMethod |

Dẫn xuất: ImportDetail8.getSubtotal() = quantity × unitPrice; ImportReceipt8.getTotalAmount() = tổng subtotal; ReturnDetail8.getOverdueDays() trả long = max(0, DAYS.between(dueDate, returnDate.toLocalDate())); getLateFine() = overdueDays × dailyLateFee; getTotalFine() = lateFine + damageFine; ReturnSlip8.getTotalFine() = tổng chi tiết. Các tổng tiền trả BigDecimal.

### Helper không phải thuộc tính hợp đồng

- LoanDetail8.cardId (int, get/set): ngữ cảnh ownership từ tblLoanSlip.cardId để kiểm tra lại các dòng khóa FOR UPDATE; không phải cột mới ở tblLoanDetail.
- ReturnDetail8.returnDate (LocalDateTime, get/set): ngữ cảnh tính dẫn xuất từ ReturnSlip8.returnDate; không có cột này ở tblReturnDetail. “Không lưu DB” ở đây không có nghĩa entity đã implements Serializable hoặc field có keyword transient.
- Đã bỏ DocumentCopy8.document: dùng association có sẵn importDetail.document. DAO nạp importDetail (id, quantity, unitPrice, document) cùng scalar document; JSP dùng copy.importDetail.document.title. Không thêm association/bảng mới.
- Đã bỏ LoanDetail8.loanDate: JSP dùng parent LoanSlip8.loanDate.
- Document8.copies/ImportReceipt8.details là quan hệ theo hợp đồng, nhưng hai module không thực hiện tải toàn bộ cây nhập kho/catalog. Các truy vấn lượt mượn nạp nhánh cần thiết, không tạo graph vòng; Document8.copies ở nhánh này giữ mảng rỗng.

## 3. Ánh xạ entity → CSDL, PK và FK

Tên bảng/cột giữ nguyên, không migration trong lần rà soát. MySQL trên Windows có thể hiển thị tên bảng lowercase; đó là cách server lưu tên, không đổi tên SQL contract.

| Entity / thuộc tính hoặc collection | Bảng và ánh xạ |
|---|---|
| User8 | tblUser.id PK AUTO_INCREMENT; username UNIQUE nullable; passwordHash nullable; fullName, dateOfBirth DATE, address nullable, email UNIQUE, phone, note nullable |
| Reader8.id kế thừa | tblReader.userId vừa PK vừa FK → tblUser.id; studentCode UNIQUE; academicYear nullable |
| Staff8.id kế thừa | tblStaff.userId vừa PK vừa FK → tblUser.id; staffCode UNIQUE; position phân loại LIBRARY_STAFF / MANAGEMENT_STAFF |
| LibraryStaff8 / ManagementStaff8 | cùng tblStaff; không có bảng riêng; StaffDAO8 tạo subtype theo position |
| Reader8.card | tblReaderCard.readerId UNIQUE FK → tblReader.userId; ReaderCard8.id → tblReaderCard.id; cardCode UNIQUE, issueDate/expiryDate DATE, status |
| Document8 | tblDocument.id PK; documentCode UNIQUE; title, documentType, author, publisher, publicationYear INT, description |
| Document8.copies | tblDocumentCopy.documentId FK → tblDocument.id; Java đi từ copy.importDetail.document khi nạp nhánh lượt mượn |
| DocumentCopy8 | tblDocumentCopy.id PK; copyCode UNIQUE; condition (quote MySQL), status |
| DocumentCopy8.importDetail | tblDocumentCopy.importDetailId FK → tblImportDetail.id; documentId trên copy phải khớp tblImportDetail.documentId (DAO phát hiện dữ liệu lệch, không tự sửa DB) |
| Supplier8 | tblSupplier.id PK; supplierCode UNIQUE, name, address, email, phone |
| ImportReceipt8 | tblImportReceipt.id PK; receiptCode UNIQUE, importDate DATE; supplier → supplierId FK tblSupplier.id; staff → staffId FK tblStaff.userId |
| ImportReceipt8.details | tblImportDetail.importReceiptId FK → tblImportReceipt.id |
| ImportDetail8 | tblImportDetail.id PK; document → documentId FK tblDocument.id; quantity INT, unitPrice DECIMAL(15,2) |
| LoanSlip8 | tblLoanSlip.id PK; loanCode UNIQUE, loanDate DATETIME; card → cardId FK tblReaderCard.id; staff → staffId FK tblStaff.userId |
| LoanSlip8.details | tblLoanDetail.loanSlipId FK → tblLoanSlip.id |
| LoanDetail8 | tblLoanDetail.id PK; copy → copyId FK tblDocumentCopy.id; dueDate DATE, status |
| ReturnSlip8 | tblReturnSlip.id PK; returnCode UNIQUE, returnDate DATETIME; card → cardId FK tblReaderCard.id; staff → staffId FK tblStaff.userId |
| ReturnSlip8.details | tblReturnDetail.returnSlipId FK → tblReturnSlip.id |
| ReturnDetail8 | tblReturnDetail.id PK; loanDetail → loanDetailId UNIQUE FK tblLoanDetail.id; returnCondition, dailyLateFee/damageFine DECIMAL(15,2), note |
| ReturnSlip8.fineInvoice | tblFineInvoice.returnSlipId UNIQUE FK → tblReturnSlip.id; không có fineInvoiceId trên tblReturnSlip |
| FineInvoice8 | tblFineInvoice.id PK; invoiceCode UNIQUE, paymentDate DATETIME, paymentMethod |

Cả 14 bảng dùng InnoDB/utf8mb4. Không bảng FullName/Address/LibraryStaff/ManagementStaff. Không cột totalAmount, subtotal, overdueDays, lateFine, totalFine. FK readerId/staffId trỏ tblReader.userId/tblStaff.userId; chỉ các khóa kế thừa userId mới trỏ tblUser.id.

## 4. API đã xác minh

Hợp đồng ban đầu chốt tên/phương thức/đối số, nhưng phần lớn không chốt kiểu trả về. Các kiểu trả về dưới đây là lựa chọn triển khai đã kiểm tra, được giữ để không phá chức năng; chúng không được suy ngược thành yêu cầu gốc. Tất cả phương thức DAO nhận Connection từ DAO8(Connection); SQLException được truyền lên Service.

| Lớp | Phương thức hiện tại đã xác minh |
|---|---|
| UserDAO8 | boolean existsByEmail(String email); int insert(User8 user) — generated id |
| ReaderDAO8 | boolean existsByStudentCode(String studentCode); void insert(Reader8 reader); Reader8 findByCardOrStudentCode(String keyword) |
| ReaderCardDAO8 | void insert(ReaderCard8 card, int readerId) |
| LoanSlipDAO8 | LoanSlip8[] findOutstandingByCardId(int cardId) |
| LoanDetailDAO8 | LoanDetail8[] findForUpdate(int[] ids); void markReturned(int[] ids) |
| DocumentCopyDAO8 | void updateConditionAndStatus(DocumentCopy8 copy) |
| ReturnSlipDAO8 | void insert(ReturnSlip8 slip); ReturnSlip8 findById(int id) |
| ReturnDetailDAO8 | void insertAll(int returnSlipId, ReturnDetail8[] details) |
| FineInvoiceDAO8 | void insert(FineInvoice8 invoice, int returnSlipId) |
| RegisterCardService8 | Map<String,String> validate(Reader8 reader); Map<String,String> checkDuplicate(Reader8 reader); Reader8 register(Reader8 reader) |
| ReturnDocumentService8 | Reader8 findReader(String keyword); LoanSlip8[] getOutstandingLoans(int cardId); ReturnSlip8 createDraft(int cardId, int[] loanDetailIds, Staff8 staff); void calculateFine(ReturnSlip8 draft); int confirmReturn(ReturnSlip8 draft, boolean paymentReceived); ReturnSlip8 getReceipt(int returnSlipId, Staff8 staff) |

Tên biến cục bộ trong code có thể là r/x/d và không phải tên thuộc tính/API mới. Constructor luôn đúng tên lớp hậu tố 8.

Helper tối thiểu: ReaderDAO8.findByCardId(int); LoanDetailDAO8.findOutstanding(int)/findById(int); ReturnDetailDAO8.findBySlip(int,LocalDateTime); FineInvoiceDAO8.findBySlip(int); StaffDAO8.findByUsername(String)/findById(int)/isLibraryStaff(int); DAO8.insertKey(PreparedStatement). StaffDAO8.findById nạp thuộc tính kế thừa, note/staffCode/position và subtype, dùng khi đọc LoanSlip/ReturnSlip. AuthService8.login(String,String) trả LibraryStaff8 hoặc null. openConnection() protected trong hai Service là điểm tiêm lỗi JDBC cho test; mặc định luôn mở MySQL thật.

Không thêm DAO nghiệp vụ nhập/cho mượn vì ngoài phạm vi; các entity/bảng của chúng vẫn đầy đủ theo thiết kế.

## 5. Truy vết trường đăng ký xuyên suốt

| JSP input name / label tiếng Anh | Servlet request → Entity | DAO và cột | Chiều đọc hiển thị |
|---|---|---|---|
| fullName / Full Name | getParameter(fullName) → Reader8.fullName (kế thừa) | UserDAO8.insert → tblUser.fullName | ReaderDAO8 → reader.fullName |
| studentCode / Student Code | studentCode → Reader8.studentCode (trim/uppercase hiện hữu) | ReaderDAO8.insert → tblReader.studentCode | reader.studentCode |
| dateOfBirth / Date of Birth | dateOfBirth → LocalDate → Reader8.dateOfBirth | UserDAO8 → tblUser.dateOfBirth | reader.dateOfBirth; dateInput giữ raw khi lỗi parse |
| email / Email | email → Reader8.email (trim/lowercase hiện hữu) | UserDAO8 → tblUser.email | reader.email |
| phone / Phone | phone → Reader8.phone | UserDAO8 → tblUser.phone | reader.phone |
| academicYear / Academic Year | academicYear → Reader8.academicYear | ReaderDAO8 → tblReader.academicYear | reader.academicYear |
| address / Address | address → Reader8.address | UserDAO8 → tblUser.address | reader.address |
| username / Username; password / Password | Username nhập mới; prepareCredentials băm BCrypt, không lưu raw password vào draft | UserDAO8 → tblUser.username UNIQUE, passwordHash | reader.username; không hiển thị mật khẩu |
| Không input note trong module 1 | User8.note giữ khả năng model/DAO, mặc định null | tblUser.note | ReaderDAO8 vẫn nạp note khi có dữ liệu |
| cardCode / Card Code | Reader8.card.cardCode nhập ở form mới, kiểm tra trùng; server cấp issueDate hôm nay, expiryDate +12 tháng, ACTIVE | ReaderCardDAO8 → tblReaderCard; readerId = Reader8.id | Confirmation/RegistrationResult hiển thị cardCode và trạng thái/ngày cấp |

RegisterReaderCardController8 (/register) truyền request reader/errors/dateInput/error; session registrationDraft/registrationResult. Các khóa điều hướng action/stage, csrf là control fields, không phải thuộc tính entity.

readerMain8.jsp → registerReaderCard8.jsp → ConfirmCardPage8.jsp → RegistrationResultPage8.jsp. Continue validate/checkDuplicate, lưu draft session, chưa ghi DB. Edit giữ draft; Cancel xóa draft, không lưu. Confirm kiểm tra lại; transaction User → Reader (userId được kế thừa) → ReaderCard; lỗi rollback cả ba bảng. PRG + synchronized session + unique keys chống gửi lặp. Confirm lỗi nạp lại errors theo trường, kể cả race trùng dữ liệu.

## 6. Truy vết module trả tài liệu

| Input / dữ liệu từ server | Servlet / Service | Entity → DAO / cột | Chiều đọc JSP |
|---|---|---|---|
| keyword | findReader(keyword) | ReaderDAO8 tìm tblReader.studentCode hoặc tblReaderCard.cardCode | reader.fullName/studentCode/card.* |
| Thẻ đang xử lý | returnReaderCardId trong session; không dùng cardId client | createDraft kiểm tra outstanding thuộc thẻ; confirm kiểm tra lại ownership từ LoanDetail helper cardId | reader và slip.card |
| loanDetailId (nhiều checkbox) | getParameterValues → int[] loanDetailIds | createDraft kiểm tra ON_LOAN; findForUpdate tại confirm; loanDetailId UNIQUE ở tblReturnDetail | LoanSlip8.details[].id |
| returnCondition_<loanDetailId> | getParameter → ReturnDetail8.returnCondition | validate GOOD/DAMAGED; tblReturnDetail.returnCondition; update copy.condition/status | d.returnCondition |
| damageFine_<loanDetailId> | getParameter → BigDecimal → ReturnDetail8.damageFine | không âm, đúng precision; GOOD = 0; tblReturnDetail.damageFine | d.damageFine |
| note_<loanDetailId> | getParameter → ReturnDetail8.note | tối đa 500; tblReturnDetail.note | c:out d.note (giữ ngôn ngữ nội dung) |
| dailyLateFee | server PolicyConfig8.DAILY_LATE_FEE = 5000.00 VND, giả định demo | snapshot tblReturnDetail.dailyLateFee | d.dailyLateFee từ DB khi in lại |
| loanDate / dueDate / copyCode / title | đọc DB, không nhận giá trị tin cậy từ client | LoanSlip8.loanDate; LoanDetail8.dueDate/copy; copy.importDetail.document.title | loan.loanDate, d.dueDate, d.copy.copyCode, d.copy.importDetail.document.title |
| returnDate / returnCode | server ngày Asia/Ho_Chi_Minh và UUID tại confirm | tblReturnSlip.returnDate/returnCode; ngữ cảnh ngày truyền cho chi tiết | slip.returnDate/returnCode |
| overdueDays / lateFine / totalFine | getter dẫn xuất; confirm tính lại server từ dòng khóa | không lưu cột; nếu ngày/tổng đổi thì rollback, yêu cầu tính lại | d.overdueDays/lateFine/totalFine; slip.totalFine |
| staff | session staff đã login; quyền filter + kiểm tra DB Service | ReturnSlip8.staff → tblReturnSlip.staffId = tblStaff.userId | slip.staff.fullName |
| paymentReceived=yes | checkbox chỉ khi tổng > 0; confirm server bắt buộc | có phạt đã thu → FineInvoice8, paymentMethod CASH; không phạt → không invoice | slip.fineInvoice.invoiceCode/paymentDate/paymentMethod |
| receipt (GET, ID phiếu) | getReceipt(id, session staff), kiểm tra quyền | ReturnSlipDAO8.findById + details/invoice helpers; chỉ SELECT | ReturnResultPage8 và window.print(), không ghi nghiệp vụ |

Tên có suffix _<id> phân biệt từng dòng form, không phải thêm hậu tố 8 hoặc đổi thuộc tính entity. Prefix cũ condition_/damage_ được thay đồng bộ; không hỗ trợ form cũ sau redeploy, tải lại trang trước khi thao tác.

LoginPage8.jsp → libraryStaffMain8.jsp → findReaderCard8.jsp → selectReturnDocuments8.jsp → ConfirmReturnPage8.jsp → returnSlip8.jsp. ReturnDocumentsController8 truyền reader/loans/expired/error hoặc slip; session returnReaderCardId/returnDraft/lastReceipt/staff. GET receipt/in lại chỉ đọc; preview/Calculate không ghi DB. Hết hạn thẻ chỉ cảnh báo, vẫn trả được.

Confirm một transaction: khóa SELECT FOR UPDATE theo ID → kiểm tra ownership/ON_LOAN và ngày/phạt/thu tiền → insert ReturnSlip/ReturnDetail → markReturned → copy GOOD thành AVAILABLE hoặc DAMAGED thành UNDER_REPAIR → invoice CASH nếu tổng > 0 → commit. Bất kỳ lỗi nào rollback toàn bộ, unique loanDetailId chặn trả hai lần.

## 7. Servlet, JSP và cấu hình

| Controller / URL | Service | JSP đúng thiết kế |
|---|---|---|
| HomeServlet8 /home | — | reader/readerMain8.jsp |
| RegisterReaderCardController8 /register | RegisterCardService8 | reader/registerReaderCard8.jsp, ConfirmCardPage8.jsp, RegistrationResultPage8.jsp |
| HomeServlet8 /staff/dashboard | — | libraryStaff/libraryStaffMain8.jsp |
| ReturnDocumentsController8 /staff/returns | ReturnDocumentService8 | libraryStaff/findReaderCard8.jsp, selectReturnDocuments8.jsp, ConfirmReturnPage8.jsp, returnSlip8.jsp |
| AuthServlet8 /login, /logout | AuthService8 | common/LoginPage8.jsp (helper xác thực được yêu cầu) |
| Error mapping trong web.xml | — | common/ErrorPage8.jsp; Header8/Footer8.jspf dùng chung |

Page8 biểu diễn JSP, không có Java class Page8. JSP dùng JSTL c:out cho dữ liệu; không dịch hoặc thay đổi tên người, địa chỉ, nhan đề, ghi chú trong DB. Tất cả nội dung ứng dụng định nghĩa trên UI/validation/receipt/invoice/print bằng tiếng Anh, html lang=en. Native browser validation theo locale trình duyệt; ứng dụng không định nghĩa custom browser-validation messages khác.

DatabaseConfig8 chỉ đọc file local gitignored/biến môi trường, không đóng gói mật khẩu trong WAR. PolicyConfig8 chứa phí giả định, timezone và UUID. SecurityFilter8 kiểm tra CSRF mọi POST và quyền /staff/*; AuthServlet8 dùng BCrypt, rotate session ID. AppLifecycle8 giải phóng JDBC driver. DatabaseTool8/ScriptDAO8 chỉ là CLI bootstrap, không tự chạy khi deploy; không web endpoint.

## 8. Kết quả và chênh lệch còn lại

Các helper/điểm tài liệu thiếu đã ghi tách biệt ở mục 1/2/4, không đổi mô hình quan hệ hoặc thêm lớp/tầng sản phẩm. Không đổi schema/seed và không reset database. Kết quả build, kiểm tra MySQL/HTTP, snapshot bảo toàn dữ liệu và giới hạn xác minh được ghi tại REVIEW_NOTES.md và TEST_REPORT.md sau khi thực thi.

## 9. Bổ sung xuất/in hóa đơn — 04/11/2026

- Giữ returnSlip8.jsp, thêm chế độ invoiceOnly; không thêm entity, Service, DAO, Servlet, bảng hoặc cột. ReturnDocumentsController8 dùng getReceipt(id, session staff) như trước.
- Nút **Export Invoice** chỉ xuất hiện nếu ReturnSlip8.fineInvoice đã tồn tại. URL GET `/staff/returns?receipt=<id>&document=invoice&print=true` mở tab mới, hiển thị hóa đơn và gọi window.print() sau load. Người dùng chọn Save as PDF trong hộp thoại trình duyệt để xuất PDF.
- Bản invoice hiển thị invoiceCode, paymentDate/paymentMethod, PAID IN FULL, độc giả, studentCode, cardCode, returnCode, thủ thư, từng thành phần phạt và tổng từ phiếu đã lưu. Reader lấy qua Service.findReader(cardCode); không SQL trong Servlet/JSP.
- URL không có print=true chỉ hiển thị hóa đơn, có nút Print Invoice / Save PDF. Đây là chế độ dùng cho demo HTML.
- Chỉ SELECT khi xuất/in lại. Không tạo hóa đơn hoặc thu tiền lần nữa. Phiếu không có phạt không hiện nút Export Invoice; yêu cầu ép document=invoice trả HTTP 404. Kiểm tra quyền thủ thư vẫn áp dụng.
- MySQL Workbench là công cụ quản trị, không cần mở để ứng dụng chạy. Dịch vụ MySQL Server (MySQL80 trên máy hiện tại) và Tomcat cần đang chạy.

## 10. Ánh xạ sau hoàn thiện ngày 16/11/2026

| Chức năng / URL / method | Controller | JSP canonical | DAO → bảng/cột thật |
|---|---|---|---|
| GET / hoặc /home | HomeServlet8 | reader/readerMain8.jsp | không DB |
| GET /register; POST action=continue/edit/cancel/confirm | RegisterReaderCardController8 | reader/registerReaderCard8.jsp; ConfirmCardPage8.jsp; RegistrationResultPage8.jsp | UserDAO8 → tblUser.username/passwordHash/fullName/dateOfBirth/address/email/phone; ReaderDAO8 → tblReader.userId/studentCode/academicYear; ReaderCardDAO8 → tblReaderCard.readerId/cardCode/issueDate/expiryDate/status |
| GET/POST /login; POST /logout | AuthServlet8 | common/LoginPage8.jsp | StaffDAO8 → tblUser JOIN tblStaff(userId) |
| GET /staff/dashboard | HomeServlet8 | libraryStaff/libraryStaffMain8.jsp | không ghi DB |
| GET /staff/returns?keyword=... | ReturnDocumentsController8 | libraryStaff/findReaderCard8.jsp; selectReturnDocuments8.jsp | ReaderDAO8 → tblUser/tblReader/tblReaderCard; LoanSlipDAO8/LoanDetailDAO8 → tblLoanSlip/tblLoanDetail/tblDocumentCopy/tblImportDetail/tblDocument |
| POST /staff/returns, action=calculate/edit/cancel | ReturnDocumentsController8 | selectReturnDocuments8.jsp; ConfirmReturnPage8.jsp | draft trong session; SELECT khi lấy lượt mượn, không ghi |
| POST /staff/returns, action=confirm | ReturnDocumentsController8 | redirect GET receipt=id → returnSlip8.jsp | ReturnSlipDAO8 → tblReturnSlip; ReturnDetailDAO8 → tblReturnDetail; LoanDetailDAO8 → status; DocumentCopyDAO8 → condition/status; FineInvoiceDAO8 → tblFineInvoice |
| GET /staff/returns?receipt=id [&document=invoice&print=true] | ReturnDocumentsController8 | libraryStaff/returnSlip8.jsp | getReceipt chỉ SELECT, quyền staff; in không ghi |

URL ở bảng là phần sau context /libman8. Tất cả link/form/redirect dùng contextPath; không có public JSP URL. Các JSP tên cũ HomePage8, RegisterCardPage8, StaffDashboardPage8, SearchReaderPage8, ReturnDocumentPage8, ReturnResultPage8 giữ như include alias để tương thích; file chứa GUI thực là tên canonical mới. Hai Java Servlet cũ đã đổi tên, không giữ class @WebServlet trùng mapping.

**Phần thiết kế 10 bảng:** không có file ERD 10 bảng/tên bảng gốc trong repo. Không thể tuyên bố ánh xạ một-một với tên chưa được cung cấp. Schema thật đã xác minh đủ 14 bảng và các ràng buộc, không cần migration. Bảng sau ánh xạ theo vai trò được prompt mới nêu:

| Vai trò/lớp trong thiết kế | Lớp/bảng hiện dùng | Giải thích tách bảng/cột |
|---|---|---|
| Tài khoản / bạn đọc, ReaderDAO8 | User8/Reader8; tblUser + tblReader | userId vừa PK vừa FK tblUser.id; username/hash/email/fullName ở User; studentCode/academicYear ở Reader |
| ReaderCardDAO8 | tblReaderCard | readerId UNIQUE FK tblReader.userId; cardCode UNIQUE; issueDate/expiryDate/status |
| Nhân viên | tblUser + tblStaff | staff.userId PK/FK; position phân quyền; không bảng LibraryStaff riêng |
| Tài liệu / bản sao | tblDocument + tblDocumentCopy | documentCode/title mô tả đầu sách; copyCode/id nhận diện từng bản sao |
| Nhà cung cấp / nhập | tblSupplier + tblImportReceipt + tblImportDetail | supplierId/staffId trên receipt; importReceiptId/documentId/quantity/unitPrice trên detail; copy.importDetailId nối nguồn nhập |
| LoanItemDAO8 (vai trò) | LoanSlipDAO8 + LoanDetailDAO8; tblLoanSlip + tblLoanDetail | loanSlip.cardId/staffId/loanDate; detail.loanSlipId/copyId/dueDate/status |
| ReturnRecordDAO8 (vai trò) | ReturnSlipDAO8; tblReturnSlip | returnCode/returnDate/cardId/staffId |
| ReturnItemDAO8 (vai trò) | ReturnDetailDAO8; tblReturnDetail | returnSlipId/loanDetailId UNIQUE/returnCondition/dailyLateFee/damageFine/note |
| Thông tin thu phạt | FineInvoiceDAO8; tblFineInvoice | returnSlipId UNIQUE/invoiceCode/paymentDate/paymentMethod; không có tổng tiền dẫn xuất lưu cột |

Giữ DAO tên theo entity/schema thật để không thêm wrapper chỉ đổi tên. Stored procedure không được gọi; toàn bộ truy cập nghiệp vụ là PreparedStatement trong DAO. Thêm existsByUsername và existsByCardCode; Service kiểm tra trùng trước preview và confirm, unique index chặn race. Password nhập chỉ được băm BCrypt; draft giữ hash, error/edit form không điền lại password. Nếu form có lỗi, hash bị xóa và người dùng nhập lại password. Confirm lưu User → Reader → Card cùng connection/transaction, rollback toàn bộ nếu lỗi.

Back / Edit Selection trên preview trả mở lại đúng thẻ và giữ checkbox/condition/damageFine/note; sửa xong Calculate tạo preview mới. Chỉ Confirm ghi nghiệp vụ. Mức 5000 VND/ngày vẫn là giả định demo, dueDate trùng ngày trả phạt 0. Footer được xóa trong template chung.
