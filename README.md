# Sales and Inventory Tracker

<div align="center">

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-3776AB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13%2B-336791?logo=postgresql&logoColor=white)
![Java RMI](https://img.shields.io/badge/Java-RMI-yellow)
![Build](https://img.shields.io/badge/Build-Apache%20Ant-A81C7D)
![License](https://img.shields.io/badge/License-Not%20Specified-lightgrey)

**A distributed retail operations system for inventory control, purchasing, point-of-sale transactions, reporting, and accountability.**

</div>

## Table of Contents

- [Project Overview](#project-overview)
- [Problem Statement](#problem-statement)
- [Core Features](#core-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Database Design](#database-design)
- [Requirements Analysis](#requirements-analysis)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation and Database Setup](#installation-and-database-setup)
- [Starting the Application](#starting-the-application)
- [Using the System](#using-the-system)
- [Roles and Permissions](#roles-and-permissions)
- [Security and Audit Controls](#security-and-audit-controls)
- [Backup and CSV Export](#backup-and-csv-export)
- [Testing and Quality Assurance](#testing-and-quality-assurance)
- [Work Plan](#work-plan)
- [Team](#team)
- [Git and Collaboration Workflow](#git-and-collaboration-workflow)
- [Troubleshooting](#troubleshooting)
- [Known Limitations](#known-limitations)

## Project Overview

Sales and Inventory Tracker is a Java desktop application for a small retail or wholesale operation. It separates the JavaFX client from the server-side business and database layers through Java RMI.

The system provides one controlled workflow for:

- Recording supplier purchases
- Receiving stock
- Managing products and categories
- Completing and refunding sales
- Maintaining a stock movement ledger
- Producing sales and inventory reports
- Managing users and role-based access
- Recording operational audit events
- Backing up and exporting system data

## Problem Statement

Retail operations commonly rely on manual records or disconnected spreadsheets. This causes inaccurate stock counts, duplicated product data, weak purchase traceability, delayed reorder decisions, and limited accountability.

This application provides a centralized, role-controlled alternative. Every sale and purchase is processed through server-side services, while stock levels and movement records are updated as part of transactional database operations.

## Core Features

### Inventory

- Create, update, search, deactivate, and list products
- Unique SKU and barcode validation
- Category and supplier assignment
- Reorder-level and out-of-stock identification
- Low-stock inventory views
- Stock adjustment and movement history

### Sales and POS

- Search products for point-of-sale transactions
- Complete sales with quantities, prices, discounts, tax, and payment method
- Automatic stock deduction
- Sale voiding with stock restoration
- Partial or full sale refunds with recorded stock returns
- Recent-sale and cashier reporting

### Purchasing

- Record supplier purchases
- Capture invoice number, purchase date, notes, and line items
- Update stock from accepted purchases
- Use supplier and product catalog information

### Reporting

- Dashboard KPIs
- Completed-sales revenue
- Gross profit
- Inventory stock value
- Reorder and out-of-stock counts
- Daily sales trends
- Top-selling products
- Cashier summaries
- CSV dashboard and table exports

### Administration

- First-administrator setup
- User creation, editing, activation, and deactivation
- Password changes and administrator password resets
- Role assignment
- PostgreSQL database backups
- Audit log review

## Technology Stack

| Area | Technology |
|---|---|
| Desktop interface | JavaFX 17+, FXML, CSS |
| Programming language | Java 17+ |
| Distributed communication | Java RMI |
| Database | PostgreSQL 13+ |
| Database access | JDBC with PostgreSQL JDBC driver |
| Build system | Apache Ant / NetBeans project structure |
| Password security | PBKDF2-HMAC-SHA256, 600,000 iterations |
| Reports and exports | SQL aggregation and CSV output |
| Backup | PostgreSQL `pg_dump` |

## System Architecture

```text
+--------------------------+
| JavaFX Desktop Client    |
| FXML views and controllers|
+-------------+------------+
              |
              | Java RMI
              v
+--------------------------+
| Remote Gateway           |
| Authentication, dispatch |
+-------------+------------+
              |
              v
+--------------------------+
| Application Services     |
| Validation and access rules|
+-------------+------------+
              |
              v
+--------------------------+
| JDBC Repositories        |
| Transaction management   |
+-------------+------------+
              |
              v
+--------------------------+
| PostgreSQL Database      |
+--------------------------+
```

The RMI client uses dynamic service proxies so the client calls service interfaces without holding database code or repository implementations. The gateway:

1. Requires a valid session token for protected operations.
2. Resolves the authenticated actor from the server session.
3. Rejects unknown or mismatched operations.
4. Injects the acting-user ID into protected methods.
5. Applies server-side role checks.
6. Records audit events for significant mutations.

Core packages:

| Package | Responsibility |
|---|---|
| `client` | JavaFX application, sessions, asynchronous tasks, alerts |
| `client.controller` | UI event handling and navigation |
| `client.fxml` | Login, dashboard, POS, inventory, purchase, user, and audit views |
| `rmi` | RMI registry, gateway, and remote service proxy |
| `service` | Business rules, authorization, transactions, repositories |
| `dto` | RMI-compatible request and response objects |
| `model` | Domain entities |
| `exception` | Business and infrastructure exception types |

## Database Design

The application uses PostgreSQL and JDBC repositories.

### Main Entities

| Entity | Purpose |
|---|---|
| Roles | Available system roles |
| Users | Account, profile, password hash, role, and active state |
| Categories | Product classification |
| Suppliers | Supplier contact and status information |
| Products | SKU, barcode, pricing, unit, stock, and reorder settings |
| Purchases | Supplier invoice headers |
| Purchase_Items | Purchased product quantities and unit costs |
| Sales | POS transaction headers |
| Sale_Items | Sold product quantities, prices, and costs |
| Stock_Movements | Stock additions, deductions, returns, and adjustments |
| Audit_Log | User operations and system activity |

### Key Relationships

```text
Roles 1 ───── * Users

Categories 1 ───── * Products
Suppliers 1 ───── * Products
Suppliers 1 ───── * Purchases

Users 1 ───── * Sales
Users 1 ───── * Purchases
Users 1 ───── * Stock_Movements

Sales 1 ───── * Sale_Items
Products 1 ─── * Sale_Items

Purchases 1 ─── * Purchase_Items
Products 1 ───── * Purchase_Items

Products 1 ───── * Stock_Movements
```

> The repository does not currently include a standalone SQL migration or schema dump. Create the database tables and relationships before starting the server. The audit repository creates `Audit_Log` automatically, while other tables must already exist.

## Requirements Analysis

### Functional Requirements

| ID | Requirement | Priority | Status |
|---|---|---|---|
| FR-01 | Allow the first administrator to be created securely | Must | Implemented |
| FR-02 | Authenticate users and assign role-specific interfaces | Must | Implemented |
| FR-03 | Create, edit, activate, and deactivate users | Must | Implemented |
| FR-04 | Manage categories and suppliers | Must | Implemented |
| FR-05 | Manage products and prevent duplicate SKU/barcode values | Must | Implemented |
| FR-06 | Search products and complete sales | Must | Implemented |
| FR-07 | Prevent sales when requested stock is unavailable | Must | Implemented |
| FR-08 | Record purchases and increase stock | Must | Implemented |
| FR-09 | Maintain a stock movement ledger | Must | Implemented |
| FR-10 | Void sales and restore returned stock | Must | Implemented |
| FR-11 | Refund selected quantities and preserve sale history | Should | Implemented |
| FR-12 | Display dashboard and report metrics | Must | Implemented |
| FR-13 | Export supported tables as CSV | Should | Implemented |
| FR-14 | Create PostgreSQL custom-format backups | Should | Implemented |
| FR-15 | Display and retain audit events | Should | Implemented |
| FR-16 | Prevent removal or demotion of the last active administrator | Must | Implemented |

### Non-Functional Requirements

| Category | Requirement |
|---|---|
| Usability | Role-specific navigation should expose only permitted functions |
| Security | Passwords must be salted and hashed; passwords must not be stored in plaintext |
| Security | Protected RMI operations must require an authenticated server session |
| Integrity | Sales, purchases, refunds, and stock updates must be transaction-controlled |
| Reliability | Database and RMI failures must be surfaced as application exceptions |
| Portability | PostgreSQL settings must be externalized in `db.properties` |
| Maintainability | UI, DTO, model, service, repository, and RMI concerns must remain separated |
| Auditability | Significant mutations must record actor and operation details |

## Project Structure

```text
Sales_and_Inventory_tracker/
├── build.xml
├── manifest.mf
├── README.md
└── src/
    ├── client/
    │   ├── controller/
    │   ├── fxml/
    │   ├── AsyncTask.java
    │   ├── ClientApp.java
    │   ├── SessionContext.java
    │   └── UiAlerts.java
    ├── dto/
    ├── exception/
    ├── model/
    ├── rmi/
    │   ├── GatewayImpl.java
    │   ├── RemoteGateway.java
    │   ├── RemoteServices.java
    │   └── ServerMain.java
    └── service/
```

## Prerequisites

- JDK 17 or newer
- JavaFX 17 or newer
- PostgreSQL 13 or newer
- PostgreSQL JDBC driver JAR
- Apache Ant or NetBeans
- PostgreSQL client tools, including `pg_dump`, for backup support
- TCP access to the RMI registry port and server host

## Installation and Database Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Jospinlevrai/Sales_and_Inventory_tracker.git
cd Sales_and_Inventory_tracker
```

### 2. Create the PostgreSQL Database

```sql
CREATE DATABASE "Inventory_tracker";
```

Create or import all application tables listed in [Database Design](#database-design). Ensure primary keys, foreign keys, indexes, and not-null constraints match the SQL used in `src/service/Jdbc*Repository.java`.

### 3. Create Database Configuration

Create `db.properties` in the repository root:

```properties
db.url=jdbc:postgresql://localhost:5432/Inventory_tracker
db.user=YOUR_POSTGRES_USERNAME
db.password=YOUR_POSTGRES_PASSWORD
```

Add `db.properties` to `.gitignore` and never commit real credentials. The server reads this file as `db.properties` by default.

### 4. Configure Libraries

In NetBeans or Apache Ant:

1. Add the JavaFX SDK to the compile and runtime module/classpath.
2. Add `postgresql-*.jar` to the compile and runtime classpath.
3. Add JavaFX runtime options if JavaFX is not available on the system.
4. Confirm `build.xml` resolves the project build.

## Starting the Application

The RMI server and JavaFX client are separate processes.

### Start the Server

From the repository root:

```bash
java rmi.ServerMain db.properties
```

The default RMI registry port is `1099`.

Start on a different registry port:

```bash
java -Dserver.port=1200 rmi.ServerMain db.properties
```

For remote clients:

```bash
java -Djava.rmi.server.hostname=SERVER_IP rmi.ServerMain db.properties
```

Successful startup reports that PostgreSQL is reachable and that the RMI server is ready.

### Start the Client

In NetBeans, run `client.ClientApp`.

On the login screen, enter:

- Server host
- RMI port
- Username
- Password

The defaults are `localhost` and `1099`.

### First-Time Setup

If no users exist:

1. Open the first-administrator setup option on the login screen.
2. Enter a username, full name, and password.
3. Sign in with the created account.

The first-administrator setup is unavailable after the first user is created.

## Using the System

1. Start PostgreSQL.
2. Start `rmi.ServerMain`.
3. Start `client.ClientApp`.
4. Connect to the RMI host and port.
5. Sign in with an active account.
6. Use the role-specific navigation.

Password rules include:

- At least eight characters
- Not identical to the username
- A username or password case-insensitive comparison check
- Password changes must provide a new, different password

## Roles and Permissions

| Role | Typical Access |
|---|---|
| Administrator | Full system access, user management, backups, CSV export, and audit access |
| Manager | Dashboard, POS, inventory, purchases, reports, and audit access |
| Cashier | POS and own-account password changes |

Authorization is enforced both by UI visibility and by server-side `AccessControl` checks. Hiding a button does not grant permission.

## Security and Audit Controls

- Passwords are converted into salted PBKDF2-HMAC-SHA256 hashes.
- Password hashes are not returned in `UserInfo`.
- Login uses a constant timing path for unknown usernames.
- RMI calls use generated session tokens.
- Acting-user IDs are supplied or injected by the gateway, not trusted from ordinary client input.
- Protected operations require active users and suitable roles.
- The last active administrator is protected from deactivation or demotion.
- Significant mutations are written to `Audit_Log`.
- Database credentials are externalized and must remain outside source control.

## Backup and CSV Export

Only administrators can create backups or export tables.

Backups use PostgreSQL custom format:

```text
pg_dump --host=<host> --port=<port> --username=<user> --format=custom --no-password <database>
```

The password is supplied through `PGPASSWORD`. The backup bytes are sent to the JavaFX client and saved through a file chooser.

Supported exports include products, categories, suppliers, users, sales, sale items, purchases, purchase items, and stock movements.

## Testing and Quality Assurance

Use the following minimum test matrix for releases:

| Test Area | Verification |
|---|---|
| Database startup | Server reports PostgreSQL reachable |
| First administrator | Setup works only when no users exist |
| Authentication | Valid login succeeds; wrong credentials fail |
| Roles | Cashier cannot open administrator-only operations |
| POS | Stock decreases only after completed sale |
| Insufficient stock | Sale is rejected and stock remains unchanged |
| Purchase | Stock increases and movement is recorded |
| Void/refund | Returned stock is restored once |
| Validation | Duplicate SKU/barcode and invalid input are rejected |
| Password | Old password works; changed password works; reset works |
| Audit | Significant operations create audit entries |
| Backup | `pg_dump` produces a restorable custom-format file |
| Network | Client can connect to remote server and RMI port |

No automated test suite is currently included in the repository. Release testing should be recorded manually or automated tests should be added under a `test/` directory.

## Work Plan

| Phase | Activities | Status |
|---|---|---|
| Analysis | Problem, actors, requirements, and constraints | Completed |
| System design | Architecture, entity relationships, UI flows, RMI services | Completed |
| Database implementation | PostgreSQL repositories and transactions | Completed |
| Core modules | Authentication, users, inventory, suppliers, purchases | Completed |
| POS module | Sales, stock updates, voids, refunds | Completed |
| Reporting | Dashboard, daily sales, top products, cashier reports | Completed |
| Administration | Audit logs, backups, CSV exports | Completed |
| Validation | Input rules and server-side authorization | Completed |
| Integration testing | Client/server/database end-to-end testing | In progress |
| Final review | Deployment notes, documentation, and presentation | In progress |

## Team

| Team Member |
|---|
| Claire Maina |
| Jospin Mukanire |
| Glovich |
| ampiremurhimanya |
| Sandie |
| Laureen774 |

Every team member should collaborate through feature branches and pull requests. Contributions should be traceable to commits and GitHub activity.

## Git and Collaboration Workflow

```bash
git switch main
git pull origin main
git switch -c feature/short-description
```

Commit work clearly:

```bash
git add .
git commit -m "feat: add low-stock inventory report"
git push origin feature/short-description
```

Before merging a pull request:

- Pull the latest `main`.
- Resolve conflicts locally.
- Compile and run both server and client.
- Verify database and role behavior.
- Review the diff for credentials or generated files.
- Request peer review.
- Merge only after validation.

Do not commit:

- Database passwords
- `db.properties`
- PostgreSQL backups
- CSV exports
- IDE build products

## Troubleshooting

### Database connection failed

- Confirm PostgreSQL is running.
- Confirm the database name and credentials.
- Verify the JDBC URL.
- Confirm required tables exist.
- Verify the PostgreSQL JDBC JAR is on the runtime classpath.

### RMI server cannot start

- Confirm port `1099`, or the configured port, is available.
- Check local firewall and network rules.
- Set `java.rmi.server.hostname` for remote access.
- Confirm `rmi.ServerMain` is compiled and runnable.

### Client cannot reach the server

- Start the server first.
- Replace `localhost` with the server's network address for remote use.
- Confirm client and server ports match.
- Check that RMI traffic is permitted on both computers.

### JavaFX classes are missing

- Add the JavaFX SDK to the module/classpath.
- Add JavaFX VM options for local execution.
- Verify that the JDK and JavaFX versions match the project configuration.

### Backup fails

- Confirm `pg_dump` is installed.
- Confirm `pg_dump` is available on `PATH`.
- Confirm database credentials and URL are valid.
- Confirm the server operating system can execute PostgreSQL client tools.

## Known Limitations

- The repository requires a manually prepared PostgreSQL schema.
- There is no automated migration or seed script.
- Automated tests are not included.
- RMI session state is held only in server memory.
- RMI discovery and backup operations require direct network/tool access.
- The project has no application license unless one is explicitly added.
