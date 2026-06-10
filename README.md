# MiniSync 🔗

**MiniSync** là một plugin Minecraft (Spigot/Bukkit) mạnh mẽ giúp liên kết tài khoản Minecraft của người chơi với tài khoản Discord của họ một cách đơn giản và an toàn.

## ✨ Tính năng

- **Liên kết dễ dàng**: Sử dụng mã (code) để xác thực giữa Discord và Minecraft.
- **Slash Commands**: Sử dụng `/sync` trực tiếp trên Discord (JDA 5).
- **Cơ sở dữ liệu MySQL**: Lưu trữ dữ liệu liên kết một cách bền vững.
- **Tự động tạo bảng**: Không cần phải nhập thủ công file SQL.
- **Tùy chỉnh linh hoạt**: Chỉnh sửa mọi thông tin qua `config.yml`.
- **Hỗ trợ Maven**: Dễ dàng biên dịch và đóng gói với đầy đủ thư viện.

## 🛠️ Cài đặt

1. Tải plugin về và bỏ vào thư mục `plugins` của server.
2. Khởi động server để tạo tệp cấu hình.
3. Mở `plugins/MiniSync/config.yml`:
   - Điền thông tin kết nối **MySQL**.
   - Điền **Discord Bot Token**.
4. Khởi động lại server hoặc tải lại plugin.

## 🎮 Cách sử dụng

1. **Trên Discord**: Gõ lệnh `/sync`. Bot sẽ gửi cho bạn một mã (ví dụ: `A1B2C3`).
2. **Trong Minecraft**: Gõ lệnh `/sync A1B2C3`.
3. Hệ thống sẽ xác nhận liên kết thành công!
4. Để hủy liên kết, dùng lệnh `/unsync` trong game.

## 🏗️ Biên dịch (Build)

Dự án sử dụng Maven. Để biên dịch:

```bash
mvn clean package
```

File `.jar` sẽ nằm trong thư mục `target/`.

## 📄 Giấy phép

Dự án này được phát hành dưới bản quyền MIT.