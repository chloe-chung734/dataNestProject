# dataNestProject — Food Delivery Sales Tracking System

Console-based Java application using **JDBC** and **MySQL**.

| Item | Value |
|------|-------|
| **Main class** | `delivery.Main` |
| **Source entry** | `src/delivery/Main.java` |
| **Database** | MySQL schema `ewha` |
| **JDBC driver** | `lib/mysql-connector-j.jar` |

Run all commands from the **project root** (`dataNestProject/`).

---

## 1. Prerequisites

| Item | Version | Verify |
|------|---------|--------|
| JDK | 17+ (21 OK) | `java -version`, `javac -version` |
| MySQL | 8.0 | Service on port `3306` |

### Install MySQL (if needed)

**Ubuntu / Debian:**

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl enable --now mysql
```

**CentOS / RHEL / Fedora:**

```bash
sudo dnf install @mysql
sudo systemctl enable --now mysqld
```

**Windows — add MySQL to PATH (optional):**

```powershell
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.0\bin"
```

---

## 2. Database setup

Create schema and load sample data **before** starting the Java app.

**Linux / macOS:**

```bash
mysql -u root -p < createschema.sql
mysql -u root -p < initdata.sql
```

**Windows (CMD):**

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < createschema.sql
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < initdata.sql
```

**Remote MySQL:**

```bash
mysql -h <host> -P <port> -u <user> -p < createschema.sql
mysql -h <host> -P <port> -u <user> -p < initdata.sql
```

`createschema.sql` creates database `ewha` with 10 tables, 4 views, and 4 indexes.  
`initdata.sql` inserts 10 rows per table.

Verify:

```sql
USE ewha;
SHOW FULL TABLES WHERE Table_type = 'VIEW';
SELECT COUNT(*) FROM order_details_view;
```

**Reset database:**

```bash
mysql -u root -p < dropschema.sql
mysql -u root -p < createschema.sql
mysql -u root -p < initdata.sql
```

If views are missing on an existing database:

```bash
sed -n '136,222p' createschema.sql | mysql -u root -p
```

> Run SQL scripts from Terminal/CMD only — not inside the `mysql>` prompt.

---

## 3. Configure database connection

Edit `src/delivery/db/DatabaseConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/ewha?useSSL=false&allowPublicKeyRetrieval=true";
private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "YOUR_MYSQL_PASSWORD");
```

Or set environment variables (no file edit):

```bash
export DB_USER=root
export DB_PASSWORD=YourPassword
```

For remote MySQL (e.g. HeatWave), replace `localhost` in `URL`.

---

## 4. Build

**Windows:**

```bash
compile.bat
```

**Linux / macOS:**

```bash
mkdir -p out
javac -d out -sourcepath src -cp lib/mysql-connector-j.jar \
  src/delivery/Main.java \
  src/delivery/util/ConsoleUI.java \
  src/delivery/menu/*.java \
  src/delivery/service/*.java \
  src/delivery/db/DatabaseConnection.java
```

---

## 5. Run

**Windows:**

```bash
run.bat
```

**Linux / macOS:**

```bash
java -cp "out:lib/mysql-connector-j.jar" delivery.Main
```

**Alternative — run from JAR:**

```bash
# Build JAR first
package.bat          # Windows
chmod +x package.sh && ./package.sh   # Linux / macOS

# Run (keep lib/mysql-connector-j.jar in lib/)
java -jar dataNestProject.jar
```

Expected on startup:

- Main menu shows options **0–10**
- First database menu prints: `✓ Connected to MySQL database`
- Invalid menu input shows: `Please enter a number between 0 and 10.`

---

## 6. Sample test inputs

Use after `initdata.sql` is loaded:

| Menu | Input | Expected |
|------|-------|----------|
| 2 | customer `1`, restaurant `2`, item `6`, qty `2`, `n` | `✓ Order recorded successfully!` |
| 3 | `emily.park@gmail.com` | Order rows |
| 4 | `Seoul` | Restaurant sales totals |
| 5 | `Americano` | Before/after price era table |
| 6 | `daniel.kim@naver.com` | Demographic spending table |
| 0 | `0` | `Goodbye.` |

---

## 7. Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `No suitable driver found` | JDBC not on classpath | Use `run.bat` or `-cp lib/mysql-connector-j.jar` |
| `Access denied for user 'root'` | Wrong password | Fix `DatabaseConnection.java` or `DB_PASSWORD` env var |
| `Unknown database 'ewha'` | Schema not created | Run `createschema.sql` |
| `Table 'order_details_view' doesn't exist` | Views missing | Re-run `createschema.sql` or view-only SQL above |
| `Table already exists` | Schema exists | Run `dropschema.sql` first, then recreate |
| DB menu fails after code edit | Stale `.class` files | Re-run `compile.bat` / `javac` |
| `./compile.bat` not recognized | Windows CMD | Use `compile.bat` (no `./`) |
