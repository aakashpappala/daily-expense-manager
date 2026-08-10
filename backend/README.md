# DAILY EXPENSE MANAGER

A complete, production-ready, multi-user personal daily expense tracking application built with **Java 17**, **Spring Boot 3.2.5**, **Spring Security (JWT)**, **Spring Data JPA / Hibernate**, **MySQL**, and a responsive **HTML5/CSS3/JavaScript** frontend.

---

## 🌟 Key Features

1. **User Authentication & Data Isolation**:
   - Secure Registration & Login powered by **Spring Security** and **JWT Tokens**.
   - Passwords hashed using **BCrypt**.
   - Strict database-level and service-level data isolation. User A can never view or modify User B's expenses, categories, budgets, notifications, or bill attachments.

2. **Default & Custom Categories**:
   - New users are automatically seeded with default categories:
     - `D-Mart`
     - `Chicken`
     - `Eggs`
     - `Vegetables`
     - `Rapido`
     - `Church Offerings`
     - `Milk & Curd`
   - Users can also create, update, and manage custom categories.

3. **Interactive Dashboard**:
   - Metrics showing Today's Expenses, Month's Expenses, Daily Budget, Monthly Budget, Remaining Limits, and Budget Status (Normal / Warning / Exceeded).
   - Visual category spending grid highlighting high-spending categories.
   - Recent expenses list and system alerts.

4. **Bill Attachment Management**:
   - Attach bill files during expense creation or later.
   - Supported formats: **JPG**, **JPEG**, **PNG**, **PDF** (up to 10MB).
   - Features: View inline in browser, Download bill, Delete bill.
   - Protected against path traversal attacks.

5. **Category Details & Daily Breakdown (Core Feature)**:
   - Monthly category spending summary.
   - Daily spending breakdown table.
   - Daily average, Highest spending day, and Lowest spending day calculations.
   - Clickable dates opening **Daily Detail Modal** showing itemized expenses (e.g. Groceries ₹350, Cleaning ₹120, Snacks ₹80) with bill previews.

6. **Spending Analysis & Dynamic Alerts**:
   - Current month vs Previous month comparison per category.
   - Dynamic percentage change calculation (`%` increase/decrease).
   - Warning banners triggered when category spending increases by 25%+.

7. **Rapido Ride Special Requirement (Java Method Overloading)**:
   - Demonstrates Java Method Overloading in `ExpenseService`:
     - `addRapidoExpense(BigDecimal amount)`
     - `addRapidoExpense(BigDecimal amount, String message)`
     - `addRapidoExpense(BigDecimal amount, String message, String location)`
     - `addRapidoExpense(BigDecimal amount, String message, String location, LocalDate expenseDate, String paymentMethod)`

8. **Budgets & Notifications System**:
   - Set custom Daily and Monthly budget limits.
   - Automatic background warnings:
     - `⚠️ Daily budget exceeded by ₹X`
     - `⚠️ Monthly budget exceeded by ₹Y`
     - `⚠️ D-Mart spending is 30% higher than last month.`
   - Notification list with read status toggle and unread badge.

9. **Analytics & Interactive Charts**:
   - Visual charts powered by **Chart.js**:
     - Category spending comparison bar chart.
     - 14-day daily spending trend line chart.

10. **Search & Filter**:
    - Filter expenses by category, date range, payment method (Cash, UPI, Card, Bank Transfer, Other), amount range, or keyword search.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Web, Spring Data JPA, Hibernate, Spring Security (JWT, BCrypt), Maven.
- **Database**: MySQL 8.x.
- **Frontend**: HTML5, Vanilla CSS3 (Custom Dark Theme with Glassmorphism), Vanilla JavaScript, Chart.js.
- **Testing**: JUnit 5, Mockito.

---

## 🗄️ Database Schema

The relational database (`expensetrack`) consists of 5 normalized entities:
1. `users` — `id`, `full_name`, `email` (unique), `password` (BCrypt), `created_at`, `updated_at`
2. `categories` — `id`, `name`, `is_default`, `user_id` (FK), `created_at`
3. `expenses` — `id`, `amount`, `expense_date`, `description`, `payment_method`, `location`, `bill_path`, `bill_file_name`, `bill_file_type`, `user_id` (FK), `category_id` (FK), `created_at`, `updated_at`
4. `budgets` — `id`, `daily_budget`, `monthly_budget`, `user_id` (FK, Unique), `created_at`, `updated_at`
5. `notifications` — `id`, `message`, `type`, `is_read`, `user_id` (FK), `created_at`

---

## 🚀 How to Run the Application

### 1. Prerequisites
- **Java 17** or higher installed.
- **Maven** installed.
- **MySQL Server** running on `localhost:3306`.

### 2. MySQL Database Setup
Create database `expensetrack`:
```sql
CREATE DATABASE IF NOT EXISTS expensetrack;
```

Ensure MySQL credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expensetrack?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=1432
```

### 3. Build & Test
Compile the application and run unit tests:
```bash
mvn clean test
```

### 4. Start the Application
Run the Spring Boot application:
```bash
mvn spring-boot:run
```

### 5. Access the Web Frontend
Open your browser and navigate to:
```
http://localhost:8082
```

---

## 📑 REST API Documentation Summary

| Endpoint | Method | Description |
|---|---|---|
| `/api/auth/register` | `POST` | Register a new user & seed default categories |
| `/api/auth/login` | `POST` | Authenticate user & return JWT token |
| `/api/auth/me` | `GET` | Get authenticated user info |
| `/api/expenses` | `GET` | List/Filter user expenses |
| `/api/expenses` | `POST` | Add expense (supports Multipart bill attachment) |
| `/api/expenses/rapido` | `POST` | Add Rapido expense (Method Overloading) |
| `/api/expenses/{id}` | `PUT` / `DELETE` | Update or Delete expense |
| `/api/expenses/{id}/bill/view` | `GET` | View attached bill inline |
| `/api/expenses/{id}/bill/download` | `GET` | Download attached bill file |
| `/api/categories` | `GET` / `POST` | Manage user categories |
| `/api/categories/{id}/details` | `GET` | Get category spending breakdown & metrics |
| `/api/categories/{id}/daily-details` | `GET` | Get daily itemized expenses for category |
| `/api/dashboard` | `GET` | Get dashboard summary data |
| `/api/budgets` | `GET` / `PUT` | View or update daily/monthly budget limits |
| `/api/notifications` | `GET` | List notifications |
| `/api/notifications/{id}/read` | `PUT` | Mark notification as read |
| `/api/reports/analytics` | `GET` | Get spending analytics & trend report data |

---

## 🔒 Security & Data Isolation

- All data queries filter strictly by `user = :authenticatedUser`.
- User authentication is validated per HTTP request using `JwtAuthenticationFilter`.
- File attachments are stored with UUID identifiers (`user_{id}_{uuid}.ext`) preventing file guessing or unauthorized cross-user access.
