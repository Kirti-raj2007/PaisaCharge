# PaisaCharge - Mobile Recharge and Billing System (Java)
CSE2003 | Object Oriented Programming using JAVA | Group 13

## Requirements
- JDK 17 or newer
- MySQL 8
- MySQL Connector/J jar (download from dev.mysql.com/downloads/connector/j, choose "Platform Independent")

## Setup
1. Create the database: `mysql -u root -p < schema.sql`
2. Open `src/Db.java` and set USER and PASS to your MySQL login.
3. Compile and run (replace the jar name with yours):

Windows:
    javac -encoding UTF-8 -d out src\*.java
    java -cp "out;mysql-connector-j-8.4.0.jar" MainFrame

Mac / Linux:
    javac -encoding UTF-8 -d out src/*.java
    java -cp "out:mysql-connector-j-8.4.0.jar" MainFrame

## Where each syllabus concept lives
| Unit | Concept | File |
|---|---|---|
| 1 | Classes Subscriber, Plan, Recharge; constructors; Math class | Subscriber, Plan, Recharge |
| 2 | Plan -> PrepaidPlan / PostpaidPlan, overriding, interface Rechargeable | Plan, PrepaidPlan, PostpaidPlan, Rechargeable, RechargeService |
| 3 | InvalidNumberException; thread for plan-expiry check | InvalidNumberException, ExpiryChecker |
| 4 | JDBC CallableStatement for recharge (stored procedure recharge_plan) | RechargeService, schema.sql |
| 5 | Swing GridLayout forms, BorderLayout, ActionListener | MainFrame |

## Modules
Subscriber records, Plan catalogue, Recharge, Bill generation, Recharge history (tabs in MainFrame).
Tables: subscribers, plans, recharges.
