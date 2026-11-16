# Rà soát theo hợp đồng thiết kế — 03/11/2026

## Cơ sở và bảng trước khi sửa

Cơ sở là hợp đồng C/D/E/F/G trong yêu cầu ban đầu và yêu cầu rà soát hiện tại. DESIGN_MAPPING.md được kiểm tra chéo, không dùng code để định nghĩa lại hợp đồng. Workspace không có tài liệu UML/thiết kế độc lập khác ngoài DESIGN_MAPPING.md, README.md, DEMO.md và TEST_REPORT.md; không tìm thấy AGENTS.md.

| Thành phần | Theo thiết kế | Trước sửa | Chênh lệch / xử lý dự kiến |
|---|---|---|---|
| DocumentCopy8 | id, copyCode, condition, status, importDetail | thêm document; importDetail chưa được ánh xạ | Bỏ association thừa; nạp ImportDetail8.document và cập nhật JSP |
| LoanDetail8 | id, copy, dueDate, status | thêm cardId, loanDate | Bỏ loanDate thừa, hiển thị LoanSlip8.loanDate; cardId là helper ownership được ghi riêng |
| ReturnDetail8 | loanDetail và các mức phạt; ngày trả thuộc ReturnSlip8 | có returnDate hỗ trợ tính dẫn xuất | Giữ ngữ cảnh không lưu DB, tách khỏi thuộc tính hợp đồng |
| Form trả | returnCondition, damageFine, note | condition_<id>, damage_<id>, note_<id> | Đổi hai prefix đầu đồng bộ JSP → Servlet → test |
| Staff8 qua DAO | subtype theo position; thuộc tính kế thừa được ánh xạ | một số truy vấn dựng LibraryStaff8 chỉ với id hoặc thiếu position/note | Dùng helper ánh xạ StaffDAO8, không thêm lớp/bảng |
| Validation returnCondition | từ chối thiếu/sai tình trạng bằng lỗi validation | Set.of(...).contains(null) có thể gây NullPointerException | Kiểm tra null rõ ràng |
| Lỗi confirm đăng ký | giữ dữ liệu và lỗi tương ứng trường | IllegalArgumentException chỉ có thông báo chung | Nạp lại lỗi trường khi confirm thất bại |
| Nội dung hiển thị | tiếng Anh theo yêu cầu mới | tiếng Việt trong JSP/server errors | Dịch UI và validation; giữ nguyên seed/tên/địa chỉ/nhan đề thực |
| Lớp / package / kế thừa / mảng | hậu tố 8, vn.ptit.libman8, entity[] | phù hợp | Giữ nguyên tên/API đã chốt |
| Schema / PK / FK / tiền dẫn xuất | 14 bảng; không cột tổng/phạt dẫn xuất | chưa thấy sai trong schema.sql | Kiểm tra metadata MySQL thật; không migration nếu khớp |

## Điểm thiếu hoặc mâu thuẫn của tài liệu

- Hợp đồng ban đầu chốt tên phương thức và đối số nhưng không chốt đầy đủ kiểu trả về (ngoại trừ UserDAO8.insert trả generated id). Bảng chữ ký trong DESIGN_MAPPING.md phải ghi đó là chữ ký triển khai đã xác minh, không tuyên bố chúng là yêu cầu ban đầu.
- Tài liệu cũ đưa helper DocumentCopy8.document, LoanDetail8.cardId/loanDate, ReturnDetail8.returnDate vào danh sách “thuộc tính chính thức”. Chúng không thuộc danh sách entity ban đầu. Hai helper dư thừa được bỏ; cardId và returnDate được giữ riêng vì cần ownership/tính dẫn xuất và không thêm cột.
- ReturnDetail8 cần ngày trả để có getter overdueDays/lateFine/totalFine, nhưng hợp đồng chỉ đặt returnDate tại ReturnSlip8. Ngữ cảnh returnDate không lưu DB là chỉnh tối thiểu đã nêu ở lần triển khai trước; không đổi bảng hay API nghiệp vụ.
- Khóa documentId tồn tại trực tiếp ở tblDocumentCopy dù Java không có DocumentCopy8.document trong hợp đồng; Java dùng importDetail.document. Không xóa cột/FK này. Nạp nhan đề qua association và kiểm tra documentId nhất quán trong dữ liệu liên quan.

## Sửa đổi đã thực hiện

- Giữ tên 16 entity, kế thừa, package, public entity[] và các API nghiệp vụ; bỏ association/helper dư thừa DocumentCopy8.document và LoanDetail8.loanDate, sửa đồng bộ DAO/JSP. Còn hai helper không lưu DB được phân biệt rõ với hợp đồng.
- LoanDetailDAO8 nạp ImportDetail8 và Document8 từ đúng FK, kiểm tra documentId của copy và import detail thống nhất. StaffDAO8.findById nạp subtype/thuộc tính thay cho các Staff object chỉ có id; ánh xạ cả note.
- Đổi returnCondition_/damageFine_ đồng bộ form, request và HTTP tests. Các field control (keyword, receipt, action/stage, csrf, paymentReceived) không phải thuộc tính entity và đã ghi rõ trong mapping.
- Chuyển toàn bộ UI/validation/server errors/print labels sang tiếng Anh, html lang=en; không sửa seed, tên người, địa chỉ, nhan đề hay ghi chú đã lưu.
- Chặn null returnCondition bằng validation tiếng Anh, không gây NullPointerException. Confirm đăng ký lỗi có lại errors ở trường tương ứng.
- Khi lần theo chiều đọc, phát hiện status ở kết quả đăng ký và CASH trên phiếu được viết literal. Đã bind reader.card.status và slip.fineInvoice.paymentMethod để hiển thị đúng thuộc tính entity, giữ nguyên giá trị DB.
- Viết lại DESIGN_MAPPING.md theo hợp đồng ban đầu, tách helper/chữ ký triển khai chưa được chốt kiểu trả về; ghi bảng truy vết JSP → Servlet → Service → Entity → DAO → cột và chiều đọc lại.

## Bằng chứng kiểm tra

- Source audit .tools/review-source-audit.json: 16 entity đúng thuộc tính thiết kế + 2 helper khai báo riêng; kế thừa/abstract/constructor đúng; không lớp sai hậu tố, không tham chiếu cũ condition_/damage_/copy.document/d.loanDate; không SQL ở controller/model/JSP, không chuỗi UI tiếng Việt trong src/main.
- MySQL metadata .tools/review-metadata.txt: 14 PK, 18 FK kiểm tra đúng bảng/cột đích, 78 kiểu cột, 15 unique keys, InnoDB/utf8mb4. Không cột dẫn xuất. Không cần migration.
- Snapshot .tools/review-db-before.txt và review-db-after.txt khớp: dữ liệu các dòng có sẵn và cấu trúc cột không đổi. Snapshot chỉ ghi số lượng/hash, không chép thông tin cá nhân/mật khẩu vào báo cáo. Test có tạo/xóa fixture UUID riêng nên bộ đếm AUTO_INCREMENT có thể tăng; không reset bộ đếm.
- SchemaAudit8 sau test từng bị Windows hủy TLS trong sandbox; chạy lại truy vấn chỉ đọc ngoài sandbox đã thành công. Không sửa URL/TLS/credentials hoặc cấu hình bảo mật để vượt lỗi này.
- SHA-256 sql/schema.sql và sql/seed.sql trước/sau khớp; không chạy migration, DROP, TRUNCATE hay reset DB.
- Maven verify trên MySQL thật + HTTP Tomcat: kết quả mới nhất tại target/surefire-reports và .tools/review-verification.log. Xem bản cập nhật REVIEW trong TEST_REPORT.md cho số lượng/kết quả thực tế.

## Giới hạn / chênh lệch còn lại

- Hợp đồng chưa quy định đầy đủ kiểu trả về của Service/DAO; giữ chữ ký triển khai và ghi rõ, không tự nâng thành hợp đồng mới. Hai helper ownership/ngày trả vẫn tồn tại ngoài danh sách thuộc tính gốc, được phép theo điều khoản helper tối thiểu; không thêm cột.
- Không có tài liệu UML độc lập để đối chiếu nét kết hợp/cardinality ngoài hợp đồng và schema đã chốt.
- Native browser validation theo ngôn ngữ trình duyệt; nội dung ứng dụng định nghĩa hoàn toàn bằng tiếng Anh. Dữ liệu thực có tiếng Việt vẫn hiển thị nguyên trạng.
- Kiểm tra UI bằng HTTP/JSP thật, chưa QA trực quan trên trình duyệt và chưa mở hộp thoại in/máy in thật. CSS print và nội dung phiếu/in lại được kiểm tra qua HTTP; không tuyên bố đã in vật lý.
- Test lỗi ghi dùng SQLException fault injection tại JDBC trên kết nối MySQL thật; không mô phỏng mất điện/network giữa commit. Test cạnh tranh dùng hai connection thực ở service; gửi lặp ở Servlet được kiểm tra HTTP tuần tự.

