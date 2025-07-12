# 💸 EzPay – Mini E-Wallet System

EzPay là một hệ thống ví điện tử đơn giản được xây dựng bằng **Spring Boot** & **PostgreSQL**. Dự án mô phỏng chức năng
cơ bản của một ứng dụng chuyển tiền như ZaloPay, MoMo, phù hợp làm **demo apply vị trí Java Backend Fresher**.

---

## 🚀 Tính năng

- ✅ **Authentication**: Đăng ký, đăng nhập bằng JWT
- ✅ **Security**: Mã hoá mật khẩu bằng BCrypt
- ✅ **Transfer**: Chuyển tiền giữa người dùng
- ✅ **History**: Lịch sử giao dịch
- ✅ **Profile**: Thông tin tài khoản
- ✅ **API Docs**: Swagger UI hỗ trợ test API

---

## 🧠 Tech Stack

| Thành phần | Công nghệ                   |
|------------|-----------------------------|
| Backend    | Spring Boot 3.5.3 (Java 17) |
| Security   | Spring Security + JWT       |
| Database   | PostgreSQL 15               |
| ORM        | Spring Data JPA             |
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

### 3. Chạy ứng dụng

```bash
# Chạy với Maven
./mvnw spring-boot:run

# Hoặc chạy với Docker
docker-compose up -d
```

**Ứng dụng chạy tại:** `http://localhost:8080`

---

## 📋 API Endpoints

### Authentication

- `POST /v1/api/auth/register` - Đăng ký
- `POST /v1/api/auth/login` - Đăng nhập

### User Profile

- `GET /v1/api/users/me` - Thông tin tài khoản

### Transactions

- `POST /v1/api/transactions/transfer` - Chuyển tiền
- `GET /v1/api/transactions/history` - Lịch sử giao dịch

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

