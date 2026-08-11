# 💰 Daily Expense Manager

A full-stack personal expense management application built with **Java, Spring Boot, MySQL, and JavaScript** to help users track daily spending, manage category-wise budgets, analyze spending patterns, and receive alerts before expenses get out of control.

## 🚀 Live Application

👉 https://daily-expense-manager-1-bvfx.onrender.com/

## 📌 Why I Built This

I built this project after facing a simple but frustrating problem in my own daily life.

I was making payments throughout the month — food, travel, shopping, UPI payments, and other small expenses — but I often had no clear idea of:

- Where my money was going
- How much I had already spent
- Which category was consuming the most money
- Whether I was exceeding my daily or monthly budget

One day, I made a payment and got an **"Insufficient Balance"** message unexpectedly. That moment made me realize how quickly small expenses can add up when they aren't tracked properly.

Instead of simply writing expenses down somewhere, I decided to build a complete web application that could actually help me understand my spending habits.

That idea became **Daily Expense Manager**.

I am also using the application myself to track my daily spending and understand where my money is going.

---

## 🎯 Project Objective

The main goal of this application is to make personal expense management simple, clear, and practical.

The application allows users to:

- Record daily expenses
- Organize expenses by category
- Set daily and monthly budgets
- Track remaining budgets
- Analyze category-wise spending
- Compare current and previous month expenses
- Upload and view bills
- Search and filter expenses
- Receive spending and budget alerts
- Create custom expense categories
- Delete custom categories when they are no longer required

---

## ✨ Features

### 🔐 Authentication

- User Registration
- User Login
- JWT-based authentication
- Secure API access
- Session persistence using Local Storage
- Logout functionality

### 💸 Expense Management

Users can record expenses with:

- Amount
- Category
- Date
- Payment Method
- Description / Note
- Location
- Bill Attachment

Supported payment methods:

- UPI
- Cash
- Card
- Bank Transfer
- Other

### 🏷️ Category Management

The application provides default expense categories and allows users to create custom categories.

Examples:

- Food
- Travel
- Shopping
- Bills
- Entertainment
- Education
- Medical
- Custom Categories

Users can also delete their custom categories when they are no longer needed.

### 🎯 Budget Management

Users can configure:

- Daily Budget
- Monthly Budget

The dashboard automatically calculates:

- Today's spending
- Monthly spending
- Remaining daily budget
- Remaining monthly budget
- Budget status

Budget status can indicate:

- Normal
- Warning
- Exceeded

### 📊 Dashboard

The dashboard provides a quick overview of financial activity.

It displays:

- Today's Expenses
- Monthly Expenses
- Remaining Daily Budget
- Remaining Monthly Budget
- Category-wise spending
- Recent expenses
- Budget alerts

### 📈 Spending Analytics

The Analytics section helps users understand their spending patterns.

It provides:

- Category-wise spending comparison
- Current month vs previous month spending
- Percentage change
- Daily spending trends
- Spending alerts

Charts are generated using **Chart.js**.

### 🔎 Expense Search & Filtering

Users can search and filter expenses using:

- Description
- Location
- Category
- Payment Method
- Start Date
- End Date

### 🧾 Bill Attachments

Users can attach bills or receipts to expenses.

Supported formats:

- JPG
- JPEG
- PNG
- PDF

Maximum file size:

`10 MB`

Users can:

- View bills
- Download bills

### 🚕 Quick Rapido Expense

The application includes a dedicated quick-entry option for Rapido rides.

Users can quickly enter:

- Ride Amount
- Reason / Message
- Location

This feature was also implemented to demonstrate **Java method overloading** on the backend.

### 🔔 Notifications & Alerts

The system provides notifications for important spending events such as:

- Budget warnings
- Spending alerts
- Budget exceeded conditions

Users can:

- View notifications
- Mark individual notifications as read
- Mark all notifications as read

### 💬 Feedback / Modification Requests

A feedback section allows users to submit suggestions or requests for improvements.

This makes it easier to collect real-world feedback and continuously improve the application.

---

## 🛠️ Technology Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT Authentication
- Maven

### Frontend

- HTML5
- CSS3
- JavaScript
- Chart.js

### Database

- MySQL
- TiDB Cloud

### Development Tools

- IntelliJ IDEA
- Git
- GitHub
- Postman
- Maven

### Deployment

- Render
- GitHub

---

## 🏗️ Application Architecture

```text
                    ┌──────────────────────┐
                    │      Frontend        │
                    │    HTML / CSS / JS   │
                    └──────────┬───────────┘
                               │
                               │ REST API
                               ▼
                    ┌──────────────────────┐
                    │   Spring Boot API    │
                    │                      │
                    │ Controllers          │
                    │ Services             │
                    │ Repositories         │
                    └──────────┬───────────┘
                               │
                               │ JPA / Hibernate
                               ▼
                    ┌──────────────────────┐
                    │       MySQL          │
                    │     / TiDB Cloud     │
                    └──────────────────────┘
