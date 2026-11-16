# Đối chiếu và hoàn thiện theo yêu cầu ngày 16/11/2026

## Trước sửa

Không có Git repository tại D:\LIBMANPTTK (`git status` báo not a git repository). Đã tạo ZIP dự phòng src/docs/sql/pom/README/scripts trong .tools trước khi sửa; cấu hình local chứa mật khẩu không đưa vào ZIP. Không có AGENTS.md hoặc file UML/ERD độc lập trong workspace. Vì chưa có bản ERD 10 bảng, không suy đoán tên/cột của 10 bảng để thay cấu trúc thật.

| Chức năng | URL / method | Controller trước sửa | JSP trước sửa | DAO → bảng |
|---|---|---|---|---|
| Trang bạn đọc | GET /home hoặc / | HomeServlet8 | common/HomePage8.jsp | không ghi DB |
| Đăng ký/xem trước/xác nhận | GET/POST /register | RegisterCardServlet8 | RegisterCardPage8, ConfirmCardPage8, RegistrationResultPage8 | UserDAO8/ReaderDAO8/ReaderCardDAO8 → tblUser/tblReader/tblReaderCard |
| Đăng nhập/xuất | GET/POST /login; POST /logout | AuthServlet8 | LoginPage8 | StaffDAO8 → tblUser/tblStaff |
| Trang thủ thư | GET /staff/dashboard | HomeServlet8 | StaffDashboardPage8 | không ghi DB |
| Tìm và chọn tài liệu trả | GET /staff/returns?keyword=... | ReturnDocumentServlet8 | SearchReaderPage8/ReturnDocumentPage8 | ReaderDAO8/LoanSlipDAO8/LoanDetailDAO8 → tblReaderCard/tblLoanSlip/tblLoanDetail/tblDocumentCopy |
| Tính/xác nhận trả | POST /staff/returns | ReturnDocumentServlet8 | ConfirmReturnPage8/ReturnResultPage8 | ReturnSlipDAO8/ReturnDetailDAO8/DocumentCopyDAO8/FineInvoiceDAO8 → phiếu, chi tiết, copy, hóa đơn |
| In/in lại | GET /staff/returns?receipt=id | ReturnDocumentServlet8 | ReturnResultPage8 | getReceipt, chỉ SELECT |

Chênh lệch cần sửa: tên controller/JSP khác prompt mới; form đăng ký chưa nhập username/password/cardCode (quy tắc cũ cho phép null, tự sinh mã); thiếu kiểm tra trùng username/cardCode ở bước form; preview trả chỉ có Cancel thay vì Back giữ lựa chọn; footer cần xóa; nhãn chức năng chưa thống nhất Register for a Reader Card / Return Documents / Return Slip.

Yêu cầu mới cho phép đăng ký kèm thông tin tài khoản trong cùng biểu mẫu, không yêu cầu đã có tài khoản/đăng nhập trước. Giữ dữ liệu Student Code, Date of Birth, Phone, Academic Year và Address đang dùng. Quy tắc 12 tháng có căn cứ từ yêu cầu ban đầu và đã có plusMonths(12), nên được giữ.

## Kế hoạch đối chiếu tên an toàn

- Controller canonical: RegisterReaderCardController8 và ReturnDocumentsController8; giữ URL /register và /staff/returns.
- JSP canonical: readerMain8.jsp, registerReaderCard8.jsp, libraryStaffMain8.jsp, findReaderCard8.jsp, selectReturnDocuments8.jsp, returnSlip8.jsp. Các tên JSP cũ giữ bằng include alias nội bộ để tương thích.
- Giữ DAO gắn schema thật: LoanItemDAO8 (vai trò thiết kế) → LoanDetailDAO8, ReturnRecordDAO8 → ReturnSlipDAO8, ReturnItemDAO8 → ReturnDetailDAO8. Không tạo DAO wrapper chỉ để đổi tên.
- Không đổi schema nếu metadata hiện tại đã đáp ứng trường/ràng buộc. Kết quả kiểm tra và bảng ánh xạ cuối ghi tại DESIGN_MAPPING.md, TEST_REPORT.md và INSTALLATION_REPORT.md.

## Kết quả sau sửa

- Build sạch WAR thành công; đã deploy Tomcat /libman8. 24 test pass (1 công thức, 2 luồng HTTP, 21 MySQL), không lỗi/skip.
- Metadata 14 bảng/18 FK/78 kiểu cột/15 unique keys khớp; không cần migration. Snapshot các dòng có sẵn và cấu trúc trước–sau khớp; schema/seed không sửa.
- UI canonical/aliases, tài khoản+mã thẻ trên form, rollback, Back giữ lựa chọn, trả một phần, đúng ngày đến hạn và xóa footer đã được kiểm tra. Chưa QA trực quan/in vật lý; ERD 10 bảng chưa có trong repo để đối chiếu tên một-một.
- Báo cáo copy 5.1/5.2: INSTALLATION_REPORT.md. Hướng dẫn URL/config/build/run/demo: DEMO.md.

## Danh sách tệp thay đổi so với backup trước sửa

REMOVED ở hai file Java cũ là đổi tên sang controller mới, không xóa chức năng. Các JSP cũ chuyển thành include alias; không giữ Servlet class trùng mapping.

~~~text
MODIFIED src/main/java/vn/ptit/libman8/controller/HomeServlet8.java
REMOVED src/main/java/vn/ptit/libman8/controller/RegisterCardServlet8.java
REMOVED src/main/java/vn/ptit/libman8/controller/ReturnDocumentServlet8.java
MODIFIED src/main/java/vn/ptit/libman8/dao/ReaderCardDAO8.java
MODIFIED src/main/java/vn/ptit/libman8/dao/UserDAO8.java
MODIFIED src/main/java/vn/ptit/libman8/service/RegisterCardService8.java
MODIFIED src/main/webapp/WEB-INF/views/common/Footer8.jspf
MODIFIED src/main/webapp/WEB-INF/views/common/Header8.jspf
MODIFIED src/main/webapp/WEB-INF/views/common/HomePage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/libraryStaff/ConfirmReturnPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/libraryStaff/ReturnDocumentPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/libraryStaff/ReturnResultPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/libraryStaff/SearchReaderPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/libraryStaff/StaffDashboardPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/reader/ConfirmCardPage8.jsp
MODIFIED src/main/webapp/WEB-INF/views/reader/RegisterCardPage8.jsp
MODIFIED src/test/java/vn/ptit/libman8/service/HttpFlowTest8.java
MODIFIED src/test/java/vn/ptit/libman8/service/MySqlIntegrationTest8.java
MODIFIED docs/DEMO.md
MODIFIED docs/DESIGN_MAPPING.md
MODIFIED docs/TEST_REPORT.md
MODIFIED scripts/build.ps1
MODIFIED README.md
NEW src/main/java/vn/ptit/libman8/controller/RegisterReaderCardController8.java
NEW src/main/java/vn/ptit/libman8/controller/ReturnDocumentsController8.java
NEW src/main/webapp/WEB-INF/views/libraryStaff/findReaderCard8.jsp
NEW src/main/webapp/WEB-INF/views/libraryStaff/libraryStaffMain8.jsp
NEW src/main/webapp/WEB-INF/views/libraryStaff/returnSlip8.jsp
NEW src/main/webapp/WEB-INF/views/libraryStaff/selectReturnDocuments8.jsp
NEW src/main/webapp/WEB-INF/views/reader/readerMain8.jsp
NEW src/main/webapp/WEB-INF/views/reader/registerReaderCard8.jsp
NEW docs/ALIGNMENT_2026-11-16.md
NEW docs/INSTALLATION_REPORT.md
~~~
