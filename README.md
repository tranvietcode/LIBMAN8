# LibMANPTTK8
Ứng dụng giữa kỳ môn Phân tích và thiết kế hệ thống thông tin. Java + JSP + Servlet + JDBC + MySQL, kiến trúc view → controller → service → DAO; Maven WAR.

## Môi trường đã kiểm tra
- Windows, workspace D:\LIBMANPTTK ban đầu trống; không có AGENTS.md ở workspace hoặc D:\.
- Oracle JDK 27.0.1.0.2; biên dịch target Java 17 (không dùng tính năng mới hơn).
- Maven 3.9.11 và Apache Tomcat 10.1.48 được tải vào .tools (không sửa cài đặt hệ thống).
- Jakarta Servlet 6.0, JSP 3.1; không trộn javax.servlet.
- MySQL Server 8.0.39 localhost:3306, root; đã kiểm tra JDBC thực tế.
- Tương thích nhánh Tomcat: [tài liệu Apache](https://tomcat.apache.org/migration-10.1.html). Maven: [release 3.9.11](https://maven.apache.org/docs/3.9.11/release-notes.html).

## Mở ứng dụng đã triển khai
URL: http://localhost:8080/libman8/

Tài khoản **demo**, không phải mật khẩu MySQL: **librarian8 / ThuThu8!Demo**. Database chỉ lưu BCrypt hash cost 12. Tài khoản này chỉ dành cho bài demo local.

Các bước cấu hình, tạo schema, seed, build và chạy đã được thực hiện trong phiên bàn giao; không cần chạy lại nếu ứng dụng đang hoạt động. Xem docs/TEST_REPORT.md cho kết quả và giới hạn.

## Cấu hình MySQL (khi cài trên máy khác)
Trong VS Code mở thư mục D:\LIBMANPTTK. Sao chép config/db.properties.example thành config/db.local.properties nếu chưa có. File local bị .gitignore bỏ qua, không được đóng gói vào WAR. Điền mật khẩu sau db.password= và Ctrl+S. Không nhập mật khẩu vào source, lệnh terminal hoặc Git. Java properties cần viết dấu backslash thành hai dấu backslash; ký tự Unicode được đọc UTF-8.

Có thể dùng biến môi trường LIBMAN_DB_URL, LIBMAN_DB_USER, LIBMAN_DB_PASSWORD. LIBMAN_CONFIG là đường dẫn file local tuyệt đối; JVM property libman.config là lựa chọn khác. Biến môi trường ưu tiên hơn file. Mặc định file tương đối config/db.local.properties; script run.ps1 tự đặt đường dẫn tuyệt đối.

## Build / tạo dữ liệu / chạy trên Windows
Tất cả lệnh dưới chạy trong PowerShell tại **D:\LIBMANPTTK**:

~~~powershell
cd D:\LIBMANPTTK
# Chỉ khi JAVA_HOME chưa phù hợp; thay bằng đường dẫn JDK thực tế của bạn.
$env:JAVA_HOME='C:\Program Files\Java\jdk-27.0.1.0.2'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build.ps1
~~~

Kết quả: target/libman8.war. Maven cần Internet ở lần tải dependency đầu; cache đặt trong .tools/m2. Nếu .tools không có khi sao chép project, cài Maven 3.9.11 và đặt trong PATH, hoặc tải [Maven zip](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip) vào .tools rồi giải nén. Tomcat: tải [Tomcat zip chính thức](https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.48/bin/apache-tomcat-10.1.48.zip) và giải nén thành .tools/apache-tomcat-10.1.48.

Tạo schema/seed bằng **một** trong hai cách:

1. MySQL Workbench: kết nối localhost:3306 root → File / Open SQL Script → sql/schema.sql → Execute All; tiếp theo mở và Execute All sql/seed.sql. Refresh Schemas để thấy libman8. Không cần tạo DB bằng tay trước.
2. Sau khi build, chạy công cụ JDBC local (đọc mật khẩu từ cấu hình):

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/init-db.ps1
~~~

Script dùng CREATE IF NOT EXISTS và insert có điều kiện theo mã nghiệp vụ; không DROP/TRUNCATE hay reset dữ liệu. Chạy seed lại giữ nguyên ngày và trạng thái đã trả. Ngày tương đối chỉ lấy khi tạo bản ghi lần đầu; muốn demo sau nhiều ngày hãy chọn dữ liệu theo ngày thực tế. Không có script reset tự động.

Chạy Tomcat nền (cửa sổ ẩn), deploy WAR vào Tomcat local:

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run.ps1
~~~

Chờ Tomcat khởi động (lần đầu có thể khoảng 20 giây), mở http://localhost:8080/libman8/. Nếu cổng 8080 đang dùng, script báo lỗi; không dừng tiến trình khác. Log ở .tools/tomcat-stderr.log và .tools/apache-tomcat-10.1.48/logs/.

Dừng Tomcat local khi cần:

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/stop.ps1
~~~

Deploy sang Tomcat 10.1 khác: đặt LIBMAN_CONFIG thành đường dẫn tuyệt đối file local trong môi trường tiến trình Tomcat; copy target/libman8.war vào CATALINA_BASE/webapps rồi khởi động Tomcat. Không dùng Tomcat 9 (javax.servlet). Nếu dùng JDK 17/21/25 thay JDK hiện có, giữ target 17.

## Kiểm thử
Unit test không cần DB: build.ps1 chạy FineTest8; các test phụ thuộc môi trường sẽ được đánh dấu SKIPPED khi chưa bật.

Tích hợp MySQL thật, cần schema và seed đã tạo:

~~~powershell
cd D:\LIBMANPTTK
$env:LIBMAN_MYSQL_TESTS='true'
# Chỉ bật HTTP test khi Tomcat đã chạy WAR mới nhất.
$env:LIBMAN_HTTP_TESTS='true'
& .\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=D:\LIBMANPTTK\.tools\m2' -B verify
~~~

HTTP URL mặc định 127.0.0.1:8080/libman8; thay bằng LIBMAN_TEST_URL nếu cần. Các test tạo dữ liệu có UUID riêng rồi xóa đúng dữ liệu do test tạo; không xóa DB hoặc thay đổi seed demo. Không chạy cùng lúc thao tác demo trên dữ liệu test. Hai test rollback dùng lỗi SQLException được tiêm tại JDBC prepareStatement, trong khi các thao tác trước đó thực sự chạy trên MySQL; không dùng DB giả. Xem target/surefire-reports/.

## Nghiệp vụ và cấu trúc

**Xuất hóa đơn phạt:** mở phiếu trả đã lưu → **Export Invoice** → chọn **Save as PDF** hoặc máy in trong hộp thoại trình duyệt. Nút chỉ có khi phiếu đã có FineInvoice. Xuất/in lại chỉ đọc dữ liệu và không tạo giao dịch mới. Không cần mở MySQL Workbench; dịch vụ MySQL Server và Tomcat cần đang chạy.
Giao diện, validation và phiếu in đã chuyển sang tiếng Anh. Dữ liệu tên người, địa chỉ, nhan đề, ghi chú giữ nguyên. Đối chiếu trước/sau và các điểm thiết kế thiếu nằm ở docs/REVIEW_NOTES.md; truy vết trường và FK ở docs/DESIGN_MAPPING.md.

- Đăng ký: /register, không yêu cầu đăng nhập trước. Form mới nhập username/password/cardCode cùng thông tin bạn đọc. Password băm BCrypt; kiểm tra trùng username, email, studentCode, cardCode. Continue không ghi DB; Confirm tạo User/Reader/Card trong cùng transaction, hết hạn +12 tháng.
- Trả: /staff/returns, cần thủ thư. Tìm mã SV hoặc thẻ; cảnh báo hết hạn vẫn được trả; tính phạt 5.000 VND/ngày/cuốn là **giả định demo**, khai báo tại PolicyConfig8.DAILY_LATE_FEE. Mức phí được lưu ở ReturnDetail cho lịch sử.
- Tiền phạt hỏng do thủ thư nhập, không âm. GOOD phải bằng 0. Có phạt cần tick đã thu đủ tiền mặt; không phạt không tạo hóa đơn.
- In: nút Print Receipt mở hộp in của trình duyệt. In lại bằng số phiếu đã lưu, chỉ GET đọc dữ liệu; quyền thủ thư vẫn được kiểm tra.
- Không có UI nhập tài liệu/cho mượn; seed phục vụ module trả. Độc giả vừa đăng ký chưa có lượt mượn là đúng phạm vi.

Chi tiết: docs/DESIGN_MAPPING.md, docs/DEMO.md, docs/TEST_REPORT.md; báo cáo copy phần 5.1/5.2 tại docs/INSTALLATION_REPORT.md.
