# Java Console UI — Team Guide

This document defines the **layer structure**, **team roles (A / B / C)**, **fixed menu numbers**, and **Service method names** for the Delivery System console app.

--- 

# Quick Summary

- A owns Main.java, menu routing, and integration.
- B owns Insert features (menus 1–2).
- C owns Update/Delete features (menus 7–10).
- Analytics team owns Search/Analysis features (menus 3–6).

---

## Layer structure (do not mix these up)

| Package | Role | Who edits? |
|---------|------|------------|
| `delivery.Main` | Main menu loop; routes choice number → feature | **A** |
| `delivery.menu` | Screen I/O; calls `*Service` only | **A** |
| `delivery.service` | Business logic for each feature | **B** or **C** (by menu #, see roles below) |
| `delivery.db` | JDBC / SQL for each feature | **B** or **C** (by menu #); shared helpers by team agreement |
| `delivery.model` | Entities/DTOs | **B** / **C** for their features; coordinate on shared types |
| `delivery.util` | Banner, table/header helpers, shared messages | **A** |

**Rules**

- Do **not** put SQL/JDBC in `menu`.
- `menu` calls `service` only; **B** and **C** implement `service` (+ `db` as needed) for their assigned menus.
- Main menu numbers (1–10) are changed **only by role A**. Do not renumber the menu.

---

## Menu numbers (fixed — do not change)

Use these numbers for merge, demo, and integration testing.

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

Invalid input message:

```text
Please enter a number between 0 and 10.
```

Implemented in: `delivery.util.ConsoleUI`, `delivery.Main`

---

## Service method names (team contract — use only these)

Implement in `src/delivery/service/` with the signatures below.  
Do **not** invent alternate names (e.g. `modifyCustomer` instead of `updateCustomer`).

| Method | Menu # | Owner |
|--------|--------|-------|
| `CustomerService.insertCustomer()` | 1 | **B** |
| `OrderService.insertOrder()` | 2 | **B** |
| `OrderService.searchCustomerOrders()` | 3 | *not B/C — assign with team* |
| `AnalysisService.searchRestaurantSales()` | 4 | *not B/C — assign with team* |
| `AnalysisService.priceChangeAnalysis()` | 5 | *not B/C — assign with team* |
| `AnalysisService.analyzeCustomerDemographics()` | 6 | *not B/C — assign with team* |
| `CustomerService.updateCustomer()` | 7 | **C** |
| `AdminService.updateMenuPrice()` | 8 | **C** |
| `CustomerService.deleteCustomer()` | 9 | **C** |
| `OrderService.deleteOrder()` | 10 | **C** |

### CustomerService

```java
CustomerService.insertCustomer()      // B
CustomerService.updateCustomer()      // C
CustomerService.deleteCustomer()      // C
```

→ Menu: `CustomerMenu` (1, 7, 9)

### OrderService

```java
OrderService.insertOrder()            // B
OrderService.searchCustomerOrders()   // other owner (menu 3)
OrderService.deleteOrder()            // C
```

→ Menu: `OrderMenu` (2, 3, 10)

### AnalysisService (menus 4–6 — outside B/C split)

```java
AnalysisService.searchRestaurantSales()
AnalysisService.priceChangeAnalysis()
AnalysisService.analyzeCustomerDemographics()
```

→ Menu: `AnalysisMenu` (4, 5, 6)  
**Note:** Menu method `customerDemographicsAnalysis()` must call service `analyzeCustomerDemographics()` only.

### AdminService

```java
AdminService.updateMenuPrice()        // C
```

→ Menu: `AdminMenu` (8)

---

## Routing map (Main → Menu → Service)

| Menu | Owner | Main `case` | Menu | Service |
|------|-------|-------------|------|---------|
| 1 | **B** | `1` | `CustomerMenu.insertCustomer()` | `CustomerService.insertCustomer()` |
| 2 | **B** | `2` | `OrderMenu.insertOrder()` | `OrderService.insertOrder()` |
| 3 | — | `3` | `OrderMenu.searchCustomerOrders()` | `OrderService.searchCustomerOrders()` |
| 4 | — | `4` | `AnalysisMenu.searchRestaurantSales()` | `AnalysisService.searchRestaurantSales()` |
| 5 | — | `5` | `AnalysisMenu.priceChangeAnalysis()` | `AnalysisService.priceChangeAnalysis()` |
| 6 | — | `6` | `AnalysisMenu.customerDemographicsAnalysis()` | `AnalysisService.analyzeCustomerDemographics()` |
| 7 | **C** | `7` | `CustomerMenu.updateCustomer()` | `CustomerService.updateCustomer()` |
| 8 | **C** | `8` | `AdminMenu.updateMenuPrice()` | `AdminService.updateMenuPrice()` |
| 9 | **C** | `9` | `CustomerMenu.deleteCustomer()` | `CustomerService.deleteCustomer()` |
| 10 | **C** | `10` | `OrderMenu.deleteOrder()` | `OrderService.deleteOrder()` |
| 0 | **A** | `0` | Exit | — |

---

## Team roles (A / B / C)

Roles are split by **feature type**, not by “service vs database layer.”  
**B** and **C** each implement end-to-end logic (service + SQL/JDBC as needed) for their menus.

| Role | Name | Menu numbers | Summary |
|------|------|--------------|---------|
| **A** | UI / Shell / Router | All (routing only) | `Main`, menus, shared UI, integration |
| **B** | Insert functions | **1, 2** | Insert Customer, Insert Order |
| **C** | Update & delete functions | **7, 8, 9, 10** | Update Customer, Update Menu Price, Delete Customer, Delete Order |

Menus **3–6** (search & analysis) are **not** owned by B or C in this split — assign those with the rest of the team (e.g. analytics owner). Routing in `Main` stays the same.

### A — UI / Shell / Router (Main menu)

- **`Main.java` ownership** — main loop and `switch` routing
- **Main menu and navigation** — fixed options 0–10
- **Common header / table output format** — `util/ConsoleUI` (and shared format helpers as needed)
- **Input validation and error handling** — at least main-menu choice validation; sub-prompt validation in `menu` or with B/C as agreed
- **Integration points** — wire `*Menu` → `*Service` when B/C deliver; keep method names in this doc

Does **not:** implement insert/update/delete SQL or business rules for menus 1–2, 7–10 (that is B/C).

**Phase 1 done when:** layout, main loop, menu stubs, service signatures, build scripts exist.

**Phase 2:** Replace stubs in `menu` with calls to B/C services; full demo pass on menus 1–10.

### B — Insert functions

| Menu | Feature | Service method |
|------|---------|----------------|
| 1 | Insert Customer | `CustomerService.insertCustomer()` |
| 2 | Insert Order | `OrderService.insertOrder()` |

**Delivers:** implementation in `service` (+ `db` / SQL for INSERT paths), tests for menus 1 and 2.

**Does not:** menus 7–10 (C), `Main.java` / menu renumbering (A).

### C — Update & delete functions

| Menu | Feature | Service method |
|------|---------|----------------|
| 7 | Update Customer Information | `CustomerService.updateCustomer()` |
| 8 | Update Menu Price | `AdminService.updateMenuPrice()` |
| 9 | Delete Customer | `CustomerService.deleteCustomer()` |
| 10 | Delete Order | `OrderService.deleteOrder()` |

**Delivers:** implementation in `service` (+ `db` / SQL for UPDATE/DELETE paths), tests for menus 7–10.

**Does not:** menus 1–2 (B), `Main.java` / menu renumbering (A).

### How A, B, and C work together

```text
User picks menu #1 (B)
    → A: Main → CustomerMenu.insertCustomer()
    → B: CustomerService.insertCustomer() + db/SQL

User picks menu #9 (C)
    → A: Main → CustomerMenu.deleteCustomer()
    → C: CustomerService.deleteCustomer() + db/SQL
```

**Shared code:** If B and C both need a JDBC connection helper or shared `model` classes, agree in chat/PR — either one adds `db/DatabaseConnection` or similar and the other reuses it. Do not duplicate conflicting connection logic.

---

## Integration workflow (B or C PR → A wires menu)

1. **B** or **C** implements the service (+ db) for their menu numbers and opens a PR.
2. **A** updates the matching `*Menu` method, e.g.:

   ```java
   CustomerService.insertCustomer();  // menu 1 — after B is ready
   ```

   Remove the `Coming Soon` stub `println`.
3. Run `compile.bat` / `run.bat` (or README `javac` command) and test that menu number.
4. Merge.

**B/C → A:** “Menu #N is ready — please wire `*Menu`.”

**A → team:** “Menu numbers and Service method names are in `javaUI.md`; do not edit `Main` switch without A.”

---

## Build / run

| OS | Command |
|----|---------|
| Windows | `compile.bat` → `run.bat` |
| Linux / macOS | See README **Java application** section |

JDK must be on your PATH.

---

## File layout

```text
dataNestProject/
├─ javaUI.md          ← this guide (UI / integration rules)
├─ README.md          ← MySQL setup, SQL scripts, Java build
├─ compile.bat / run.bat
└─ src/delivery/
   ├─ Main.java
   ├─ menu/
   ├─ service/
   ├─ db/
   ├─ model/
   └─ util/
```

For questions or renamed methods, talk to role A first, then update **this file and `service/*.java` together**.
