# Java Console UI — Team Guide

This document defines the **layer structure**, **team roles (A / B / C / Analytics)**, **fixed menu numbers**, and **Service method names** for the Delivery System console app.

---

## Quick Summary

- **A** owns `Main.java`, menu routing, and `ConsoleUI`.
- **B** owns Insert features (menus 1–2). Menu 2 inserts `orders`, `order_item`, and `bill` in one transaction.
- **C** owns Update/Delete features (menus 7–10).
- **Analytics / Pillar 3** owns Search/Analysis features (menus 3–6), view-based.

All menus (1–10) are implemented and wired through `*Menu` → `*Service`.

---

## Menu routing

| # | Feature | `Main` → Menu → Service |
|---|---------|-------------------------|
| 1 | Insert Customer | `CustomerMenu` → `CustomerService.insertCustomer()` |
| 2 | Insert Order | `OrderMenu` → `OrderService.insertOrder()` |
| 3 | Search Customer Orders | `OrderMenu` → `OrderService.searchCustomerOrders()` |
| 4 | Search Restaurant Sales | `AnalysisMenu` → `AnalysisService.searchRestaurantSales()` |
| 5 | Analyze Price Change | `AnalysisMenu` → `AnalysisService.priceChangeAnalysis()` |
| 6 | Analyze Customer Demographics | `AnalysisMenu` → `AnalysisService.analyzeCustomerDemographics()` |
| 7 | Update Customer | `CustomerMenu` → `CustomerService.updateCustomer()` |
| 8 | Update Menu Price | `AdminMenu` → `AdminService.updateMenuPrice()` |
| 9 | Delete Customer | `CustomerMenu` → `CustomerService.deleteCustomer()` |
| 10 | Delete Order | `OrderMenu` → `OrderService.deleteOrder()` |

---

## Layer structure

| Package | Role | Who edits? |
|---------|------|------------|
| `delivery.Main` | Main menu loop; routes choice → `*Menu` | **A** |
| `delivery.menu` | Thin delegate — calls `*Service` only | **A** (wiring) |
| `delivery.service` | Console I/O (`Scanner`), `PreparedStatement` SQL, results | **B**, **C**, **Analytics** |
| `delivery.db` | `DatabaseConnection` — singleton JDBC connection | Shared |
| `delivery.model` | Reserved for DTOs; currently `package-info.java` only | TBD |
| `delivery.util` | Banner, main menu text, invalid-choice message | **A** |

**Rules**

- Do **not** put SQL/JDBC in `menu`.
- `menu` calls `service` only.
- Prompts and SQL live in `service` (current codebase pattern).
- Main menu numbers (1–10) are changed **only by role A**.

---

## Menu numbers (fixed — do not change)

| # | Feature |
|---|---------|
| 1 | Insert Customer |
| 2 | Insert Order |
| 3 | Search Customer Orders |
| 4 | Search Restaurant Sales |
| 5 | Analyze Price Change |
| 6 | Analyze Customer Demographics |
| 7 | Update Customer |
| 8 | Update Menu Price |
| 9 | Delete Customer |
| 10 | Delete Order |
| 0 | Exit |

Invalid input message (main menu only):

```text
Please enter a number between 0 and 10.
```

Implemented in: `delivery.util.ConsoleUI.INVALID_CHOICE_MESSAGE`, `delivery.Main`

---

## Service method names (team contract)

Implement in `src/delivery/service/`. Do **not** rename these methods.

| Method | Menu # | Owner |
|--------|--------|-------|
| `CustomerService.insertCustomer()` | 1 | **B** |
| `OrderService.insertOrder()` | 2 | **B** |
| `OrderService.searchCustomerOrders()` | 3 | **Analytics** |
| `AnalysisService.searchRestaurantSales()` | 4 | **Analytics** |
| `AnalysisService.priceChangeAnalysis()` | 5 | **Analytics** |
| `AnalysisService.analyzeCustomerDemographics()` | 6 | **Analytics** |
| `CustomerService.updateCustomer()` | 7 | **C** |
| `AdminService.updateMenuPrice()` | 8 | **C** |
| `CustomerService.deleteCustomer()` | 9 | **C** |
| `OrderService.deleteOrder()` | 10 | **C** |

### CustomerService → `CustomerMenu` (1, 7, 9)

```java
CustomerService.insertCustomer()
CustomerService.updateCustomer()
CustomerService.deleteCustomer()
```

### OrderService → `OrderMenu` (2, 3, 10)

```java
OrderService.insertOrder()
OrderService.searchCustomerOrders()
OrderService.deleteOrder()
```

Menu 2 (`insertOrder`) flow: validate IDs → collect line items → `setAutoCommit(false)` → INSERT `orders` → INSERT `order_item`(s) → INSERT `bill` (10% tax) → `commit()`.

### AnalysisService → `AnalysisMenu` (4, 5, 6)

```java
AnalysisService.searchRestaurantSales()
AnalysisService.priceChangeAnalysis()
AnalysisService.analyzeCustomerDemographics()
```

Menu method `customerDemographicsAnalysis()` must call `analyzeCustomerDemographics()` only.

### AdminService → `AdminMenu` (8)

```java
AdminService.updateMenuPrice()
```

---

## Routing map (Main → Menu → Service)

| Menu | Owner | Main `case` | Menu class | Service method |
|------|-------|-------------|------------|----------------|
| 1 | **B** | `1` | `CustomerMenu.insertCustomer()` | `CustomerService.insertCustomer()` |
| 2 | **B** | `2` | `OrderMenu.insertOrder()` | `OrderService.insertOrder()` |
| 3 | **Analytics** | `3` | `OrderMenu.searchCustomerOrders()` | `OrderService.searchCustomerOrders()` |
| 4 | **Analytics** | `4` | `AnalysisMenu.searchRestaurantSales()` | `AnalysisService.searchRestaurantSales()` |
| 5 | **Analytics** | `5` | `AnalysisMenu.priceChangeAnalysis()` | `AnalysisService.priceChangeAnalysis()` |
| 6 | **Analytics** | `6` | `AnalysisMenu.customerDemographicsAnalysis()` | `AnalysisService.analyzeCustomerDemographics()` |
| 7 | **C** | `7` | `CustomerMenu.updateCustomer()` | `CustomerService.updateCustomer()` |
| 8 | **C** | `8` | `AdminMenu.updateMenuPrice()` | `AdminService.updateMenuPrice()` |
| 9 | **C** | `9` | `CustomerMenu.deleteCustomer()` | `CustomerService.deleteCustomer()` |
| 10 | **C** | `10` | `OrderMenu.deleteOrder()` | `OrderService.deleteOrder()` |
| 0 | **A** | `0` | Exit | `DatabaseConnection.closeConnection()` |

---

## Analytics menus — views and user input (Pillar 3)

All analytics SELECT menus use `PreparedStatement` and SQL **views** defined in `createschema.sql`.

| Menu | Service | View | User input | SQL summary |
|------|---------|------|------------|-------------|
| 3 | `OrderService.searchCustomerOrders()` | `order_details_view` | Customer **email** | `SELECT ... FROM order_details_view WHERE email = ?` |
| 4 | `AnalysisService.searchRestaurantSales()` | `order_details_view` | Restaurant **city** | Subquery on distinct orders, `GROUP BY restaurant_name` |
| 5 | `AnalysisService.priceChangeAnalysis()` | `v_item_sales_by_price_era` | Menu item **name** | `GROUP BY price_era, era_old_price, ...` |
| 6 | `AnalysisService.analyzeCustomerDemographics()` | `v_customer_sales_by_demo` | Customer **email** | `GROUP BY demo period columns` |

`customer_spending_view` exists in the schema for per-customer aggregation but is **not** used by any menu.

### Sample test data (`initdata.sql`)

| Menu | Example input |
|------|---------------|
| 2 | customer `1`, restaurant `2`, item `6`, qty `2`, `n` |
| 3 | `emily.park@gmail.com` |
| 4 | `Seoul` |
| 5 | `Americano` |
| 6 | `daniel.kim@naver.com` |

---

## Team roles (A / B / C / Analytics)

| Role | Menu numbers | Summary |
|------|--------------|---------|
| **A** | All (routing) | `Main`, `*Menu` wiring, `ConsoleUI` |
| **B** | 1, 2 | Insert Customer; Insert Order (`orders` + `order_item` + `bill`) |
| **C** | 7–10 | Update Customer, Update Menu Price, Delete Customer, Delete Order |
| **Analytics** | 3–6 | View-based SELECT menus (REQ6, REQ7) |

### A — UI / Shell / Router

- Owns `Main.java` switch loop and `ConsoleUI`
- Wires `*Menu` → `*Service` when features are delivered
- Does **not** implement business SQL for B/C/Analytics menus

### B — Insert

| Menu | Feature | Notes |
|------|---------|-------|
| 1 | Insert Customer | Full INSERT into `customer` |
| 2 | Insert Order | Transaction: `orders` + `order_item` + `bill`; 10% tax on subtotal |

### C — Update & Delete

| Menu | Feature | Notes |
|------|---------|-------|
| 7 | Update Customer | Transaction; also inserts `customer_demographic_history` |
| 8 | Update Menu Price | Transaction; inserts `menu_price_history` |
| 9 | Delete Customer | Customer ID; blocked if customer has orders (FK RESTRICT) |
| 10 | Delete Order | Order ID; cascades `order_item` + `bill` |

### Analytics — Search & Analysis (Pillar 3)

Delivers menus 3–6 in `OrderService` / `AnalysisService` using views.  
Schema views are defined in `createschema.sql` (views section at end of file).

---

## Integration workflow

### When B or C delivers a new feature

1. B/C implements `service` and opens a PR.
2. A ensures `*Menu` delegates to `*Service` (no `Coming Soon` stub).
3. Recompile and test the menu number.
4. Merge.

---

## Build / run

| OS | Command |
|----|---------|
| Windows | `compile.bat` → `run.bat` |
| Linux / macOS | See [README.md](README.md) **How to execute** section |
| JAR (REQ18) | `package.bat` / `package.sh` → `java -jar dataNestProject.jar` |

Requires JDK on PATH and `lib/mysql-connector-j.jar` on the classpath at runtime.

**Database credentials:** edit `DatabaseConnection.java` or set `DB_USER` / `DB_PASSWORD` environment variables.

---

## File layout

```text
dataNestProject/
├─ javaUI.md              ← this guide
├─ README.md              ← execution instructions (REQ19)
├─ compile.bat / run.bat
├─ package.bat / package.sh
├─ dataNestProject.jar    ← after packaging (REQ18)
├─ lib/mysql-connector-j.jar
├─ createschema.sql       ← 10 tables + 4 views + 4 indexes
├─ initdata.sql
├─ dropschema.sql
└─ src/delivery/
   ├─ Main.java
   ├─ menu/
   ├─ service/
   ├─ db/DatabaseConnection.java
   ├─ model/
   └─ util/ConsoleUI.java
```

For questions or renamed methods, talk to role **A** first, then update **this file and `service/*.java` together**.
