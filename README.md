# MiniSync 2.0 🔗

Plugin liên kết tài khoản Minecraft với Discord dành cho Paper 1.20.6 và NIGHTMARE TCYT.

## Tính năng
- Discord slash command `/sync` tạo mã liên kết riêng tư.
- Minecraft `/sync <code>` xác minh và liên kết UUID với Discord ID.
- Minecraft `/unsync` hủy liên kết.
- Mã xác minh hết hạn tự động (mặc định 10 phút), chỉ dùng một lần.
- Mỗi Discord ID chỉ được liên kết với một UUID.
- MySQL connection pool bằng HikariCP.
- Tác vụ database và khởi động Discord chạy ngoài luồng chính Minecraft.
- Tự tạo bảng và nâng cấp schema từ MiniSync 1.x mà không xóa dữ liệu liên kết.

## Yêu cầu
- Paper 1.20.6
- Java 21
- MySQL 8.x (hoặc dịch vụ tương thích MySQL)
- Discord bot token và quyền sử dụng application/slash commands

## Cài đặt
1. Tải `MiniSync-2.0.0.jar` từ GitHub Releases.
2. Đặt JAR vào thư mục `plugins` của server.
3. Khởi động server một lần để tạo `plugins/MiniSync/config.yml`.
4. Điền thông tin MySQL và Discord bot token.
5. Khởi động lại server. Kiểm tra log có thông báo MySQL và Discord kết nối thành công.

**Không gửi Discord token, mật khẩu database hoặc file config có thông tin bí mật cho người khác.**

## Sử dụng
1. Trên Discord, chạy slash command `/sync`.
2. Bot trả mã riêng tư; nhập mã đó trong Minecraft bằng `/sync <code>`.
3. Để hủy liên kết, chạy `/unsync` trong Minecraft.

## Cấu hình
- `sync.code-expiry-minutes`: thời hạn mã xác minh, mặc định 10 phút.
- `mysql.*`: thông tin kết nối MySQL.
- `discord.token`: token bot Discord.

## Build
Cần JDK 21 và Maven:

```bash
mvn clean package
```

JAR đã đóng gói thư viện nằm tại `target/MiniSync-2.0.0.jar`.

## Lưu ý database
MiniSync 2.0 giữ lại bảng liên kết `minisync`. Bảng mã `minisync_codes` được bổ sung cột hết hạn; các mã cũ không có thời hạn sẽ bị xóa khi nâng cấp. Hãy sao lưu database trước khi cập nhật plugin.

## License
MIT
