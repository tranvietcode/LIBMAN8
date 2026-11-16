# Báo cáo kiểm tra thực tế

## Hoàn thiện theo đặc tả cập nhật — 16/11/2026

- Đã đọc pom/build/run, controller/JSP/DAO/Service, schema/seed, cấu hình kết nối (không xuất mật khẩu local), test và tài liệu repo. `git status` xác nhận thư mục không phải Git repository; backup trước sửa tại .tools/alignment-backup-*.zip. Không có file ERD/UML 10 bảng trong repo.
- Schema MySQL thật được kiểm tra: 14 bảng, 14 PK, 18 FK đúng đích, 78 kiểu cột, 15 unique keys, InnoDB/utf8mb4. Schema đáp ứng username/passwordHash/cardCode và các ràng buộc nên không cần migration; schema.sql/seed.sql giữ nguyên.
- Đổi tên controller canonical thành RegisterReaderCardController8, ReturnDocumentsController8 và JSP theo readerMain8/registerReaderCard8/libraryStaffMain8/findReaderCard8/selectReturnDocuments8/returnSlip8. URL /login, /register, /staff/returns giữ nguyên; JSP cũ là include alias. Build clean đã loại bỏ hai class Servlet tên cũ, tránh trùng mapping.
- Form đăng ký mới nhập username/password/cardCode; Service băm BCrypt, kiểm tra định dạng/trùng username/cardCode cùng email/studentCode, transaction User→Reader→Card, lỗi rollback. Password không điền lại ở form lỗi hoặc confirmation. Seed/độc giả cũ có credentials NULL vẫn dùng được.
- Preview trả có Back / Edit Selection giữ lựa chọn, tình trạng, damageFine và note. Footer yêu cầu đã xóa khỏi template dùng chung; nhãn chức năng bằng tiếng Anh. Không đổi cách tính phạt/lưu phiếu/in lại.
- Maven `clean package -DskipTests`: BUILD SUCCESS. Lần offline đầu thiếu dependency/metadata clean plugin, đã tải phần cần thiết từ Maven Central rồi build sạch thành công; không đổi kiến trúc/dependency ứng dụng.
- Deploy WAR lên Tomcat hiện có và chờ form mới xuất hiện. Maven verify bật LIBMAN_MYSQL_TESTS=true và LIBMAN_HTTP_TESTS=true: **24 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS** (FineTest8: 1, HttpFlowTest8: 2, MySqlIntegrationTest8: 21). Log .tools/alignment-build.log và .tools/alignment-verification.log; target/surefire-reports.
- Kiểm thử bổ sung thực tế: đăng ký tài khoản/mã thẻ thành công và đọc lại BCrypt; lỗi username/cardCode trùng trên Service/HTTP; rollback hồ sơ/tài khoản khi lưu Card thất bại; không lưu trước Confirm; password không xuất hiện trong HTML; footer biến mất; Back giữ dữ liệu trả; trả một phần với hai copy cùng đầu sách; dueDate bằng ngày trả có phạt 0. Các test cũ về sai thẻ/không có sách, quá hạn/hỏng, thu tiền, gửi lặp/cạnh tranh, rollback trả và xuất/in lại vẫn pass.
- Snapshot trước–sau .tools/alignment-db-before.txt / alignment-db-after.txt khớp dữ liệu các dòng và cấu trúc cột; không reset/xóa dữ liệu cũ. Fixture riêng được dọn, AUTO_INCREMENT có thể tăng theo test bình thường.
- Chưa QA trực quan hoặc bấm in vật lý. Truy cập/điều hướng/form/phiếu/in lại được kiểm tra HTTP thật. ERD 10 bảng chưa có tên/cột nguồn nên chỉ ánh xạ theo vai trò, chưa thể tuyên bố đối chiếu một-một với bản ERD đó. Xem DESIGN_MAPPING.md mục 10 và INSTALLATION_REPORT.md để copy báo cáo 5.1/5.2.

## Bổ sung xuất/in hóa đơn — 04/11/2026

- MySQL80 đang Running; Tomcat ban đầu chưa chạy, đã khởi động bằng scripts/run.ps1. Không cần mở Workbench để chạy ứng dụng.
- Đã thêm Export Invoice trên ReturnResultPage8.jsp cho phiếu có FineInvoice. Chế độ document=invoice dùng lại JSP/Servlet/Service hiện có, lấy hóa đơn từ DB, hiển thị chi tiết và mở window.print() nếu print=true. Save as PDF là lựa chọn trong hộp thoại in của trình duyệt, không phải endpoint PDF server.
- Đã chạy Maven verify trên MySQL/Tomcat thật: **21 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS**. Log .tools/invoice-verification.log; target/surefire-reports.
- HTTP tests bổ sung: có nút Export Invoice khi có phạt; hóa đơn trả 200, đúng mã/nội dung/tổng; auto-print script có mặt khi print=true; xuất/in lại giữ số phiếu và số hóa đơn; không phạt không có nút và ép URL invoice trả 404; chưa đăng nhập bị chuyển tới login.
- Đã lưu phản hồi HTML thật của hóa đơn có sẵn thành .tools/invoice-demo.html với CSS nhúng; nguồn tại .tools/invoice-demo-info.txt. Không tạo thêm nghiệp vụ demo trên dữ liệu có sẵn. Fixture kiểm thử được tạo/dọn riêng.
- Chưa mở hộp thoại in trực tiếp bằng UI: Computer Use dừng vì không xác định chắc URL trình duyệt. Không tuyên bố đã in vật lý hoặc đã lưu PDF bằng hộp thoại.
- Helper demo mới chứa mật khẩu đã bị auto-review từ chối và không được tạo. Đã dùng phiên HTTP test có sẵn để lấy HTML, không thêm mật khẩu vào helper/source/tài liệu. Schema, seed và model không đổi.

## Kết quả mới nhất — rà soát hợp đồng và UI tiếng Anh (03/11/2026)

Đã đọc và đối chiếu hợp đồng ban đầu, DESIGN_MAPPING.md, các tài liệu trong workspace, schema/seed, 16 entity, DAO/Service/Servlet/Filter/config và toàn bộ JSP của hai module. Bảng chênh lệch trước sửa và phần chưa được chốt trong thiết kế nằm ở [REVIEW_NOTES.md](REVIEW_NOTES.md); truy vết từng trường và FK ở [DESIGN_MAPPING.md](DESIGN_MAPPING.md).

Đã sửa association DocumentCopy8.importDetail.document, bỏ hai helper dư thừa document/loanDate, nạp đúng Staff subtype/thuộc tính; đồng bộ returnCondition_/damageFine_ giữa JSP–Servlet–test; xử lý null condition bằng validation; gắn lại lỗi trường khi confirm đăng ký lỗi. UI/server validation/phiếu/bản in chuyển sang tiếng Anh, lang=en. Trạng thái thẻ và phương thức thanh toán hiển thị từ entity thay vì literal. Không dịch dữ liệu tên người, địa chỉ, nhan đề hoặc ghi chú; schema.sql/seed.sql không đổi.

| Kiểm tra đã chạy trên bản cuối | Kết quả |
|---|---|
| Maven offline verify, JDK 27 target 17, WAR | BUILD SUCCESS |
| FineTest8 | 1 pass |
| MySqlIntegrationTest8, MySQL 8.0.39 thật | 18 pass |
| HttpFlowTest8, Tomcat thật | 2 pass |
| Tổng | **21 tests; 0 failures; 0 errors; 0 skipped** |
| Metadata DB | 14 PK, 18 FK đúng đích, 78 kiểu cột, 15 unique keys; InnoDB/utf8mb4 |
| Source contract audit | 16 entity khớp thuộc tính/kế thừa/constructor + 2 helper đã ghi riêng; hậu tố 8 đúng; không SQL sai tầng/tham chiếu cũ/UI tiếng Việt trong src/main |
| Bảo toàn dữ liệu | Snapshot số dòng/hash dữ liệu và cấu trúc trước–sau khớp; không migration/reset |

Lệnh đã chạy tại D:\LIBMANPTTK: đặt LIBMAN_MYSQL_TESTS=true, LIBMAN_HTTP_TESTS=true rồi Maven `-Dmaven.repo.local=D:\LIBMANPTTK\.tools\m2 -o -B verify`. Bằng chứng: target/surefire-reports, .tools/review-verification.log, .tools/review-metadata.txt, .tools/review-source-audit.json, .tools/review-db-before.txt và review-db-after.txt. WAR được deploy lại vào Tomcat local.

Ngoài các kiểm thử nghiệp vụ cũ (đăng ký/trùng/rollback; đúng hạn/quá hạn/hỏng; sai ownership; trả lặp/cạnh tranh; thiếu thu tiền; rollback trả; in lại không ghi), đã xác minh:

- Full Name, Date of Birth, Email, Phone, Academic Year, Address và Card đọc lại đúng từ MySQL; độc giả đăng ký không cần username/passwordHash.
- Continue/preview không lưu, Edit giữ dữ liệu, Confirm mới ghi; field error tiếng Anh, CSRF và escape vẫn hoạt động.
- ImportDetail8 được nạp (id/quantity/unitPrice/document), Document scalar/title và Staff/position đọc đúng; copy.documentId và import detail.documentId không lệch trong dữ liệu thực.
- HTML dùng returnCondition_<id>, damageFine_<id>; thiếu tình trạng trả nhận validation tiếng Anh, không HTTP 500, lượt mượn vẫn ON_LOAN.
- Nhãn Loan Date lấy từ LoanSlip8, nhan đề đi qua importDetail.document; ghi chú/nhan đề tiếng Việt của fixture giữ nguyên trong phiếu cuối. Fine Invoice, CASH, tiền và nút in/in lại được kiểm tra qua HTTP.

Giới hạn: chưa QA trực quan trong trình duyệt, chưa mở hộp thoại in hoặc in vật lý; native browser validation theo locale trình duyệt. Hai helper ngoài thuộc tính gốc (LoanDetail8.cardId, ReturnDetail8.returnDate) vẫn cần cho ownership/ngữ cảnh tính dẫn xuất và không lưu cột mới. Thiết kế chưa chốt đầy đủ kiểu trả về API, nên ghi rõ chữ ký triển khai, không tự định nghĩa lại thiết kế. Không có UML độc lập trong workspace. Test lỗi ghi dùng fault injection JDBC trên MySQL thật; không phải kiểm thử mất điện/network giữa COMMIT. Fixture được dọn riêng; AUTO_INCREMENT có thể tăng bình thường, không reset. Truy vấn snapshot bị Windows hủy TLS trong sandbox được chạy lại thành công ngoài sandbox, không thay đổi cấu hình TLS/DB.

## Lịch sử các lần triển khai trước

## Cập nhật xử lý phản hồi đăng nhập — 03/11/2026 09:14
- Kiểm tra lại MySQL: librarian8 có quyền LIBRARY_STAFF, BCrypt khớp mật khẩu demo đã công bố; đăng nhập HTTP chính xác trả 302 tới /libman8/staff/dashboard. Chưa tái hiện được lỗi trong ảnh người dùng, không đặt lại mật khẩu hoặc sửa tài khoản trong DB.
- Sửa form giữ tên đăng nhập khi thất bại, chuẩn hóa khoảng trắng đầu/cuối tên đăng nhập; nếu mật khẩu có khoảng trắng thừa và xác thực thất bại thì báo rõ. Không tự cắt khoảng trắng mật khẩu, không hiển thị lại mật khẩu.
- Bổ sung HTTP test cho tình huống này và chạy lại verify: **20 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS lúc 09:14:56. Log: .tools/login-verification.log; báo cáo mới: target/surefire-reports/.

Ngày 03/11/2026, múi giờ Asia/Ho_Chi_Minh. Lần verify đầy đủ kết thúc 08:40, BUILD SUCCESS.

## Môi trường và hành động đã thực hiện
- Workspace D:\LIBMANPTTK ban đầu trống, không tìm thấy AGENTS.md tại workspace và thư mục cha D:\; không ghi đè project cũ. Không có Git repository ban đầu; không init/commit/push.
- JDK chính: Oracle 27.0.1.0.2. Maven 3.9.11, target Java 17; Tomcat 10.1.48, Jakarta Servlet 6.0. Formatter dùng JBR 25 có sẵn trong Android Studio vì formatter không tương thích JDK 27; quá trình biên dịch/test/chạy Tomcat vẫn dùng JDK chính.
- MySQL 8.0.39 localhost:3306 root: cổng TCP mở và JDBC xác thực thành công bằng file local do người dùng điền. Trước triển khai chưa tồn tại libman8.
- Đã chạy sql/schema.sql và sql/seed.sql qua JDBC trên MySQL thật, tạo đủ 14 bảng InnoDB utf8mb4. Không DROP/TRUNCATE database.
- Đã build target/libman8.war và deploy Tomcat local. Trang chủ, form đăng ký và các màn hình nghiệp vụ trả được gọi qua HTTP thật.
- Cấu hình local được .gitignore bỏ qua; đã kiểm tra danh sách entry WAR không có db.local.properties, config mẫu, test classes, .tools hoặc credentials. Mật khẩu MySQL không được đưa vào source/tài liệu/log.

## Lệnh verify đã chạy
Thư mục làm việc D:\LIBMANPTTK, bật LIBMAN_MYSQL_TESTS=true và LIBMAN_HTTP_TESTS=true:

~~~powershell
& .\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=D:\LIBMANPTTK\.tools\m2' -o -B verify
~~~

| Nhóm | Đã chạy | Failure / Error / Skip |
|---|---:|---:|
| FineTest8 | 1 | 0 / 0 / 0 |
| MySqlIntegrationTest8 | 17 | 0 / 0 / 0 |
| HttpFlowTest8 | 1 luồng xuyên suốt với nhiều assertions | 0 / 0 / 0 |
| Tổng | **19** | **0 / 0 / 0** |

Bằng chứng máy: target/surefire-reports/*.xml và *.txt; log tổng .tools/final-verification.log. Các lần đầu có skip khi chưa bật DB tests và một lỗi HTTP do encoding JSP; số trên là kết quả sau sửa. Một kỳ vọng test giữ nguyên hoa/thường của mã sinh viên cũng đã được sửa cho phù hợp normalization server.

## Nội dung đã xác minh
- Đăng ký thành công: User → Reader (userId kế thừa) → ReaderCard, mã riêng, ACTIVE, ngày +12 tháng.
- Kiểm tra dữ liệu sai, email trùng, mã sinh viên trùng; form HTTP giữ dữ liệu khi Edit, trường email có lỗi; escape chuỗi script.
- Continue không tạo tblUser; Confirm mới ghi, gửi Confirm lặp không thêm bản ghi; thiếu CSRF bị 403.
- Rollback đăng ký: thực thi insert User/Reader trên MySQL, tiêm SQLException ngay khi chuẩn bị insert Card, sau đó xác nhận không còn User/Reader.
- Trả đúng hạn không phạt/không invoice; quá hạn; hư hỏng; đồng thời quá hạn và hư hỏng. Kiểm tra tổng từ dữ liệu lưu và trạng thái Copy/LoanDetail.
- Chọn lượt mượn thẻ khác bị chặn; chọn ID lặp bị chặn; GOOD có tiền hỏng hoặc tiền âm bị chặn.
- Thiếu xác nhận thu tiền bị chặn và không đổi trạng thái. HTTP gửi totalFine=0/staffId=0 không ảnh hưởng dữ liệu server.
- Hai kết nối/threads đồng thời trả cùng LoanDetail: đúng một thành công, chỉ một ReturnDetail. Trả lặp sau thành công bị chặn.
- Ngày trả thay đổi hoặc dueDate đổi làm tổng phạt đổi: bị yêu cầu preview lại, không lưu.
- Rollback trả: các insert ReturnSlip/ReturnDetail và update LoanDetail/Copy chạy thật, tiêm SQLException ở prepareStatement insert FineInvoice; xác minh toàn bộ rollback.
- Nhiều cuốn cùng phiếu trả: cập nhật cả hai cuốn và chỉ một invoice.
- Gọi lại getReceipt và HTTP mở/in lại không làm tăng số ReturnSlip. Sau logout, URL phiếu chuyển tới đăng nhập. GET trực tiếp JSP dưới WEB-INF trả 404.
- Độc giả không có sách, không tìm thấy độc giả, cảnh báo thẻ hết hạn được xác minh qua HTTP.
- Seed chạy thêm hai lần và so sánh snapshot toàn bộ 14 bảng: dữ liệu không đổi. Test chỉ dọn fixture UUID do chính test tạo, không tiêu thụ các cuốn seed demo.
- Rà soát source: mọi lớp tự viết (kể cả lớp lồng ở test) có hậu tố 8; không FullName8/Address8/Member8/Employee8; không javax.servlet. Không SQL ở controller/model/JSP. DAO không commit/rollback/đóng Connection sở hữu bởi service.

## Giới hạn đã biết
- Đã xác minh giao diện bằng HTML/JSP qua HTTP, chưa hoàn tất kiểm tra trực quan bằng trình duyệt: browser host không khả dụng, công cụ Computer Use dừng do không xác định chắc URL hiện tại. Không tự tiếp tục thao tác UI sau khi công cụ dừng.
- Chưa bấm hộp thoại in hoặc dùng máy in thật. Đã có nút window.print(), CSS print và kiểm tra nội dung phiếu/in lại qua HTTP. Người demo có thể in hoặc Save as PDF trong trình duyệt.
- Kiểm thử cạnh tranh dùng hai request nghiệp vụ/service trên hai kết nối MySQL thực; chống gửi lặp ở Servlet được kiểm tra HTTP tuần tự. Chưa làm kiểm thử tải lớn hoặc nhiều máy.
- Hai test lỗi ghi là fault injection tại JDBC prepareStatement; các câu SQL trước lỗi được thực thi trên MySQL thật. Không tuyên bố đã gây lỗi mất điện/network giữa COMMIT hoặc dùng DB giả.
- JDK 27 phát cảnh báo nội bộ của Maven/Guice (Unsafe/final-field); build và test vẫn pass. Chưa chạy lại trên JDK 17/21 hoặc Tomcat khác; target và API đã chọn tương thích Java 17+/Tomcat 10.1.
- Đăng ký và trả là hai demo độc lập đúng phạm vi; không có UI cho mượn hoặc nhập kho, không tự reset sách đã trả.
