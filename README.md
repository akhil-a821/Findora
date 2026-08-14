# CampusFind — Smart College Lost & Found System

CampusFind is a modern full-stack web application designed for college campuses to connect students and staff who have lost or found items. It uses a **Java Spring Boot 3.x** backend, **Spring Security**, **Spring Data JPA**, **Hibernate**, **MySQL/H2**, **Thymeleaf**, and **Bootstrap 5**.

The system features an **intelligent 100-point rule-based smart matching engine** that automatically identifies potential matches between lost and found reports and guides users through a secure ownership claim verification and return workflow.

---

## 🌟 Key Features

1. **Smart Matching Engine**:
   - Automatically compares newly posted LOST reports against FOUND reports (and vice-versa).
   - Calculates a match percentage out of 100 based on category, location, date proximity, brand, color, item name similarity, and keyword overlap.
   - Displays clear matching factor breakdowns (e.g. `✓ Same category`, `✓ Same location`, `✓ Same day`, `✓ Same color`).

2. **Secure Verification & Claim System**:
   - Private verification details (e.g. serial numbers, custom engravings, hidden stickers) are stored securely and never shown to the public.
   - Claimants submit ownership explanations and verification answers.
   - Finders review and approve or reject claims.

3. **Item Return Flow**:
   - Once a claim is approved, the item status transitions to `CLAIMED`.
   - Finders mark the item as `RETURNED` upon physical exchange, updating platform statistics and notifying both parties.

4. **In-App Real-Time Notification System**:
   - Instant notification alerts for potential matches, claim submissions, approvals, rejections, and completed returns.
   - Top navbar badge indicator with unread notification counter.

5. **Campus-Wide Search & Multi-Filter**:
   - Case-insensitive search across item names, descriptions, brands, categories, and locations.
   - Quick filtering by Lost/Found status, Category, and Campus Location (Library, Canteen, Computer Lab, Main Gate, etc.).

6. **User Dashboard & My Reports**:
   - Personal dashboard with summary statistics, potential matches feed, and activity log.
   - Tabbed view to manage posted reports and view claim statuses.

7. **Admin Moderation Panel**:
   - Restricted to `ROLE_ADMIN`.
   - System-wide statistics, user directory management, and report moderation (Delete/Close reports).

8. **Image Upload & Previews**:
   - Upload item photos with file format validation (JPG, PNG, WEBP) and live image preview.
   - Automatic fallback SVG category placeholders if no image is uploaded.

---

## 🛠️ Technology Stack

### Backend
- **Java**: 17+
- **Spring Boot**: 3.2.2
- **Spring Web / REST APIs**: Spring MVC
- **Spring Security**: Form login, BCrypt password hashing, session management, role-based authorization (`ROLE_USER`, `ROLE_ADMIN`)
- **Spring Data JPA & Hibernate**: Entity mapping and custom queries
- **Validation**: Jakarta Bean Validation

### Database
- **H2 Database**: Configured by default for instant zero-config launch (`jdbc:h2:mem:campusfinddb`)
- **MySQL**: Full support configured in `application.properties`

### Frontend
- **Thymeleaf**: Server-side template engine
- **Bootstrap 5**: Responsive layout with custom HSL SaaS styling overlay
- **FontAwesome 6**: Icons
- **Vanilla JavaScript**: Dynamic image previews, toast notifications, AJAX mark-as-read

---

## 📊 Smart Matching Engine Algorithm (100 Points Total)

| Matching Criteria | Max Points | Description |
| :--- | :--- | :--- |
| **Category Match** | 20 pts | Exact category match (e.g. Electronics ↔ Electronics) |
| **Location Match** | 25 pts | Exact or substring campus location match (e.g. Library) |
| **Date Proximity** | 20 pts | Same day (20 pts), 1 day difference (15 pts), 2-3 days (10 pts) |
| **Brand Match** | 10 pts | Case-insensitive brand name similarity (e.g. Apple) |
| **Color Match** | 10 pts | Matching color attribute (e.g. White) |
| **Item Name Similarity**| 10 pts | Jaccard / token similarity on item title |
| **Description Keywords**| 5 pts | Overlapping key nouns & descriptor words |

*Note: Matches are created and displayed when the calculated score is $\ge 60\%$.*

---

## 🚀 How to Run the Application

### Prerequisites
- **Java 17** or newer installed (`java -version`)
- **Apache Maven** installed (`mvn -version`)

### Step 1: Clone or Navigate to Project Directory
```bash
cd "c:/Users/anila/OneDrive/Desktop/my jav project"
```

### Step 2: Build the Project
```bash
mvn clean compile
```

### Step 3: Run the Spring Boot Application
```bash
mvn spring-boot:run
```

### Step 4: Access in Browser
Open your browser and visit:
```text
http://localhost:8080/
```

- H2 Database Console (Optional): `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:campusfinddb`, User: `sa`, Password: *blank*)

---

## 🗄️ MySQL Database Setup (Optional)

To connect CampusFind to a local MySQL instance:

1. Open MySQL Command Line or Workbench and create the database:
   ```sql
   CREATE DATABASE campusfind_db;
   ```
2. Open `src/main/resources/application.properties` and uncomment the MySQL block:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/campusfind_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```
3. Restart the Spring Boot application. Hibernate will automatically create all tables and relationships (`users`, `reports`, `matches`, `claims`, `notifications`).

---

## 🏗️ Project Architecture & Layering

```text
src/main/java/com/campusfind/
├── CampusFindApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── ClaimController.java
│   ├── DashboardController.java
│   ├── GlobalModelAttributes.java
│   ├── HomeController.java
│   ├── MatchController.java
│   ├── NotificationController.java
│   └── ReportController.java
├── dto/
│   ├── ClaimRequestDto.java
│   ├── MatchResponseDto.java
│   ├── ReportRequestDto.java
│   └── UserRegistrationDto.java
├── entity/
│   ├── Claim.java
│   ├── Match.java
│   ├── Notification.java
│   ├── Report.java
│   ├── User.java
│   └── enums/
│       ├── ClaimStatus.java
│       ├── NotificationType.java
│       ├── ReportStatus.java
│       ├── ReportType.java
│       └── Role.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── ClaimRepository.java
│   ├── MatchRepository.java
│   ├── NotificationRepository.java
│   ├── ReportRepository.java
│   └── UserRepository.java
└── service/
    ├── ClaimService.java
    ├── CustomUserDetailsService.java
    ├── MatchingService.java
    ├── NotificationService.java
    ├── ReportService.java
    └── UserService.java
```

---

## 🔮 Future Enhancements
- AI-based image similarity recognition using computer vision
- Interactive campus map integration for pin-pointing lost locations
- Automated email notification dispatch via Spring Mail
- Mobile PWA / Native app companion

---

© 2026 CampusFind — Smart College Lost & Found System
