# 💰 EzPay - Hệ thống chuyển tiền điện tử

**EzPay** là một ứng dụng web full-stack cho phép người dùng chuyển tiền nhanh chóng và an toàn với giao diện hiện đại và thông báo real-time.

## 🚀 Tính năng chính

### 👥 Quản lý người dùng
- ✅ **Đăng ký/Đăng nhập** với xác thực JWT
- ✅ **Xác nhận email** thông qua SendGrid
- ✅ **Quên mật khẩu** và đặt lại qua email
- ✅ **Quản lý profile** cá nhân
- ✅ **Phân quyền** USER/ADMIN

### 💸 Giao dịch
- ✅ **Chuyển tiền** với xác thực OTP
- ✅ **Lịch sử giao dịch** chi tiết
- ✅ **Tìm kiếm người nhận** thông minh
- ✅ **Validation** số dư và input
- ✅ **Thông báo real-time** qua WebSocket

### 🔔 Thông báo
- ✅ **Real-time notifications** với WebSocket
- ✅ **Thông báo email** cho các sự kiện quan trọng
- ✅ **Notification bell** với unread count
- ✅ **Mark as read** functionality

### 👨‍💼 Admin Dashboard
- ✅ **Thống kê tổng quan** hệ thống
- ✅ **Nạp tiền** cho người dùng
- ✅ **Quản lý người dùng**
- ✅ **Top receivers** analytics
- ✅ **System metrics** và reports

## 🛠️ Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.4** - Framework
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database ORM
- **JWT** - Token-based authentication
- **PostgreSQL** - Database
- **SendGrid** - Email service
- **WebSocket** - Real-time communication
- **Maven** - Build tool

### Frontend
- **React 18** - UI Framework
- **TypeScript** - Type safety
- **Tailwind CSS** - Styling
- **React Router** - Navigation
- **Axios** - HTTP client
- **React Hot Toast** - Notifications
- **Heroicons** - Icons

### DevOps & Tools
- **Docker** - Containerization
- **GitHub Actions** - CI/CD (planned)
- **Swagger/OpenAPI** - API documentation

## 📁 Cấu trúc dự án

```
EzPay/
├── EzPay/                          # Backend Spring Boot
│   ├── src/main/java/com/thinhtran/EzPay/
│   │   ├── config/                 # Configuration classes
│   │   ├── controller/             # REST Controllers
│   │   ├── dto/                    # Data Transfer Objects
│   │   ├── entity/                 # JPA Entities
│   │   ├── exception/              # Custom Exceptions
│   │   ├── repository/             # Data Repositories
│   │   ├── security/               # Security Configuration
│   │   └── service/                # Business Logic
│   ├── src/main/resources/
│   │   ├── application.yml         # Main config (public)
│   │   ├── application-dev.yml     # Development config (ignored)
│   │   └── application-prod.yml    # Production config (ignored)
│   └── src/test/                   # Unit & Integration Tests
├── frontend/                       # Frontend React App
│   ├── src/
│   │   ├── components/             # React Components
│   │   ├── contexts/               # React Contexts
│   │   ├── services/               # API Services
│   │   └── types/                  # TypeScript Types
│   └── public/                     # Static assets
├── docker-compose.yml              # Docker setup
└── README.md                       # This file
```

## 🔧 Cài đặt và chạy

### Prerequisites
- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 14+**
- **Maven 3.8+**
- **SendGrid API Key**

### 1. Clone repository
```bash
git clone https://github.com/Thinhtran42/ezpay.git
cd ezpay
```

### 2. Setup Database
```bash
# Create PostgreSQL database
createdb ezpay_db

# Or using Docker
docker run --name postgres-ezpay -e POSTGRES_DB=ezpay_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -p 5432:5432 -d postgres:14
```

### 3. Backend Setup

#### Option A: Manual Setup
```bash
cd EzPay

# Copy and configure development settings
cp src/main/resources/application-template.yml src/main/resources/application-dev.yml

# Edit application-dev.yml with your actual values:
# - Database connection
# - SendGrid API key
# - JWT secret

# Run application
mvn spring-boot:run
```

#### Option B: Docker Setup
```bash
# Edit docker-compose.yml with your SendGrid API key
docker-compose up -d
```

### 4. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

### 5. Access Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## ⚙️ Configuration

### Environment Variables

Create `application-dev.yml` với nội dung:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ezpay_db
    username: your_db_username
    password: your_db_password

# SendGrid Configuration
sendgrid:
  api-key: YOUR_SENDGRID_API_KEY
  from-email: your-email@domain.com
  from-name: EzPay

# JWT Configuration
jwt:
  secret: your-256-bit-secret-key
  expirationMs: 86400000 # 24 hours

# Application URL
app:
  frontend-url: http://localhost:3000
```

### SendGrid Setup
1. Tạo tài khoản [SendGrid](https://sendgrid.com/)
2. Tạo API Key trong Settings > API Keys
3. Verify Sender trong Settings > Sender Authentication
4. Cập nhật API key vào `application-dev.yml`

## 📚 API Documentation

### Authentication Endpoints
```
POST /v1/api/auth/register     # Đăng ký
POST /v1/api/auth/login        # Đăng nhập
POST /v1/api/auth/verify-email # Xác nhận email
POST /v1/api/auth/forgot-password # Quên mật khẩu
POST /v1/api/auth/reset-password  # Đặt lại mật khẩu
```

### User Endpoints
```
GET  /v1/api/users/me          # Thông tin người dùng
PUT  /v1/api/users/me          # Cập nhật profile
GET  /v1/api/users/search      # Tìm kiếm người dùng
```

### Transaction Endpoints
```
POST /v1/api/transactions      # Chuyển tiền
GET  /v1/api/transactions      # Lịch sử giao dịch
POST /v1/api/transactions/top-up # Nạp tiền (Admin)
GET  /v1/api/transactions/statistics # Thống kê (Admin)
```

### Notification Endpoints
```
GET  /v1/api/notifications     # Danh sách thông báo
GET  /v1/api/notifications/unread # Thông báo chưa đọc
PUT  /v1/api/notifications/{id}/read # Đánh dấu đã đọc
```

Xem chi tiết tại: http://localhost:8080/swagger-ui.html

## 🧪 Testing

### Backend Tests
```bash
cd EzPay
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 🔒 Security Features

- **JWT Authentication** với expiration handling
- **Password encryption** với BCrypt
- **Email verification** bắt buộc
- **OTP verification** cho giao dịch
- **Input validation** và sanitization
- **CORS configuration** 
- **SQL injection prevention** với JPA
- **XSS protection** với CSP headers

## 🚀 Deployment

### Production Environment Variables
```bash
# Database
DATABASE_URL=postgresql://user:pass@host:5432/ezpay_prod
DATABASE_USERNAME=prod_user
DATABASE_PASSWORD=prod_password

# SendGrid
SENDGRID_API_KEY=your_production_api_key
SENDGRID_FROM_EMAIL=noreply@yourdomain.com

# JWT
JWT_SECRET=your_production_256_bit_secret
JWT_EXPIRATION_MS=86400000

# Application
APP_FRONTEND_URL=https://yourdomain.com
```

### Docker Deployment
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

## 📝 Sample Data

### Default Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@ezpay.com`

### Test Users
- **Username**: `user1` / **Password**: `user123`
- **Username**: `user2` / **Password**: `user123`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Authors

- **Thinh Tran** - *Initial work* - [@Thinhtran42](https://github.com/Thinhtran42)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the powerful UI library
- SendGrid for reliable email service
- All open source contributors

## 📞 Support

If you have any questions or need help:

- 📧 Email: thinhtran.dev@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/Thinhtran42/ezpay/issues)
- 📖 Wiki: [Project Wiki](https://github.com/Thinhtran42/ezpay/wiki)

---

⭐ **Star this repository if you find it helpful!**
