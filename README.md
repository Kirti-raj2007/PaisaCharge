# PaisaCharge: Mobile Recharge and Billing System

**CSE2003 | Object Oriented Programming using JAVA | B.Tech CSE Semester 3 | Mini Project | Group 13**

An application for mobile recharge, plan selection and bill generation for subscribers.
The project comes in two versions that share the same design:

| Version | What it is | Where |
|---|---|---|
| Java desktop app | Swing UI, JDBC and MySQL. This is the main submission. | `src/`, `schema.sql` |
| Web demo | Single-page site, deployable to Netlify by drag and drop. | `index.html` (separate file) |

## Team

| Role | Name | UID |
|---|---|---|
| Leader | Kirti Raj | 2510001027 |
| Member 1 | Tanishq | 2510001002 |
| Member 2 | Mir Arsalan | 2510001009 |
| Member 3 | Pritam | 2510001015 |

## Modules

1. **Subscriber records**: add, view and remove subscribers (name, mobile number, Prepaid or Postpaid).
2. **Plan catalogue**: list of prepaid and postpaid plans with price, validity and data.
3. **Recharge**: pick a subscriber's number and a plan, then recharge. Validity is extended from the current expiry date if the plan is still active.
4. **Bill generation**: invoice with plan amount, 18% GST and total. It can be printed.
5. **Recharge history**: every recharge with date, number, plan and amount. Select a row to reopen its bill.

## Syllabus concepts applied

| Unit | Concepts | Where in the code |
|---|---|---|
| 1 | Classes `Subscriber`, `Plan`, `Recharge`; `Math` class; constructors | `Subscriber.java`, `Plan.java`, `Recharge.java` |
| 2 | Inheritance `Plan` -> `PrepaidPlan` / `PostpaidPlan`; overriding; interface `Rechargeable` | `Plan.java`, `PrepaidPlan.java`, `PostpaidPlan.java`, `Rechargeable.java`, `RechargeService.java` |
| 3 | `InvalidNumberException`; thread for plan-expiry check | `InvalidNumberException.java`, `ExpiryChecker.java` |
| 4 | JDBC `CallableStatement` for recharge | `RechargeService.java`, stored procedure `recharge_plan` in `schema.sql` |
| 5 | Swing `GridLayout` forms; `BorderLayout`; `ActionListener` | `MainFrame.java` |

### How the OOP design works

- `Plan` holds the common fields and methods. `PrepaidPlan` keeps the listed price (GST included). `PostpaidPlan` overrides `cost()`, `base()` and `gst()` to add 18% GST on top.
- `RechargeService` implements `Rechargeable` and does all database work.
- A mobile number must be 10 digits and start with 6, 7, 8 or 9. Otherwise `InvalidNumberException` is thrown, and the UI shows it as a dialog.
- `ExpiryChecker` runs on its own thread every 5 seconds. It updates the status bar with active and expired counts.
- The `recharge_plan` stored procedure inserts the recharge row and updates the subscriber's expiry date in one call.

## Database

Database name: `paisacharge`. Tables: `subscribers`, `plans`, `recharges`.

```
subscribers(id, name, mobile UNIQUE, type, expiry)
plans(id, name, price, days, data, type)
recharges(id, subscriber_id -> subscribers.id, plan_id -> plans.id,
          amount, base, gst, recharged_at, expiry)
```

## Running the Java app

**Requirements:** JDK 17 or newer, MySQL 8, and the MySQL Connector/J jar
(download from dev.mysql.com/downloads/connector/j, choose "Platform Independent").

1. Create the database and sample data:
   ```
   mysql -u root -p < schema.sql
   ```
2. Open `src/Db.java` and set `USER` and `PASS` to your MySQL login.
3. Compile and run. Replace the jar name with the version you downloaded.

   Windows:
   ```
   javac -encoding UTF-8 -d out src\*.java
   java -cp "out;mysql-connector-j-8.4.0.jar" MainFrame
   ```
   Mac / Linux:
   ```
   javac -encoding UTF-8 -d out src/*.java
   java -cp "out:mysql-connector-j-8.4.0.jar" MainFrame
   ```

**Using it:** add a subscriber, open Recharge, enter the number, choose a plan and click Recharge now. The bill opens automatically. A Prepaid subscriber can only buy Prepaid plans, and the same applies to Postpaid.
The Light mode / Dark mode button in the header switches the theme.

## Deploying the web demo to Netlify

The web demo is one file, `index.html`. It stores its data in the browser (localStorage) instead of MySQL, because Netlify cannot run Java or a database.

1. Put `index.html` inside a folder, for example `paisacharge`.
2. Open app.netlify.com/drop in your browser and sign in.
3. Drag the folder onto the page. Netlify gives you a live link in a few seconds.
4. To update the site later, open the site in Netlify, go to Deploys, and drag the folder again.

## Troubleshooting

| Problem | Fix |
|---|---|
| "Database error: Communications link failure" | MySQL is not running, or the port in `Db.java` is wrong. |
| "Access denied for user" | `USER` or `PASS` in `Db.java` is wrong. |
| "No suitable driver" | The Connector/J jar is missing from the `-cp` classpath. |
| "Unknown database 'paisacharge'" | Run `schema.sql` first. |
| Plan dropdown is empty | `schema.sql` was not run fully. Re-run it. If plans are duplicated, drop the `plans` table rows before re-importing. |
| Buttons look unthemed on macOS | The app already switches to the cross-platform look. Run it with JDK 17 or newer. |

## Project structure

```
PaisaCharge/
  README.md
  schema.sql
  src/
    InvalidNumberException.java
    Rechargeable.java
    Plan.java
    PrepaidPlan.java
    PostpaidPlan.java
    Subscriber.java
    Recharge.java
    Db.java
    RechargeService.java
    ExpiryChecker.java
    MainFrame.java
```
