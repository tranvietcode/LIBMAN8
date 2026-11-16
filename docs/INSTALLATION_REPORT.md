# Báo cáo cài đặt hệ thống LibMan 8

## 5.1 Tổ chức dự án

Hệ thống LibMan 8 được tổ chức dưới dạng dự án Maven Java Web, đóng gói WAR có tên libman8.war và triển khai trên Apache Tomcat 10.1 với context /libman8. Mã Java nằm trong src/main/java/vn/ptit/libman8, chia thành model, dao, service, controller, config và filter. Model biểu diễn dữ liệu; DAO dùng PreparedStatement để truy cập MySQL; Service kiểm tra nghiệp vụ và quản lý transaction; controller nhận HTTP request và forward đến JSP. Các JSP nằm dưới WEB-INF/views nên người dùng truy cập qua Servlet mapping thay vì gọi JSP trực tiếp.

Cây thư mục mã nguồn thực tế sau khi hoàn thiện:

~~~text
src/
├── main/
│   ├── java/
│   │   └── vn/
│   │       └── ptit/
│   │           └── libman8/
│   │               ├── config/
│   │               │   ├── AppLifecycle8.java
│   │               │   ├── DatabaseConfig8.java
│   │               │   ├── DatabaseTool8.java
│   │               │   └── PolicyConfig8.java
│   │               ├── controller/
│   │               │   ├── AuthServlet8.java
│   │               │   ├── HomeServlet8.java
│   │               │   ├── RegisterReaderCardController8.java
│   │               │   └── ReturnDocumentsController8.java
│   │               ├── dao/
│   │               │   ├── DAO8.java
│   │               │   ├── DocumentCopyDAO8.java
│   │               │   ├── FineInvoiceDAO8.java
│   │               │   ├── LoanDetailDAO8.java
│   │               │   ├── LoanSlipDAO8.java
│   │               │   ├── ReaderCardDAO8.java
│   │               │   ├── ReaderDAO8.java
│   │               │   ├── ReturnDetailDAO8.java
│   │               │   ├── ReturnSlipDAO8.java
│   │               │   ├── ScriptDAO8.java
│   │               │   ├── StaffDAO8.java
│   │               │   └── UserDAO8.java
│   │               ├── filter/
│   │               │   └── SecurityFilter8.java
│   │               ├── model/
│   │               │   ├── Document8.java
│   │               │   ├── DocumentCopy8.java
│   │               │   ├── FineInvoice8.java
│   │               │   ├── ImportDetail8.java
│   │               │   ├── ImportReceipt8.java
│   │               │   ├── LibraryStaff8.java
│   │               │   ├── LoanDetail8.java
│   │               │   ├── LoanSlip8.java
│   │               │   ├── ManagementStaff8.java
│   │               │   ├── Reader8.java
│   │               │   ├── ReaderCard8.java
│   │               │   ├── ReturnDetail8.java
│   │               │   ├── ReturnSlip8.java
│   │               │   ├── Staff8.java
│   │               │   ├── Supplier8.java
│   │               │   └── User8.java
│   │               └── service/
│   │                   ├── AuthService8.java
│   │                   ├── RegisterCardService8.java
│   │                   └── ReturnDocumentService8.java
│   └── webapp/
│       ├── assets/
│       │   └── style.css
│       └── WEB-INF/
│           ├── views/
│           │   ├── common/
│           │   │   ├── ErrorPage8.jsp
│           │   │   ├── Footer8.jspf
│           │   │   ├── Header8.jspf
│           │   │   ├── HomePage8.jsp
│           │   │   └── LoginPage8.jsp
│           │   ├── libraryStaff/
│           │   │   ├── ConfirmReturnPage8.jsp
│           │   │   ├── findReaderCard8.jsp
│           │   │   ├── libraryStaffMain8.jsp
│           │   │   ├── ReturnDocumentPage8.jsp
│           │   │   ├── ReturnResultPage8.jsp
│           │   │   ├── returnSlip8.jsp
│           │   │   ├── SearchReaderPage8.jsp
│           │   │   ├── selectReturnDocuments8.jsp
│           │   │   └── StaffDashboardPage8.jsp
│           │   └── reader/
│           │       ├── ConfirmCardPage8.jsp
│           │       ├── readerMain8.jsp
│           │       ├── RegisterCardPage8.jsp
│           │       ├── registerReaderCard8.jsp
│           │       └── RegistrationResultPage8.jsp
│           └── web.xml
└── test/
    └── java/
        └── vn/
            └── ptit/
                └── libman8/
                    ├── dao/
                    │   └── FixtureDAO8.java
                    └── service/
                        ├── FineTest8.java
                        ├── HttpFlowTest8.java
                        └── MySqlIntegrationTest8.java
~~~

Các tệp/thư mục ở gốc dự án gồm pom.xml, README.md, .gitignore, config/, sql/, scripts/, docs/, src/, target/ và .tools/. config/db.properties.example là mẫu cấu hình; config/db.local.properties chứa cấu hình riêng tại máy chạy và bị Git bỏ qua. sql/schema.sql định nghĩa 14 bảng InnoDB/utf8mb4; sql/seed.sql nạp dữ liệu mẫu. scripts/ chứa build.ps1, run.ps1, stop.ps1, init-db.ps1 và verify.ps1. target/ là đầu ra Maven, có libman8.war; .tools/ chứa Maven/Tomcat/cache và log local, không đưa vào source bàn giao. src/test/java chứa kiểm thử công thức, kiểm thử MySQL và luồng HTTP; FixtureDAO8 tạo và dọn đúng dữ liệu UUID của test.

pom.xml sử dụng Jakarta Servlet API 6.0.0 (provided), JSTL API 3.0.2 và implementation 3.0.1, MySQL Connector/J 8.4.0, jBCrypt 0.4 và JUnit Jupiter 5.11.4 (test). Compiler target Java 17; máy kiểm tra đang chạy JDK 27.0.1.0.2. Maven Compiler Plugin 3.14.1, WAR Plugin 3.4.0 và Surefire 3.5.2 đảm nhiệm biên dịch, đóng WAR và chạy test hậu tố Test8. Không sử dụng Spring, ORM hoặc stored procedure.

## 5.2 Cài đặt các module

### Module Register for a Reader Card

Người dùng mở GET /libman8/home (readerMain8.jsp) và chọn Register for a Reader Card. GET /libman8/register được RegisterReaderCardController8 xử lý, forward đến registerReaderCard8.jsp. Biểu mẫu gồm username, password, fullName, email, cardCode cùng các trường đang sử dụng là studentCode, dateOfBirth, phone, academicYear và address. Người đăng ký không cần tài khoản hoặc đăng nhập trước; thông tin tài khoản mới được nhập ngay trong biểu mẫu này.

Khi POST action=continue, controller tạo Reader8/ReaderCard8, gọi RegisterCardService8 kiểm tra định dạng, bắt buộc và trùng username/email/studentCode/cardCode. Mật khẩu được băm BCrypt, không hiển thị lại hoặc giữ dạng rõ trong session. Dữ liệu hợp lệ mới được lưu thành draft và chuyển sang ConfirmCardPage8.jsp. Nút Back / Edit Details cho phép sửa; Cancel hủy; Confirm Registration mới ghi dữ liệu. RegistrationResultPage8.jsp hiển thị mã thẻ, ngày cấp, ngày hết hạn và trạng thái sau thành công. Quy tắc hiệu lực 12 tháng được giữ vì đã có trong yêu cầu gốc và được triển khai bằng issueDate.plusMonths(12).

Trong một transaction JDBC, Service truyền cùng Connection cho UserDAO8.insert để ghi tblUser và lấy generated id, ReaderDAO8.insert để ghi tblReader với userId kế thừa, sau đó ReaderCardDAO8.insert ghi tblReaderCard với readerId tương ứng. Nếu một bước thất bại, rollback toàn bộ. tblUser.username/email, tblReader.studentCode và tblReaderCard.cardCode có unique constraint, đồng thời được kiểm tra trước preview/confirm. Redirect sau thành công và draft session ngăn gửi Confirm lặp. Các độc giả cũ có username/passwordHash NULL vẫn được bảo toàn; Service hỗ trợ cấp mã tự động cho lời gọi nội bộ cũ, còn form mới bắt buộc username/password/cardCode.

### Module Return Documents

Nhân viên đăng nhập tại /libman8/login qua AuthServlet8/AuthService8 và được chuyển đến /libman8/staff/dashboard (libraryStaffMain8.jsp). SecurityFilter8 kiểm tra quyền khu vực /staff/* và CSRF cho POST. Chọn Return Documents mở GET /libman8/staff/returns, do ReturnDocumentsController8 xử lý và forward đến findReaderCard8.jsp. Mã thẻ được nhập hoặc nhận từ máy quét hoạt động như bàn phím; hệ thống cũng giữ khả năng tìm bằng studentCode hiện có.

ReaderDAO8 tra cứu đúng Reader8 và ReaderCard8 qua tblUser/tblReader/tblReaderCard. LoanSlipDAO8 và LoanDetailDAO8 đọc tblLoanSlip/tblLoanDetail, chỉ lấy các dòng ON_LOAN, kết hợp tblDocumentCopy, tblImportDetail và tblDocument để hiển thị copyCode, title, loanDate và dueDate tại selectReturnDocuments8.jsp. Các dòng được phân biệt bằng loanDetailId và copyId, nên có thể chọn một phần các cuốn cùng tên. Khi không tìm thấy thẻ hoặc không có sách đang mượn, giao diện hiển thị thông báo tương ứng. Thẻ hết hạn vẫn được trả và có cảnh báo.

POST action=calculate tạo preview trong session, không ghi CSDL. Nhân viên nhập returnCondition, damageFine và note theo từng dòng; ReturnDocumentService8 tính overdueDays=max(0, ngày trả trừ dueDate), lateFine=overdueDays×dailyLateFee, cộng damageFine bằng BigDecimal. Đến hạn đúng ngày trả được tính 0 ngày quá hạn. Mức phí cấu hình hiện hành là 5.000 VND/ngày/cuốn, giả định demo tại PolicyConfig8.DAILY_LATE_FEE; mức này được lưu trên từng chi tiết khi trả để giữ lịch sử. ConfirmReturnPage8.jsp hiển thị chi tiết và tổng phạt. Back / Edit Selection giữ checkbox/condition/damageFine/note; sau khi sửa người dùng tính preview lại. Nếu tổng phạt dương, Confirm yêu cầu xác nhận đã thu đủ tiền mặt.

Khi POST action=confirm, Service mở transaction và LoanDetailDAO8.findForUpdate khóa các dòng đã chọn. Hệ thống kiểm tra lại dòng mượn thuộc thẻ và còn ON_LOAN, tính lại ngày và tổng tiền, từ chối nếu preview đã thay đổi. ReturnSlipDAO8 và ReturnDetailDAO8 ghi tblReturnSlip/tblReturnDetail; LoanDetailDAO8 cập nhật RETURNED; DocumentCopyDAO8 cập nhật AVAILABLE cho GOOD hoặc UNDER_REPAIR cho DAMAGED. FineInvoiceDAO8 chỉ ghi tblFineInvoice với paymentMethod CASH khi có phạt đã thu. Lỗi bất kỳ rollback cả phiếu, chi tiết, trạng thái và hóa đơn. Unique loanDetailId trong tblReturnDetail cùng việc khóa và kiểm tra trạng thái chặn trả trùng.

Sau commit, controller redirect GET /libman8/staff/returns?receipt=<id> và hiển thị returnSlip8.jsp. Print Slip dùng chức năng in trình duyệt; Export Invoice chỉ có khi phiếu đã lưu có hóa đơn. In lại theo ID chỉ SELECT và vẫn kiểm tra quyền nhân viên, không tạo thêm giao dịch hay khoản thu. Giao diện và phiếu in bằng tiếng Anh; footer yêu cầu đã xóa ở template chung.

### SQL và dữ liệu mẫu

Ứng dụng gọi SQL trực tiếp qua PreparedStatement trong DAO, không gọi stored procedure. sql/schema.sql tạo schema và bảng nếu chưa tồn tại; sql/seed.sql dùng INSERT có điều kiện theo mã nghiệp vụ, dữ liệu ngày tương đối ở lần nạp đầu và không reset sách đã trả khi chạy lại. Seed gồm nhân viên, bạn đọc/thẻ, supplier, phiếu nhập/chi tiết, document/copy và các lượt mượn để demo; hai nghiệp vụ nhập/cho mượn không có UI theo phạm vi bài.

Có thể nạp bằng MySQL Workbench: mở sql/schema.sql rồi Execute All, tiếp theo sql/seed.sql; hoặc sau build chạy scripts/init-db.ps1 để công cụ JDBC đọc cấu hình local và thực thi hai file. Không cần mở Workbench khi dùng website; dịch vụ MySQL Server và Tomcat cần đang chạy. Không chạy lại seed để reset dữ liệu.

Bản ERD 10 bảng không có trong repo, vì vậy phần cài đặt mô tả schema thật 14 bảng và ghi rõ chênh lệch, không tuyên bố đã chuyển DB thành 10 bảng. Thiết kế LoanItemDAO8/ReturnRecordDAO8/ReturnItemDAO8 được ánh xạ theo vai trò sang LoanDetailDAO8/ReturnSlipDAO8/ReturnDetailDAO8. Bảng chi tiết tên/cột/FK và các URL/JSP canonical nằm ở DESIGN_MAPPING.md mục 10.

## Bảng đối chiếu URL / controller / JSP / DAO / DB

| Chức năng | HTTP URL (context /libman8) | Controller | JSP | DAO / bảng |
|---|---|---|---|---|
| Trang bạn đọc | GET /home hoặc / | HomeServlet8 | readerMain8.jsp | — |
| Đăng ký, preview, confirm | GET/POST /register | RegisterReaderCardController8 | registerReaderCard8.jsp → ConfirmCardPage8.jsp → RegistrationResultPage8.jsp | UserDAO8/ReaderDAO8/ReaderCardDAO8 → tblUser/tblReader/tblReaderCard |
| Đăng nhập/xuất | GET/POST /login; POST /logout | AuthServlet8 | LoginPage8.jsp | StaffDAO8 → tblUser/tblStaff |
| Trang nhân viên | GET /staff/dashboard | HomeServlet8 | libraryStaffMain8.jsp | — |
| Tìm thẻ, chọn trả | GET /staff/returns?keyword=... | ReturnDocumentsController8 | findReaderCard8.jsp → selectReturnDocuments8.jsp | ReaderDAO8, LoanSlipDAO8, LoanDetailDAO8 → reader/card/loan/detail/copy/document/import detail |
| Preview, Back, confirm | POST /staff/returns | ReturnDocumentsController8 | ConfirmReturnPage8.jsp → returnSlip8.jsp | ReturnSlipDAO8/ReturnDetailDAO8/LoanDetailDAO8/DocumentCopyDAO8/FineInvoiceDAO8 → tblReturnSlip/tblReturnDetail/tblLoanDetail/tblDocumentCopy/tblFineInvoice |
| In/in lại | GET /staff/returns?receipt=id | ReturnDocumentsController8 | returnSlip8.jsp | getReceipt chỉ SELECT |

Build sạch và deploy WAR thành công; Maven verify thực tế: 24 test pass, 0 failure/error/skip. Dữ liệu và cấu trúc trước–sau khớp. Bằng chứng chi tiết ở TEST_REPORT.md; danh sách tệp thay đổi ở ALIGNMENT_2026-11-16.md. Chưa kiểm tra hộp thoại in/máy in thật và chưa có ERD 10 bảng để đối chiếu tên một-một.
