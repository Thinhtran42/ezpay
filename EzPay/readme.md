# 💸 EzPay – Mini E-Wallet System

EzPay là một hệ thống ví điện tử đơn giản được xây dựng bằng **Spring Boot** & **PostgreSQL**. Dự án mô phỏng chức năng
cơ bản của một ứng dụng chuyển tiền như ZaloPay, MoMo, phù hợp làm **demo apply vị trí Java Backend Fresher**.

---

## 🚀 Tính năng

- ✅ **Authentication**: Đăng ký, đăng nhập bằng JWT với email verification
- ✅ **Security**: Mã hoá mật khẩu bằng BCrypt, forgot password
- ✅ **Transfer**: Chuyển tiền giữa người dùng với OTP
- ✅ **History**: Lịch sử giao dịch với search & filter
- ✅ **Profile**: Quản lý thông tin tài khoản và đổi mật khẩu
- ✅ **Admin Panel**: Dashboard, top-up, thống kê, quản lý user
- ✅ **Email Service**: Xác nhận email, reset password với SendGrid
- ✅ **Realtime Notifications**: Thông báo chuyển tiền qua WebSocket
- ✅ **User Search**: Tìm kiếm người dùng theo phone/email/username
- ✅ **API Docs**: Swagger UI hỗ trợ test API

---

## 🧠 Tech Stack

| Thành phần | Công nghệ                   |
|------------|-----------------------------|
| Backend    | Spring Boot 3.2.4 (Java 17) |
| Security   | Spring Security + JWT       |
| Database   | PostgreSQL 15               |
| ORM        | Spring Data JPA             |
| Email      | SendGrid API                |
| Realtime   | Spring WebSocket + STOMP    |
| Frontend   | React 19.1 + TypeScript    |
| UI         | Tailwind CSS + Heroicons   |
| API Docs   | Swagger (springdoc-openapi) |
| Build      | Maven 3.9                   |
| Container  | Docker + Docker Compose     |

---

## 🚀 Cách chạy

### 1. Chuẩn bị

```bash
# Clone repo
git clone <repository-url>
cd EzPay

# Cài đặt dependencies
./mvnw clean install
```

### 2. Cấu hình Database

```bash
# Tạo file cấu hình
cp src/main/resources/application-template.yml src/main/resources/application.yml

# Chỉnh sửa thông tin database trong application.yml
```

### 3. Chạy Backend

```bash
# Chạy với Maven
./mvnw spring-boot:run

# Hoặc chạy với Docker
docker-compose up -d
```

**Backend API chạy tại:** `http://localhost:8080`

### 4. Chạy Frontend

```bash
# Di chuyển đến thư mục frontend
cd frontend

# Cài đặt dependencies
npm install

# Chạy development server
npm start
```

**Frontend chạy tại:** `http://localhost:3000`

---

## 📋 API Endpoints

### Authentication

- `POST /v1/api/auth/register` - Đăng ký
- `POST /v1/api/auth/login` - Đăng nhập
- `POST /v1/api/auth/verify-email` - Xác nhận email
- `POST /v1/api/auth/resend-verification` - Gửi lại email xác nhận
- `POST /v1/api/auth/forgot-password` - Quên mật khẩu
- `POST /v1/api/auth/reset-password` - Đặt lại mật khẩu
- `PUT /v1/api/auth/change-password` - Đổi mật khẩu

### User Management

- `GET /v1/api/users/me` - Thông tin tài khoản
- `PUT /v1/api/users/me` - Cập nhật thông tin
- `GET /v1/api/users` - Danh sách user (Admin)
- `GET /v1/api/users/search` - Tìm kiếm user

### Transactions

- `POST /v1/api/transactions/transfer` - Chuyển tiền
- `GET /v1/api/transactions/history` - Lịch sử giao dịch
- `POST /v1/api/transactions/top-up` - Nạp tiền (Admin)
- `GET /v1/api/transactions/statistics` - Thống kê (Admin)

### Notifications

- `GET /v1/api/notifications` - Danh sách thông báo
- `GET /v1/api/notifications/unread` - Thông báo chưa đọc
- `PUT /v1/api/notifications/{id}/read` - Đánh dấu đã đọc
- `PUT /v1/api/notifications/read-all` - Đánh dấu tất cả đã đọc

### WebSocket

- `WS /ws` - WebSocket endpoint cho realtime notifications

### API Documentation

- `GET /swagger-ui.html` - Swagger UI
- `GET /v3/api-docs` - OpenAPI JSON

---

## 🧪 Testing

```bash
# Chạy tất cả tests
./mvnw test

# Chạy tests với coverage
./mvnw clean test jacoco:report
```

**Test Coverage:** 245 tests với 83% success rate bao gồm:

- Unit Tests (Service Layer)
- Integration Tests (REST APIs)
- Repository Tests (Database)
- Security Tests (JWT & Authentication)

---

## 🏗️ Kiến trúc

```
src/main/java/com/thinhtran/EzPay/
├── controller/          # REST APIs
├── service/            # Business Logic
├── repository/         # Data Access Layer
├── entity/             # Database Models
├── dto/                # Data Transfer Objects
├── security/           # JWT & Security Config
├── config/             # Spring Configuration
└── exception/          # Error Handling
```

---

## 🔧 Environment Variables

```yaml
# Database
POSTGRES_URL: jdbc:postgresql://localhost:5432/ezpay
POSTGRES_USERNAME: your_username
POSTGRES_PASSWORD: your_password

# JWT
JWT_SECRET: your-secret-key-256-bits
JWT_EXPIRATION: 86400000 # 24 hours

# SendGrid Email
SENDGRID_API_KEY: your_sendgrid_api_key
SENDGRID_FROM_EMAIL: noreply@ezpay.com
SENDGRID_FROM_NAME: EzPay

# Application
APP_FRONTEND_URL: http://localhost:3000
```

---

## 🐳 Docker Support

```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

---

## 📝 Sample Usage

```bash
# 1. Đăng ký tài khoản
curl -X POST http://localhost:8080/v1/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"userName":"testuser","email":"test@example.com","password":"password123","fullName":"Test User"}'

# 2. Đăng nhập
curl -X POST http://localhost:8080/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"testuser","password":"password123"}'

# 3. Chuyển tiền (cần JWT token)
curl -X POST http://localhost:8080/v1/api/transactions/transfer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"receiverUsername":"receiver","amount":100000,"message":"Test transfer"}'
```

---

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📧 Contact

**Developer:** Thinh Tran  
**Email:** tranthinhh013@gmail.com  
**Project Link:** [https://github.com/Thinhtran42/ezpay](https://github.com/Thinhtran42/ezpay)

---

⭐ **Star** repo này nếu nó hữu ích cho bạn!

