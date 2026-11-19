# Sales and Inventory Tracker

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-Desktop-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791)
![Java RMI](https://img.shields.io/badge/Java-RMI-yellow)

A distributed sales, purchasing, and inventory management desktop application built with JavaFX, Java RMI, and PostgreSQL. The client provides role-specific workflows, while a central RMI server owns authentication, authorization, database access, business rules, and audit logging.

## Table of Contents

- [Overview](#overview)
- [Main Features](#main-features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Server Configuration](#server-configuration)
- [Run the Application](#run-the-application)
- [Roles and Permissions](#roles-and-permissions)
- [Security](#security)
- [Backup and Export](#backup-and-export)
- [Troubleshooting](#troubleshooting)
- [Git and Team Workflow](#git-and-team-workflow)
- [Contributors](#contributors)

## Overview

The system helps a small retail or inventory operation manage products, categories, suppliers, purchases, point-of-sale transactions, stock movements, users, reports, and audit records.

### Problems Addressed

- Manual stock counts and unreliable inventory balances
- Repeated or incorrect supplier and product records
- Weak tracking of sales, purchases, and stock adjustments
- Limited visibility into stock value and reorder requirements
- Lack of accountability for administrative and transactional activity

## Main Features

- Secure login and first-administrator setup
- Role-based access for administrator, manager, and cashier users
- Product, category, and supplier management
- Point-of-sale transactions with automatic stock updates
- Supplier purchase entry and purchase history
- Stock adjustments and stock movement ledger
- Dashboard with sales, profit, stock value, and reorder indicators
- Daily sales and cashier performance reports
- Database backup creation and table-level CSV export
- Durable audit records for significant operations
- Password changes and administrator-controlled password resets
- Protection against inactive accounts and unsafe last-administrator removal

## Technology Stack

| Layer | Technology |
|---|---|
| Desktop UI | JavaFX and FXML |
| Application language | Java |
| Service communication | Java RMI |
| Database | PostgreSQL |
| JDBC | PostgreSQL JDBC driver |
| Build/development | Apache Ant / NetBeans project structure |
| Security | PBKDF2-HMAC-SHA256 password hashing |
| Reporting | Java-backed database reports and CSV exports |

## Architecture

```text
JavaFX Client
  -> RemoteServices
  -> RemoteGateway interface
  -> RMI Registry
  -> GatewayImpl
  -> Application and domain services
  -> JDBC repositories
  -> PostgreSQL
```

The client does not contain direct database access or business-rule implementations. The RMI gateway dispatches only registered service methods, adds the authenticated acting user to authorized operations, and rejects unauthenticated calls.

Core packages:

- `client` — JavaFX entry point, session state, FXML controllers, and UI helpers
- `rmi` — RMI server, gateway, registry client, and remote dispatch
- `service` — business rules, repositories, database connection, backup, and reporting
- `dto` — serializable request and response objects
- `model` — domain entities
- `exception` — application and infrastructure exception types

## Project Structure

```text
Sales_and_Inventory_tracker/
|-- src/
|   |-- client/
|   |   |-- controller/
|   |   |-- fxml/
|   |   |-- ClientApp.java
|   |   `-- SessionContext.java
|   |-- dto/
|   |-- exception/
|   |-- model/
|   |-- rmi/
|   `-- service/
|-- build.xml
|-- manifest.mf
`-- README.md
```

## Prerequisites

- JDK 17 or newer
- JavaFX 17 or newer
- PostgreSQL 13 or newer
- PostgreSQL JDBC driver (`postgresql-*.jar`)
- Apache Ant, or NetBeans with Java and JavaFX support
- Network access to the RMI server port, `1099` by default

To run across multiple computers, allow TCP traffic for the RMI registry port and configure `java.rmi.server.hostname` to the server computer's address.

## Database Setup

1. Create a PostgreSQL database named `Inventory_tracker`.
2. Create a database user that can connect to the database.
3. Apply the project database schema, including the tables and relationships used by the repositories:

   - `Roles`
   - `Users`
   - `Categories`
   - `Suppliers`
   - `Products`
   - `Sales`
   - `Sale_Items`
   - `Purchases`
   - `Purchase_Items`
   - `Stock_Movements`
   - `Audit_Log`

> The repository does not currently include a SQL migration or schema creation script. Import or create the schema separately before starting the server. `Audit_Log` is created automatically when the audit repository is initialized, but the remaining tables must exist.

Create `db.properties` in the repository root:

```properties
db.url=jdbc:postgresql://localhost:5432/Inventory_tracker
db.user=YOUR_POSTGRES_USERNAME
db.password=YOUR_POSTGRES_PASSWORD
```

Keep database credentials out of source control. In PostgreSQL, password-escaped or special-character values must be provided exactly as required by your driver and configuration.

## Server Configuration

The server is started with a properties file and a registry port:

```text
java rmi.ServerMain db.properties
```

Default RMI registry port: `1099`

Start on a custom port:

```text
java -Dserver.port=1200 rmi.ServerMain db.properties
```

For remote clients:

```text
java -Djava.rmi.server.hostname=SERVER_IP rmi.ServerMain db.properties
```

The server:

- Verifies that PostgreSQL is reachable
- Loads database settings
- Creates the service and repository graph
- Starts the RMI registry
- Registers the remote gateway as `InventoryTracker`

## Run the Application

### In NetBeans

1. Open the repository as a project.
2. Add the JavaFX SDK to the project libraries or module-path configuration.
3. Add the PostgreSQL JDBC JAR.
4. Configure the server main class as `rmi.ServerMain`.
5. Run the server and confirm that it reports that the database is reachable.
6. Configure and run the client main class as `client.ClientApp`.

### First Sign-In

When no users exist, use the **Set up administrator** link or dialog on the login screen. The first setup action creates the administrator account and the required roles.

Passwords must:

- Contain at least eight characters
- Not be identical to or the username
- Be changed by the user or administrator through the application's account tools

The login screen asks for:

- Server host
- RMI port
- Username
- Password

The default host and port are `localhost` and `1099`.

## Roles and Permissions

| Role | Access |
|---|---|
| Administrator | Full access, including users, inventory, purchases, reports, audit records, backup, and CSV export |
| Manager | Dashboard, point of sale, inventory, purchases, and audit records |
| Cashier | Point of sale and account password management |

Authorization is also checked on the server. UI visibility alone is not treated as a security boundary.

## Security

- Passwords are stored only as salted PBKDF2-HMAC-SHA256 hashes
- Password derivation uses 600,000 iterations
- Login responses never expose password hashes
- RMI operations are protected by session tokens
- The gateway adds the authenticated user ID to actions rather than trusting a client-supplied actor
- The first-user setup is permitted only when no users exist
- Accounts can be deactivated, but the last active administrator cannot be removed or demoted
- Business exceptions are returned separately from unexpected server failures

## Backup and Export

Administrators can create a PostgreSQL custom-format backup from the application. The server uses `pg_dump --format=custom --no-password`, writes it to a temporary file, and returns the file bytes to the client for saving.

Administrators can also export supported database tables as CSV files. CSV output is written in memory and saved through the JavaFX file chooser.

For production use:

- Ensure `pg_dump` is installed and available on the server's `PATH`
- Keep PostgreSQL backups outside the application directory
- Encrypt or restrict access to backup files
- Test restore procedures regularly

## Troubleshooting

### Database connection failure

- Confirm PostgreSQL is running
- Verify `db.url`, `db.user`, and `db.password`
- Confirm the database, schema, and required tables exist
- Check PostgreSQL authentication rules in `pg_hba.conf`
- Confirm the PostgreSQL JDBC driver is on the runtime classpath

### Server cannot start

- Confirm port `1099`, or the configured port, is available
- Check firewall and VPN rules for the RMI port
- For remote access, set `java.rmi.server.hostname`
- Verify `pg_dump` is available for backup operations

### Client cannot reach the server

- Confirm the server process is running
- Enter the server IP or hostname instead of `localhost` for another computer
- Use the same RMI port on both client and server
- Check that the server and client networks permit RMI traffic

### JavaFX or JDBC class not found

- Add JavaFX SDK JARs to the runtime module or classpath
- Add the PostgreSQL JDBC JAR
- Add required VM options if JavaFX is not available on the classpath

## Git and Team Workflow

Use feature branches and merge them through pull requests.

```text
git switch main
git pull origin main
git switch -c feature/your-feature
```

Commit focused changes with clear messages:

```text
git add .
git commit -m "feat: add stock movement report"
git push origin feature/your-feature
```

Before merging:

- Pull the latest `main`
- Resolve merge conflicts locally
- Compile and run the server and client
- Verify database changes and permissions
- Request a peer review
- Do not commit passwords, database dumps, or generated build output

## Contributors

This repository is maintained by a team including:

- Claire Maina
- Jospinlevrai
- Jospin Mukanire
- Glovich
- ampiremurhimanya
- Sandie
- Laureen774
