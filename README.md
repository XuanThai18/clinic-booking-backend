# 🏥 Clinic Booking System - Backend

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Secure-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

Đây là Backend Server cho hệ thống **Đặt Lịch Khám Bệnh (Clinic Booking)**. Hệ thống cung cấp API RESTful để quản lý người dùng, bác sĩ, lịch trình và quy trình đặt hẹn khám bệnh trực tuyến.

---

## 🚀 Tính Năng Chính

### 1. Xác thực & Phân quyền (Authentication & Authorization)
* Đăng ký / Đăng nhập (JWT Authentication).
* Quên mật khẩu (Gửi Token qua Email).
* Phân quyền Role-based:
    * **PATIENT:** Đặt lịch, xem lịch sử, cập nhật hồ sơ cá nhân.
    * **DOCTOR:** Quản lý lịch rảnh, xem danh sách hẹn, cập nhật kết quả khám (bệnh án), xem lịch sử khám.
    * **ADMIN:** Quản lý User, Bác sĩ, Phòng khám, Chuyên khoa.

### 2. Quản lý Bác sĩ (Doctor Management)
* Quản lý thông tin chuyên môn (Học vị, Giá khám, Mô tả).
* Đăng ký lịch làm việc (Schedule) theo khung giờ.
* Tự cập nhật hồ sơ và ảnh chứng chỉ.

### 3. Quy trình Đặt hẹn (Appointment Workflow)
* **Bệnh nhân:** Tìm kiếm bác sĩ, chọn ngày/giờ, đặt lịch.
* **Quy trình:** PENDING (Chờ duyệt) ➝ CONFIRMED (Đã duyệt) ➝ COMPLETED (Đã khám) hoặc CANCELLED (Hủy).
* Xử lý logic: Ngăn chặn đặt trùng giờ, chặn đặt lịch quá khứ.

### 4. Tiện ích khác
* Upload hình ảnh (Avatar, Chứng chỉ).
* Gửi Email tự động (Reset password).
* Thống kê lịch sử khám bệnh.

---

## 🛠️ Công Nghệ Sử Dụng

* **Ngôn ngữ:** Java 17
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL (hoặc MySQL)
* **ORM:** Spring Data JPA / Hibernate
* **Security:** Spring Security, JWT (Json Web Token)
* **Build Tool:** Maven
* **Mail:** Java Mail Sender (Gmail SMTP)

---

## ⚙️ Cài Đặt & Chạy Dự Án

### 1. Yêu cầu hệ thống (Prerequisites)
* Java Development Kit (JDK) 17 trở lên.
* Maven.
* PostgreSQL (hoặc MySQL) đã cài đặt và đang chạy.

### 2. Clone dự án
```bash
git clone [https://github.com/username/clinic-booking-backend.git](https://github.com/username/clinic-booking-backend.git)
cd clinic-booking-backend

3. Cấu hình Database & Biến môi trường
spring.application.name=Clinic Booking Backend

# =======================================
# === PostgreSQL Database Configuration ==
# =======================================
spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/<DATABASE_NAME>
spring.datasource.username=<DB_USERNAME>
spring.datasource.password=<DB_PASSWORD>

# =======================================
# === JPA & Hibernate Configuration ======
# =======================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# =======================================
# === JWT Secret Key =====================
# =======================================
# Must be a long, secure, random string. Example 64+ chars:
application.security.jwt.secret-key=<YOUR_SECURE_JWT_SECRET>

# =======================================
# === Database Encryption Key ============
# =======================================
# For AES: key length must be 16, 24, or 32 chars
application.security.db.encryption-key=<YOUR_32_CHAR_AES_KEY>

# =======================================
# === Frontend URL =======================
# =======================================
app.frontend.url=http://localhost:5173

# =======================================
# === Google reCAPTCHA v2 ================
# =======================================
recaptcha.secret=<RECAPTCHA_SECRET_KEY>
recaptcha.verify-url=https://www.google.com/recaptcha/api/siteverify

# =======================================
# === Cloudinary Configuration ===========
# =======================================
cloudinary.cloud_name=<CLOUDINARY_CLOUD_NAME>
cloudinary.api_key=<CLOUDINARY_API_KEY>
cloudinary.api_secret=<CLOUDINARY_API_SECRET>

# =======================================
# === Email (SMTP) Configuration =========
# =======================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<EMAIL_USERNAME>
spring.mail.password=<EMAIL_APP_PASSWORD>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

4. Chạy ứng dụng
mvn spring-boot:run

📚 API Endpoints Chính
Auth:
<img width="620" height="234" alt="image" src="https://github.com/user-attachments/assets/06e6bda0-8b89-4d24-b7fb-b421bbf61279" />
Users & Doctors:
<img width="700" height="228" alt="image" src="https://github.com/user-attachments/assets/a74e2663-10a8-44ba-9507-06b136926d74" />
Appointments (Lịch hẹn):
<img width="839" height="278" alt="image" src="https://github.com/user-attachments/assets/0a32b8a1-4432-4f9c-979a-2c9f746d97de" />

📂 Cấu trúc thư mục
src/main/java/vn/xuanthai/clinic
├── config          # Cấu hình (Security, CORS, Swagger)
├── controller      # API Controllers
├── dto             # Data Transfer Objects (Request/Response)
├── entity          # Database Entities (JPA)
├── exception       # Xử lý lỗi tập trung (Global Exception Handler)
├── repository      # Data Access Layer
├── service         # Business Logic Layer
└── utils           # Các hàm tiện ích (JwtUtils...)


