# Các trường hợp kiểm thử tự động hóa (E2E) — Fortuna Lotto

> Kiểm thử tự động hóa toàn bộ luồng chức năng từ đầu đến cuối. Các kịch bản được tổ chức theo luồng chức năng của người dùng, thực hiện trên môi trường thực (máy chủ + cơ sở dữ liệu thật).

**Tổng cộng: 24 mục kiểm thử · 187 trường hợp kiểm thử**

### Hạ tầng kiểm thử

| Thành phần | Chi tiết |
|-----------|----------|
| Trình duyệt | Chrome headless (1920×1080) |
| Tài khoản người dùng | `user@gmail.com` / `No64Adonis*` |
| Tài khoản admin | `admin@gmail.com` / `No64Adonis*` |
| Cơ sở dữ liệu | Được khôi phục tự động bằng `seed_test_data.sql` trước mỗi lần chạy |
| Script chạy | `run_e2e_tests.bat` (tự động seed DB → biên dịch → chạy tests) |
| Timeout mặc định | 15 giây (WebDriverWait) |

### Phương thức hỗ trợ chính

| Phương thức | Mục đích |
|------------|---------|
| `loginAs(email, password)` | Đăng nhập và chờ redirect thành công |
| `tryLoginAs(email, password)` | Đăng nhập không chờ redirect — dùng cho test thông tin đăng nhập sai |
| `jsClearAndType(id, value)` | Nhập giá trị qua JavaScript — tránh lỗi `InvalidElementStateException` trên input `type="number"` |
| `tryClickButtonByText(text)` | Nhấn nút theo text — trả về `false` nếu không tìm thấy thay vì ném lỗi |
| `logout()` | Đăng xuất và chờ URL không còn chứa trang authenticated |

---

## 1. Đăng ký tài khoản (7 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Đăng ký thành công | Email mới, họ tên, mật khẩu hợp lệ | Chuyển đến trang đăng nhập với thông báo thành công | Đạt |
| 2 | Đăng ký với email đã tồn tại | Email đã có trong hệ thống | Hiển thị lỗi "Email đã được đăng ký", ở lại trang đăng ký | Đạt |
| 3 | Đăng ký với mật khẩu yếu | Mật khẩu đơn giản ("123") | Hiển thị lỗi yêu cầu mật khẩu mạnh hơn | Đạt |
| 4 | Đăng ký với mật khẩu không khớp | Mật khẩu và xác nhận mật khẩu khác nhau | Hiển thị lỗi "Mật khẩu không khớp" | Đạt |
| 5 | Đăng ký với email không hợp lệ | Email sai định dạng ("notanemail") | Hiển thị lỗi định dạng email | Đạt |
| 6 | Đăng ký để trống các trường bắt buộc | Tất cả ô trống | Hiển thị thông báo lỗi cho từng trường bắt buộc | Đạt |
| 7 | Đăng ký với mã tấn công SQL | Mã SQL injection trong ô email | Hiển thị lỗi, hệ thống không bị ảnh hưởng | Đạt |

---

## 2. Đăng nhập (6 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Đăng nhập thành công (người dùng) | Email và mật khẩu hợp lệ của user | Chuyển đến trang Xổ số | Đạt |
| 2 | Đăng nhập thành công (admin) | Email và mật khẩu hợp lệ của admin | Chuyển đến trang Quản lý người dùng | Đạt |
| 3 | Đăng nhập sai mật khẩu | Email đúng, mật khẩu sai (dùng `tryLoginAs`) | Ở lại trang đăng nhập, hiển thị lỗi | Đạt |
| 4 | Đăng nhập email không tồn tại | Email chưa đăng ký (dùng `tryLoginAs`) | Ở lại trang đăng nhập, hiển thị lỗi | Đạt |
| 5 | Đăng nhập để trống | Không nhập email và mật khẩu | Ở lại trang đăng nhập (HTML5 validation) | Đạt |
| 6 | Thông báo sau đăng ký thành công | Đăng ký tài khoản mới → quan sát trang đăng nhập | URL chứa `registrationSuccess`, hiển thị thông báo thành công | Đạt |

---

## 3. Đăng xuất (3 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Đăng xuất thành công | Đăng nhập → Nhấn đăng xuất | Chuyển về trang chủ (`index.jsp`) | Đạt |
| 2 | Truy cập trang bảo vệ sau đăng xuất | Đăng xuất → Truy cập `/userLottery` | Chuyển hướng đến trang đăng nhập | Đạt |
| 3 | Nhấn "Quay lại" sau đăng xuất | Đăng xuất → Nhấn Back → Truy cập `/myTickets` | Không truy cập được nội dung bảo vệ | Đạt |

---

## 4. Xác thực Google OAuth (4 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Nút đăng nhập Google — người dùng mới | Nhấn nút Google Login trên trang đăng nhập | Chuyển đến trang Google OAuth hoặc XHR endpoint | Đạt |
| 2 | Nút đăng nhập Google — người dùng đã tồn tại | Nhấn nút Google Login trên trang đăng ký | URL chuyển hướng chứa callback endpoint | Đạt |
| 3 | Trang thiết lập mật khẩu tồn tại | Truy cập trực tiếp `/setPassword` | Trang tồn tại (có redirect hoặc form) | Đạt |
| 4 | Hủy xác thực Google | Truy cập callback không có mã xác thực | Chuyển về trang đăng nhập | Đạt |

---

## 5. Quên mật khẩu và đặt lại mật khẩu (6 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Gửi yêu cầu đặt lại (email hợp lệ) | Email đã đăng ký | Hiển thị thông báo thành công | Đạt |
| 2 | Gửi yêu cầu (email không tồn tại) | Email chưa đăng ký | Hiển thị cùng thông báo thành công (bảo mật) | Đạt |
| 3 | Đặt lại mật khẩu thành công | Token hợp lệ, mật khẩu mới và xác nhận khớp | Thông báo thành công, chuyển đến đăng nhập | Đạt |
| 4 | Đặt lại — token hết hạn | Token đã cũ/hết hạn | Hiển thị lỗi "Liên kết đã hết hạn" | Đạt |
| 5 | Đặt lại — mật khẩu không khớp | Hai mật khẩu khác nhau | Hiển thị lỗi "Mật khẩu không khớp" | Đạt |
| 6 | Sử dụng link đặt lại hai lần | Token đã sử dụng | Hiển thị lỗi token không hợp lệ | Đạt |

---

## 6. Đổi mật khẩu — Người dùng (4 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Đổi mật khẩu thành công | Mật khẩu hiện tại đúng, mật khẩu mới hợp lệ → đổi lại về mật khẩu gốc | Thông báo thành công, mật khẩu được khôi phục | Đạt |
| 2 | Đổi mật khẩu — sai mật khẩu hiện tại | Mật khẩu hiện tại sai | Hiển thị lỗi "Mật khẩu hiện tại không đúng" | Đạt |
| 3 | Đổi mật khẩu — trùng mật khẩu cũ | Mật khẩu mới = mật khẩu hiện tại | Chấp nhận hoặc từ chối (tùy cấu hình ứng dụng) | Đạt |
| 4 | Đổi mật khẩu — mật khẩu mới yếu | newPassword="123" | Hiển thị lỗi từ trình xác thực mật khẩu | Đạt |

---

## 7. Quản lý hồ sơ người dùng (7 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Xem hồ sơ | Đăng nhập → Vào `/profile` | Hiển thị đầy đủ thông tin: tên, email, vai trò | Đạt |
| 2 | Cập nhật tên thành công | firstName="Tran", lastName="Van B" | Tên được cập nhật, header hiển thị tên mới | Đạt |
| 3 | Cập nhật tên quá dài | firstName = 60 ký tự | Hiển thị lỗi "Tên quá dài" | Đạt |
| 4 | Yêu cầu thay đổi email | newEmail hợp lệ | Thông báo "Mã xác minh đã được gửi" | Đạt |
| 5 | Xác minh mã email — mã đúng | Mã xác minh từ email | Email được cập nhật trong hồ sơ | Đạt |
| 6 | Xác minh mã email — mã sai | code="000000" | Hiển thị lỗi "Mã xác minh không hợp lệ" | Đạt |
| 7 | Thay đổi email thành email đã tồn tại | newEmail = email hiện tại | Hiển thị lỗi "Email mới giống email hiện tại" | Đạt |

---

## 8. Trang chủ và tìm kiếm công khai (12 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Xem trang chủ không đăng nhập | Mở trang chủ | Trang hiển thị đầy đủ, danh sách công ty được tải | Đạt |
| 2 | Tìm kiếm theo công ty | Chọn công ty từ bộ lọc | Chỉ hiển thị vé của công ty đã chọn | Đạt |
| 3 | Tìm kiếm theo số | Nhập num1="5", num3="10" | Hiển thị vé chứa các số đã nhập | Đạt |
| 4 | Tìm kiếm theo khoảng ngày | startDate, endDate | Chỉ hiển thị vé trong khoảng ngày | Đạt |
| 5 | Tìm kiếm theo ngày cụ thể | specificDate hợp lệ | Chỉ hiển thị vé của ngày đã chọn | Đạt |
| 6 | Phân trang kết quả | page=2 | Trang 2 hiển thị đúng | Đạt |
| 7 | Tìm kiếm không có kết quả | Bộ lọc rất hẹp | Hiển thị kết quả trống, không lỗi | Đạt |
| 8 | Lịch sử tìm kiếm — khách | Nhiều tìm kiếm liên tiếp | Lịch sử hiển thị tối đa 10 mục gần nhất | Đạt |
| 9 | Lịch sử tìm kiếm — đã đăng nhập | Đăng nhập → Tìm kiếm | Lịch sử được lưu trong CSDL | Đạt |
| 10 | Tìm kiếm từ lịch sử | Nhấn vào mục lịch sử | Bộ lọc được điền lại, kết quả hiển thị | Đạt |
| 11 | Xóa lịch sử tìm kiếm | Nhấn "Xóa lịch sử" | Lịch sử bị xóa hoàn toàn | Đạt |
| 12 | Tăng lượt xem vé | Tìm kiếm trả về kết quả | Lượt xem của các vé hiển thị tăng lên | Đạt |

---

## 9. Xem xổ số — Người dùng đã đăng nhập (8 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Xem trang xổ số người dùng | Đăng nhập → Vào `/userLottery` | Hiển thị danh sách vé đã xuất bản, bộ lọc tìm kiếm | Đạt |
| 2 | Tìm kiếm theo công ty | Chọn công ty | Kết quả lọc theo công ty | Đạt |
| 3 | Tìm kiếm theo số | Nhập num1-num6 | Kết quả lọc theo số | Đạt |
| 4 | Tìm kiếm theo khoảng ngày | startDate, endDate | Kết quả lọc theo ngày | Đạt |
| 5 | Tìm kiếm theo ngày cụ thể | specificDate | Kết quả lọc theo ngày cụ thể | Đạt |
| 6 | Phân trang | page=2 | Phân trang hoạt động đúng | Đạt |
| 7 | Không có kết quả | Bộ lọc không khớp | Hiển thị thông báo không có kết quả | Đạt |
| 8 | Truy cập khi chưa đăng nhập | Mở `/userLottery` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |

---

## 10. Mua vé xổ số (10 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang mua vé tải thành công | Đăng nhập → Vào `/ticketPurchase` | Trang hiển thị danh sách vé và bộ lọc | Đạt |
| 2 | Tìm kiếm vé khả dụng | Chọn công ty "Vietlott" | Kết quả hiển thị vé phù hợp | Đạt |
| 3 | Mua một vé thành công | Vé đã xuất bản, số dư đủ | Thông báo mua thành công | Đạt |
| 4 | Mua nhiều vé đã chọn | Chọn nhiều vé → Nhấn "Buy Selected" | Mua hàng loạt thành công | Đạt |
| 5 | Mua tất cả vé | Nhấn "Buy All" | Tất cả vé được mua | Đạt |
| 6 | Mua vé chưa chọn | Không chọn vé nào → Nhấn "Buy Selected" | Hiển thị cảnh báo yêu cầu chọn vé (dùng `tryClickButtonByText`) | Đạt |
| 7 | Mua vé — số dư không đủ | Số dư < giá vé | Hiển thị lỗi "Số dư không đủ" | Đạt |
| 8 | Mua vé đã bị mua | Vé đã có chủ | Hiển thị lỗi "Vé không khả dụng" | Đạt |
| 9 | Phân trang trang mua vé | page=2 | Phân trang hoạt động đúng | Đạt |
| 10 | Truy cập khi chưa đăng nhập | Mở `/ticketPurchase` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |

---

## 11. Vé của tôi (6 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Xem danh sách vé | Đăng nhập → Vào `/myTickets` | Hiển thị danh sách vé đã mua | Đạt |
| 2 | Tìm kiếm vé đã mua | Nhập từ khóa tìm kiếm | Kết quả lọc đúng | Đạt |
| 3 | Tìm kiếm theo khoảng ngày | startDate, endDate | Kết quả lọc theo ngày | Đạt |
| 4 | Phân trang | page=2 | Phân trang hoạt động đúng | Đạt |
| 5 | Người dùng mới — không có vé | Tài khoản mới chưa mua vé | Hiển thị danh sách trống, không lỗi | Đạt |
| 6 | Truy cập khi chưa đăng nhập | Mở `/myTickets` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |

---

## 12. Nạp tiền (8 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang nạp tiền tải thành công | Đăng nhập → Vào `/topup` | Hiển thị trang nạp tiền với số dư và tùy chọn | Đạt |
| 2 | Nạp tiền với số tiền cố định | Nhấn nút số tiền cố định | Ở lại trang nạp tiền | Đạt |
| 3 | Nạp tiền với số tiền tùy chỉnh | amount="25.00" (dùng `jsClearAndTypeByName`) | Chấp nhận số tiền tùy chỉnh | Đạt |
| 4 | Nạp tiền với phương thức thanh toán | Chọn paymentMethodId từ dropdown | Cho phép chọn phương thức thanh toán | Đạt |
| 5 | Nạp tiền số 0 | amount="0" (dùng `jsClearAndTypeByName`) | Từ chối nạp tiền số 0 | Đạt |
| 6 | Nạp tiền số âm | amount="-10" (dùng `jsClearAndTypeByName`) | Từ chối số tiền âm | Đạt |
| 7 | Nạp tiền không có phương thức | amount="10", không chọn phương thức | Yêu cầu chọn phương thức thanh toán | Đạt |
| 8 | Truy cập khi chưa đăng nhập | Mở `/topup` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |

---

## 13. Quản lý phương thức thanh toán — Người dùng (10 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang thanh toán tải thành công | Đăng nhập → Vào `/payments` | Hiển thị danh sách phương thức thanh toán | Đạt |
| 2 | Thêm phương thức thanh toán mới | Số thẻ, ngày hết hạn, CVV hợp lệ | Thông báo thêm thành công | Đạt |
| 3 | Thêm thẻ không hợp lệ | Số thẻ sai định dạng | Hiển thị lỗi | Đạt |
| 4 | Thêm thẻ hết hạn | Ngày hết hạn đã qua | Hiển thị lỗi "Thẻ hết hạn" | Đạt |
| 5 | Thêm thẻ CVV không hợp lệ | CVV sai | Hiển thị lỗi | Đạt |
| 6 | Thêm thẻ để trống | Tất cả trường trống | Hiển thị lỗi validation | Đạt |
| 7 | Xóa phương thức thanh toán | paymentId hợp lệ | Xóa thành công | Đạt |
| 8 | Thêm thẻ trùng | Số thẻ đã tồn tại | Hiển thị lỗi hoặc chấp nhận | Đạt |
| 9 | Giới hạn số phương thức tối đa | Thêm vượt quá giới hạn | Xử lý đúng (chấp nhận hoặc từ chối) | Đạt |
| 10 | Truy cập khi chưa đăng nhập | Mở `/payments` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |

---

## 14. Quản lý xổ số — Admin (12 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang admin xổ số tải thành công | Đăng nhập admin → Vào `/adminLottery` | Hiển thị danh sách vé, bộ lọc, form tạo vé | Đạt |
| 2 | Tạo vé thành công | ticketID, 6 số, công ty (qua `CompanySelector` API) | Thông báo tạo thành công | Đạt |
| 3 | Tạo vé trùng ID | ticketID đã tồn tại (qua `CompanySelector` API) | Hiển thị lỗi trùng lặp | Đạt |
| 4 | Tạo vé thiếu trường | ticketID trống → JS alert "Nhập ticket ID" | Alert hiện và được đóng, ở lại trang | Đạt |
| 5 | Chèn hàng loạt từ CSV | Dữ liệu CSV hợp lệ | Thông báo thành công với số lượng | Đạt |
| 6 | Chèn hàng loạt — sai định dạng | CSV sai định dạng | Thông báo lỗi | Đạt |
| 7 | Tìm kiếm vé admin | Tham số tìm kiếm | Kết quả lọc đúng | Đạt |
| 8 | Xuất bản vé | ticketId hợp lệ → Publish | Trạng thái vé thay đổi thành Published | Đạt |
| 9 | Hủy xuất bản vé | ticketId hợp lệ → Unpublish | Trạng thái vé thay đổi thành Unpublished | Đạt |
| 10 | Cập nhật giá theo công ty | newPrice="5.00", công ty (qua `CompanySelector` API) | Thông báo cập nhật giá thành công | Đạt |
| 11 | Chọn tất cả và xóa chọn | Nhấn Select All → Clear | Checkbox chọn/bỏ chọn đúng | Đạt |
| 12 | Người dùng thường không truy cập được | Đăng nhập user → Truy cập `/adminLottery` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 15. Quản lý giá vé — Admin (6 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang quản lý giá tải thành công | Đăng nhập admin → Vào `/priceManagement` | Hiển thị danh sách công ty và giá | Đạt |
| 2 | Cập nhật giá thành công | Giá mới hợp lệ | Thông báo "Cập nhật giá thành công" | Đạt |
| 3 | Cập nhật giá = 0 | newPrice="0" | Xử lý đúng (lỗi hoặc chấp nhận) | Đạt |
| 4 | Cập nhật giá âm | newPrice="-5" | Từ chối giá âm | Đạt |
| 5 | Tất cả công ty được liệt kê | Không có bộ lọc | Hiển thị đầy đủ danh sách công ty | Đạt |
| 6 | Người dùng thường không truy cập được | Đăng nhập user → `/priceManagement` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 16. Quản lý người dùng — Admin (14 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang quản lý người dùng tải thành công | Đăng nhập admin → `/userManagement` | Hiển thị danh sách người dùng phân trang | Đạt |
| 2 | Tìm kiếm theo email | search="user@" | Hiển thị người dùng khớp | Đạt |
| 3 | Tìm kiếm theo tên | search="Admin" | Hiển thị người dùng khớp | Đạt |
| 4 | Lọc theo vai trò | role="admin" | Chỉ hiển thị admin | Đạt |
| 5 | Lọc theo ngày đăng nhập cuối | lastLoginFrom, lastLoginTo | Lọc theo khoảng ngày đăng nhập | Đạt |
| 6 | Tạo người dùng mới | Email, tên, mật khẩu hợp lệ | Tạo thành công | Đạt |
| 7 | Tạo người dùng trùng email | Email đã tồn tại | Hiển thị lỗi trùng lặp | Đạt |
| 8 | Cập nhật vai trò | userId, role mới | Cập nhật thành công | Đạt |
| 9 | Vô hiệu hóa người dùng | Chỉ chọn tài khoản dummy (bỏ qua admin/user chính) | Vô hiệu hóa thành công, không ảnh hưởng tài khoản test | Đạt |
| 10 | Vô hiệu hóa hàng loạt | Chọn checkbox dummy users (bỏ qua admin/user chính) | Vô hiệu hóa hàng loạt thành công | Đạt |
| 11 | Chỉnh sửa người dùng | Sửa firstName → "E2E_Edited" (chờ modal, dùng `tryClickButtonByText`) | Cập nhật thông tin thành công | Đạt |
| 12 | Phân trang | page=2 | Phân trang hoạt động đúng | Đạt |
| 13 | Xuất người dùng ra Excel/CSV | Nhấn nút xuất | Tải xuống tệp xuất dữ liệu | Đạt |
| 14 | Người dùng thường không truy cập được | Đăng nhập user → `/userManagement` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 17. Quản lý giao dịch — Admin (7 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang giao dịch tải thành công | Đăng nhập admin → `/adminTransactions` | Hiển thị danh sách giao dịch | Đạt |
| 2 | Tìm kiếm theo email | email="user@" | Lọc giao dịch theo email | Đạt |
| 3 | Tìm kiếm theo khoảng ngày | startDate, endDate | Lọc giao dịch theo ngày | Đạt |
| 4 | Đặt lại form tìm kiếm | Nhấn "Reset" | Bộ lọc được xóa | Đạt |
| 5 | Phân trang | page=2 | Phân trang hoạt động đúng | Đạt |
| 6 | Thông báo không có giao dịch | Bộ lọc không khớp | Hiển thị thông báo trống | Đạt |
| 7 | Người dùng thường không truy cập được | Đăng nhập user → `/adminTransactions` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 18. Hồ sơ Admin (7 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang hồ sơ admin tải thành công | Đăng nhập admin → `/adminProfile` | Hiển thị đầy đủ thông tin admin | Đạt |
| 2 | Cập nhật tên admin | firstName mới | Tên được cập nhật thành công | Đạt |
| 3 | Yêu cầu thay đổi email | newEmail hợp lệ | Thông báo "Mã xác minh đã được gửi" | Đạt |
| 4 | Đổi mật khẩu admin thành công | Mật khẩu đúng (scroll đến form mật khẩu) → đổi lại về gốc | Đổi thành công, mật khẩu được khôi phục | Đạt |
| 5 | Đổi mật khẩu admin — sai mật khẩu hiện tại | Mật khẩu hiện tại sai (scroll đến form) | Hiển thị lỗi "Mật khẩu không đúng" | Đạt |
| 6 | Đổi mật khẩu admin — mật khẩu yếu | newPassword="weak" (scroll đến form) | Hiển thị lỗi yêu cầu mật khẩu mạnh | Đạt |
| 7 | Người dùng thường không truy cập được | Đăng nhập user → `/adminProfile` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 19. Quản lý phương thức thanh toán — Admin (8 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Trang quản lý thanh toán tải thành công | Đăng nhập admin → `/paymentMethodManagement` | Hiển thị danh sách phương thức thanh toán | Đạt |
| 2 | Thêm phương thức thanh toán | Chi tiết thẻ hợp lệ | Thêm thành công | Đạt |
| 3 | Thêm thẻ số không hợp lệ | Số thẻ sai | Hiển thị lỗi | Đạt |
| 4 | Thêm thẻ hết hạn | Ngày hết hạn đã qua | Hiển thị lỗi | Đạt |
| 5 | Xóa phương thức thanh toán | paymentId hợp lệ | Xóa thành công | Đạt |
| 6 | Trường trống validation | Tất cả trường trống (dùng `tryClickButtonByText`) | Ở lại trang, không thêm | Đạt |
| 7 | Thêm thẻ trùng | Số thẻ đã tồn tại | Xử lý đúng | Đạt |
| 8 | Người dùng thường không truy cập được | Đăng nhập user → `/paymentMethodManagement` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 20. Xuất danh sách người dùng — Admin (5 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Nút xuất tồn tại | Vào trang quản lý người dùng | Nút "Export" hiển thị trên trang | Đạt |
| 2 | Xuất tất cả người dùng | Nhấn "Export" | Tải xuống tệp CSV, Content-Type đúng | Đạt |
| 3 | Xuất người dùng đã lọc | Lọc theo vai trò → Xuất | CSV chỉ chứa người dùng khớp bộ lọc | Đạt |
| 4 | Xuất người dùng đã tìm kiếm | Tìm kiếm → Xuất | CSV chứa kết quả tìm kiếm | Đạt |
| 5 | Người dùng thường không xuất được | Đăng nhập user → `/exportUsers` | Chuyển hướng đến welcome.jsp | Đạt |

---

## 21. Phân quyền và kiểm soát truy cập (8 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | User không truy cập được `/adminLottery` | Đăng nhập user → `/adminLottery` | Chuyển hướng đến welcome.jsp | Đạt |
| 2 | User không truy cập được `/userManagement` | Đăng nhập user → `/userManagement` | Chuyển hướng đến welcome.jsp | Đạt |
| 3 | User không truy cập được `/adminTransactions` | Đăng nhập user → `/adminTransactions` | Chuyển hướng đến welcome.jsp | Đạt |
| 4 | User không truy cập được `/priceManagement` | Đăng nhập user → `/priceManagement` | Chuyển hướng đến welcome.jsp | Đạt |
| 5 | Không đăng nhập → `/profile` | Mở `/profile` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |
| 6 | Không đăng nhập → `/ticketPurchase` | Mở `/ticketPurchase` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |
| 7 | Không đăng nhập → `/topup` | Mở `/topup` không đăng nhập | Chuyển hướng đến trang đăng nhập | Đạt |
| 8 | Khách truy cập trang chủ | Mở `/homepage` không đăng nhập | Trang hiển thị bình thường | Đạt |

---

## 22. Điều hướng và giao diện (10 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Link điều hướng trên trang chủ | Mở trang chủ | Có link Login và Register | Đạt |
| 2 | Sidebar người dùng | Đăng nhập user → `/userLottery` | Sidebar hiển thị | Đạt |
| 3 | Sidebar admin | Đăng nhập admin → `/adminLottery` | Sidebar hiển thị | Đạt |
| 4 | Link trong sidebar người dùng | Đăng nhập user | Sidebar chứa link: userLottery, ticketPurchase, myTickets, profile | Đạt |
| 5 | Link trong sidebar admin | Đăng nhập admin | Sidebar chứa link: adminLottery, userManagement, adminTransactions | Đạt |
| 6 | Tiêu đề trang chính xác | Mở trang chủ và trang đăng nhập | Tiêu đề chứa "Lottery" hoặc "Fortuna" | Đạt |
| 7 | Giao diện đáp ứng — điện thoại | Viewport 375×812 (iPhone X) | Trang hiển thị đúng | Đạt |
| 8 | Giao diện đáp ứng — máy tính bảng | Viewport 768×1024 (iPad) | Trang hiển thị đúng | Đạt |
| 9 | Link đăng xuất hiển thị | Đăng nhập user → Kiểm tra page source | Có link `/logout` hoặc text "Logout" (tìm kiếm không phân biệt hoa thường) | Đạt |
| 10 | Xử lý trang lỗi | Truy cập URL không tồn tại | Xử lý đúng, không crash | Đạt |

---

## 23. Phân trang (8 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | Phân trang trang chủ | Mở `/homepage` | Phân trang hoạt động | Đạt |
| 2 | Phân trang trang chủ — trang 2 | page=2 | Trang 2 hiển thị đúng | Đạt |
| 3 | Phân trang xổ số người dùng | Đăng nhập → `/userLottery` | Phân trang hoạt động | Đạt |
| 4 | Phân trang mua vé | Đăng nhập → `/ticketPurchase` | Phân trang hoạt động | Đạt |
| 5 | Phân trang vé của tôi | Đăng nhập → `/myTickets` | Phân trang hoạt động | Đạt |
| 6 | Phân trang xổ số admin | Đăng nhập admin → `/adminLottery` | Phân trang hoạt động | Đạt |
| 7 | Phân trang quản lý người dùng | Đăng nhập admin → `/userManagement` | Phân trang hoạt động | Đạt |
| 8 | Phân trang giao dịch admin | Đăng nhập admin → `/adminTransactions` | Phân trang hoạt động | Đạt |

---

## 24. Bảo mật và SQL Injection (11 tests)

| # | Kịch bản | Dữ liệu đầu vào | Kết quả mong đợi | Kết quả |
|---|----------|------------------|-------------------|---------| 
| 1 | SQL injection qua đăng nhập | email="' OR 1=1 --" (dùng `tryLoginAs`) | Ở lại trang đăng nhập, hệ thống an toàn | Đạt |
| 2 | SQL injection qua tìm kiếm | Nhập chuỗi SQL vào ô tìm kiếm | Không hiển thị dữ liệu nhạy cảm | Đạt |
| 3 | SQL injection qua đăng ký | Chuỗi SQL trong email và mật khẩu | Từ chối hoặc làm sạch đầu vào | Đạt |
| 4 | XSS qua tìm kiếm | `<script>alert('xss')</script>` trong ô tìm kiếm | Mã script không thực thi | Đạt |
| 5 | XSS qua đăng ký | `<script>` trong họ tên | Mã script bị loại bỏ hoặc escape | Đạt |
| 6 | Session fixation | Đăng nhập → Kiểm tra session ID khác trước khi đăng nhập | Session ID thay đổi sau đăng nhập | Đạt |
| 7 | Truy cập trực tiếp JSP | Mở trực tiếp file JSP qua URL | Không truy cập được file JSP nội bộ | Đạt |
| 8 | HTTP method tampering | Gửi DELETE request đến trang | Xử lý đúng (lỗi hoặc bỏ qua) | Đạt |
| 9 | Path traversal | Đường dẫn chứa `../../../../etc/passwd` | Không trả về file hệ thống | Đạt |
| 10 | Bảo mật phiên đồng thời | Đăng nhập → Đăng xuất → Kiểm tra phiên | Phiên được xóa hoàn toàn | Đạt |
| 11 | Mật khẩu không hiển thị trong URL | Đăng nhập xong → Kiểm tra URL | URL không chứa mật khẩu | Đạt |

---

**Tổng cộng: 24 mục kiểm thử · 187 trường hợp kiểm thử · Tỷ lệ đạt: 100%**
