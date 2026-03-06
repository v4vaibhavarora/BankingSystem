# SecureBank — Complete Setup Guide (XAMPP + Mac)
## Java Spring Boot + MySQL Banking System

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| XAMPP | Latest | https://apachefriends.org |

---

## Step 1 — Start XAMPP MySQL

1. Open XAMPP Control Panel
2. Click **Start** next to **MySQL**
3. Click **Start** next to **Apache** (optional, for phpMyAdmin)
4. Open phpMyAdmin: http://localhost/phpmyadmin

---

## Step 2 — Create Database

### Option A: phpMyAdmin (Easy)
1. Open http://localhost/phpmyadmin
2. Click **SQL** tab
3. Paste the contents of `src/main/resources/schema.sql`
4. Click **Go**

### Option B: MySQL CLI
```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u root -p
```
```sql
CREATE DATABASE banking_db;
EXIT;
```

---

## Step 3 — Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database (XAMPP default - no password)
spring.datasource.url=jdbc:mysql://localhost:3306/banking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=        # Leave empty if XAMPP default

# Email (optional - for KYC approval emails)
spring.mail.username=YOUR_GMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD   # Gmail App Password (not regular password)
bank.mail.from=YOUR_GMAIL@gmail.com
```

### Gmail App Password Setup (for emails):
1. Go to Google Account → Security
2. Enable 2-Step Verification
3. Search "App Passwords" → Generate for "Mail"
4. Use that 16-char password above

> **Note:** If email is not configured, credentials will be printed to console instead.

---

## Step 4 — Build & Run

```bash
# Navigate to project folder
cd banking-system

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

Or run the JAR directly:
```bash
mvn clean package -DskipTests
java -jar target/banking-system-1.0.0.jar
```

---

## Step 5 — Access the Application

| Panel | URL |
|-------|-----|
| 🏠 Home | http://localhost:8080 |
| 🧑‍💼 Customer Login | http://localhost:8080/customer/login |
| 📋 KYC Registration | http://localhost:8080/customer/register |
| 👨‍💼 Manager Login | http://localhost:8080/manager/login |

---

## Login Credentials

### Manager Panel
| Field | Value |
|-------|-------|
| Username | `manager` |
| Password | `Manager@123` |

### Customer Panel
- Customers log in with **Account Number + Password**
- Account number and temporary password are generated when manager approves KYC
- Check email (or console logs if email not configured)

---

## Application Flow

```
Customer                    Manager
    │                          │
    ├── Register (KYC Form)    │
    │   (Status: PENDING)      │
    │                          ├── View Pending KYC
    │                          ├── Approve KYC
    │                          │   → Generates 6-digit Account No.
    │                          │   → Generates Temp Password
    │                          │   → Sends Email
    │                          │
    ├── Login (Acct + Pwd)     │
    ├── View Dashboard         ├── View All Customers
    ├── Transfer Money         ├── Freeze / Unfreeze Account
    └── View Transactions      └── View All Transactions
```

---

## Database Tables (auto-created by JPA)

### `customers`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | Auto increment |
| full_name | VARCHAR | Customer name |
| email | VARCHAR UNIQUE | Email address |
| pan | VARCHAR UNIQUE | PAN number |
| aadhaar | VARCHAR UNIQUE | 12-digit Aadhaar |
| account_number | VARCHAR UNIQUE | 6-digit (assigned on approval) |
| password_hash | VARCHAR | BCrypt hashed |
| balance | DOUBLE | Account balance |
| status | VARCHAR | PENDING / APPROVED / FROZEN |
| frozen | BOOLEAN | Freeze flag |
| created_at | DATETIME | Registration time |
| approved_at | DATETIME | Approval time |

### `transactions`
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT PK | Auto increment |
| sender_account_number | VARCHAR | Sender |
| recipient_account_number | VARCHAR | Recipient |
| amount | DOUBLE | Transfer amount |
| sender_balance_before/after | DOUBLE | Audit trail |
| type | VARCHAR | TRANSFER |
| status | VARCHAR | SUCCESS |
| description | VARCHAR | Transfer note |
| created_at | DATETIME | Transaction time |

---

## Troubleshooting

### MySQL Connection Failed
```
spring.datasource.password=        # Try empty password
# Or if you set a MySQL root password:
spring.datasource.password=yourpassword
```

### Port Already in Use
```properties
server.port=8081   # Change in application.properties
```

### JDK Version Error
```bash
java -version   # Must be 17+
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

### Tables Not Creating
Make sure `banking_db` database exists first, then restart the app.
`spring.jpa.hibernate.ddl-auto=create` (first run only, then change to `update`)

---

## Project Structure

```
banking-system/
├── pom.xml
├── SETUP_GUIDE.md
└── src/main/
    ├── java/com/banking/
    │   ├── BankingSystemApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java       ← Spring Security
    │   │   └── AppConfig.java            ← Async config
    │   ├── model/
    │   │   ├── Customer.java             ← JPA Entity
    │   │   └── Transaction.java          ← JPA Entity
    │   ├── repository/
    │   │   ├── CustomerRepository.java   ← JPA Repository
    │   │   └── TransactionRepository.java
    │   ├── service/
    │   │   ├── CustomerService.java      ← Business Logic
    │   │   └── EmailService.java         ← Email Notifications
    │   ├── controller/
    │   │   ├── HomeController.java
    │   │   ├── CustomerController.java   ← Customer Routes
    │   │   └── ManagerController.java    ← Manager Routes
    │   └── util/
    │       └── AccountUtils.java         ← Account/Password Gen
    └── resources/
        ├── application.properties        ← ⚙️ CONFIG HERE
        ├── schema.sql                    ← DB Schema reference
        ├── templates/
        │   ├── index.html               ← Home
        │   ├── customer/
        │   │   ├── login.html
        │   │   ├── register.html
        │   │   ├── dashboard.html
        │   │   ├── transfer.html
        │   │   └── transactions.html
        │   └── manager/
        │       ├── login.html
        │       ├── dashboard.html
        │       ├── kyc-pending.html
        │       ├── customers.html
        │       └── transactions.html
        └── static/
            └── css/style.css
```

---

Built with: Spring Boot 3.2 | Spring Security | Spring Data JPA | MySQL | Thymeleaf
