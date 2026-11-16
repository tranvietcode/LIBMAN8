# Demo và chạy LibMan 8 — cập nhật 16/11/2026

## Môi trường / URL

Thư mục làm việc: **D:\LIBMANPTTK**. Java target 17 (máy hiện tại JDK 27.0.1.0.2), Maven 3.9.11, Tomcat 10.1.48, Jakarta Servlet 6, MySQL 8.0.39. Context **/libman8**.

| Chức năng | URL / HTTP method |
|---|---|
| Trang bạn đọc | GET http://localhost:8080/libman8/ hoặc /libman8/home |
| Register for a Reader Card | GET /libman8/register; POST action=continue/edit/cancel/confirm |
| Đăng nhập nhân viên | GET/POST /libman8/login |
| Đăng xuất | POST /libman8/logout |
| Trang nhân viên | GET /libman8/staff/dashboard |
| Return Documents | GET /libman8/staff/returns; tìm bằng keyword=cardCode hoặc studentCode |
| Preview / Back / confirm | POST /libman8/staff/returns, action=calculate/edit/cancel/confirm |
| Return Slip / in lại | GET /libman8/staff/returns?receipt=<id> |
| Hóa đơn phạt | GET /libman8/staff/returns?receipt=<id>&document=invoice[&print=true] |

Các JSP nằm dưới WEB-INF, không mở trực tiếp bằng URL .jsp. File .tools/invoice-demo.html chỉ là bản chụp HTML cục bộ của hóa đơn đã lưu; dùng website live cho nghiệp vụ.

## Cấu hình kết nối và nạp seed

Mở config/db.local.properties (đã gitignore) bằng VS Code; khi cài trên máy khác sao chép config/db.properties.example và điền mật khẩu tại máy. Ví dụ giá trị mẫu:

~~~properties
db.url=jdbc:mysql://localhost:3306/libman8?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
db.user=root
db.password=<YOUR_LOCAL_PASSWORD>
~~~

Không ghi mật khẩu MySQL thật vào source/log/báo cáo. Dịch vụ MySQL80 phải Running; không cần mở MySQL Workbench khi dùng website. Workbench chỉ cần khi muốn xem dữ liệu hoặc chạy SQL bằng GUI.

Database đã có 14 bảng/dữ liệu, không cần tạo lại. Ở máy mới, mở sql/schema.sql trong Workbench và Execute All, sau đó sql/seed.sql; hoặc build xong chạy scripts/init-db.ps1. Seed dùng INSERT có điều kiện, không xóa/reset DB và không đưa sách đã trả về ON_LOAN. Không có stored procedure được gọi.

## Build, deploy và chạy Windows

Lệnh dưới chạy trong PowerShell tại D:\LIBMANPTTK. Chỉ chạy khi cần build/chạy lại; phiên hoàn thiện đã deploy bản mới.

~~~powershell
cd D:\LIBMANPTTK
$env:JAVA_HOME='C:\Program Files\Java\jdk-27.0.1.0.2'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
~~~

Đầu ra target/libman8.war. build.ps1 thực hiện clean package để bỏ class Servlet tên cũ sau rename; Maven cache trong .tools/m2. Nếu Tomcat local đang chạy và cần khởi động lại bản mới:

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run.ps1
~~~

Nếu Tomcat đang tắt, chỉ cần run.ps1; chờ khoảng 15–20 giây rồi mở /libman8/. Khi deploy nóng cho Tomcat đang chạy, có thể copy target/libman8.war vào .tools/apache-tomcat-10.1.48/webapps/libman8.war và chờ redeploy. Không chạy lại schema/seed để khởi động ứng dụng. Log tại .tools/tomcat-stderr.log.

## Module 1 — Register for a Reader Card

1. Trang chính → Register for a Reader Card. Không cần đăng nhập hoặc có tài khoản trước.
2. Nhập username mới (3–80 chữ/số/dot/_/-), password ít nhất 8 ký tự và tối đa 72 byte UTF-8, cardCode mới (3–50 chữ/số/_/-), fullName, email, studentCode, dateOfBirth và phone. Academic Year/Address không bắt buộc. Dùng mã riêng, ví dụ username reader_demo_001 và cardCode RC-DEMO-001; đổi số nếu đã dùng.
3. Thử cardCode DEMO-CARD-1 hoặc username đã đăng ký: lỗi hiển thị ở trường tương ứng; dữ liệu thông thường được giữ, password không điền lại. Thử email/mã sinh viên trùng cũng bị chặn.
4. Continue → Review Your Details. Chưa có INSERT vào DB. Password chỉ giữ hash trong draft và không hiển thị trên màn hình xác nhận.
5. Back / Edit Details quay lại dữ liệu cũ, nhập lại password khi gửi form; Cancel không lưu. Confirm Registration mới lưu User → Reader → Card trong cùng transaction.
6. Kết quả hiển thị đúng cardCode nhập, issueDate hôm nay, expiryDate +12 tháng, ACTIVE. Quy tắc 12 tháng có căn cứ trong yêu cầu ban đầu và được triển khai thật bằng plusMonths(12).
7. Refresh/gửi Confirm lặp không tạo thêm. Có thể tra cứu thẻ mới từ khu vực nhân viên; chưa có lượt mượn là bình thường vì không xây UI cho mượn.

Độc giả seed/cũ có username/passwordHash NULL vẫn được giữ và vẫn tìm/trả tài liệu được. Module chỉ có đăng nhập nhân viên; chưa xây trang đăng nhập bạn đọc ngoài phạm vi hai nghiệp vụ.

## Module 2 — Return Documents / Return Slip

Đăng nhập tài khoản thủ thư demo nêu trong README → Return Documents. Tài khoản này dành riêng cho ứng dụng local; mật khẩu MySQL là cấu hình khác.

| Tìm kiếm | Dữ liệu seed ban đầu | Mục đích demo |
|---|---|---|
| SV001 / DEMO-CARD-1 | COPY-DEMO-1 và COPY-DEMO-2 cùng đầu sách | Chọn một cuốn để chứng minh trả một phần, nhận diện bằng copyCode/loanDetailId |
| SV002 / DEMO-CARD-2 | COPY-DEMO-3, dueDate = ngày seed -3 | Quá hạn |
| SV003 / DEMO-CARD-3 | COPY-DEMO-4, dueDate = ngày seed +7 | Nhập DAMAGED và phạt hư hỏng, ví dụ 20.000 |
| SV004 / DEMO-CARD-4 | COPY-DEMO-5, dueDate = ngày seed -5 | Quá hạn và hư hỏng |
| SV005 / DEMO-CARD-5 | Không có lượt mượn | Danh sách trống |
| SV006 / DEMO-CARD-6 | Thẻ hết hạn, COPY-DEMO-6 có lượt mượn lúc seed | Cảnh báo thẻ hết hạn; vẫn trả nếu lượt mượn chưa được xử lý |

Ngày seed là ngày chạy lần đầu; seed lại không đổi dueDate. Vì đã có thao tác demo và thời gian trôi qua, danh sách và tiền phạt hiện tại phải tính từ DB, không mặc định các số tiền tại ngày seed. Những cuốn đã trả không xuất hiện nữa; không reset để demo lại.

1. Nhập/quét mã thẻ vào ô Card Code or Student Code (máy quét dạng bàn phím tương đương nhập text). Tìm thẻ không tồn tại: Reader not found. SV005: no outstanding loans.
2. Chọn một hoặc nhiều copy ON_LOAN. Chọn GOOD/DAMAGED và nhập damageFine/note theo từng dòng. GOOD bắt buộc damageFine=0.
3. Calculate Fine & Preview: chỉ xem trước, không ghi DB. overdueDays=max(0, ngày trả - dueDate); trùng dueDate phạt 0. Mức 5.000 VND/ngày/cuốn là giả định demo giữ tại PolicyConfig8.DAILY_LATE_FEE; tiền BigDecimal và dailyLateFee được lưu snapshot trong chi tiết khi Confirm.
4. Back / Edit Selection quay lại đúng thẻ và giữ checkbox/condition/damageFine/note. Sửa lựa chọn rồi Calculate lại. Cancel Draft hủy mà không lưu.
5. Có phạt thì phải tick đã thu đủ CASH. Complete Return/Confirm kiểm tra lại ownership, ON_LOAN, ngày và phạt; lưu/update cùng transaction. Thiếu thu tiền bị từ chối.
6. Sau commit mở Return Slip theo ID. GOOD → AVAILABLE; DAMAGED → UNDER_REPAIR; lượt mượn RETURNED. Trả một phần thì các cuốn còn lại vẫn ON_LOAN. Không phạt không tạo FineInvoice.
7. Gửi Confirm lại không thêm phiếu. Print Receipt/Print Return Slip in dữ liệu đã lưu; mở lại qua mục Reprint a Saved Receipt để in lại. Export Invoice chỉ xuất hiện khi có FineInvoice; chọn Save as PDF hoặc máy in trong hộp thoại browser. Không ghi thêm giao dịch khi in/xuất.
8. Log Out rồi vào URL /staff/returns hoặc phiếu phải chuyển tới login. Footer bài giữa kỳ đã được xóa khỏi các trang.

## Kiểm thử tự động / căn cứ báo cáo

~~~powershell
cd D:\LIBMANPTTK
$env:LIBMAN_MYSQL_TESTS='true'
$env:LIBMAN_HTTP_TESTS='true'
& .\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=D:\LIBMANPTTK\.tools\m2' -B verify
~~~

Cần MySQL và Tomcat đã chạy WAR mới. Test tạo fixture UUID riêng, dọn đúng dữ liệu test và không tiêu thụ seed nghiệp vụ. Kết quả tại target/surefire-reports; log hoàn thiện .tools/alignment-verification.log. Báo cáo copy mục 5.1/5.2: INSTALLATION_REPORT.md; bảng ERD/schema/DAO và tên tương thích: DESIGN_MAPPING.md mục 10. Bản ERD 10 bảng chưa có trong repo, chưa thể xác minh tên/cột một-một; DB thật 14 bảng được giữ.
